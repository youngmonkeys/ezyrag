/*
 * Copyright 2026 youngmonkeys.org
 *
 * Licensed under the ezyplatform, Version 1.0.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://youngmonkeys.org/licenses/ezyplatform-1.0.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.youngmonkeys.ezyrag.vd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfox.util.EzyNext;
import org.youngmonkeys.ezyplatform.service.MutableSettingService;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;
import org.youngmonkeys.ezyrag.entity.RagCollection;
import org.youngmonkeys.ezyrag.entity.RagCollectionPoint;
import org.youngmonkeys.ezyrag.entity.RagCollectionSegment;
import org.youngmonkeys.ezyrag.model.RagVectorPointModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.repo.RagCollectionPointRepository;
import org.youngmonkeys.ezyrag.repo.RagCollectionRepository;
import org.youngmonkeys.ezyrag.repo.RagCollectionSegmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_MYSQL_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_MYSQL_VECTOR_SIZE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_VECTOR_DATA_DIR;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_MYSQL_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_MYSQL_VECTOR_SIZE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_VECTOR_DATA_DIR;

public class EzyVectorDatabase extends EzyLoggable
    implements RagVectorDatabaseService {

    private final MutableSettingService settingService;
    private final RagCollectionRepository collectionRepository;
    private final RagCollectionPointRepository collectionPointRepository;
    private final RagCollectionSegmentRepository collectionSegmentRepository;
    private final ObjectMapper objectMapper;
    private final Object writeLock = new Object();
    private final Set<Long> backfillingCollectionIds =
        ConcurrentHashMap.newKeySet();

    public EzyVectorDatabase(
        MutableSettingService settingService,
        RagCollectionRepository collectionRepository,
        RagCollectionPointRepository collectionPointRepository,
        RagCollectionSegmentRepository collectionSegmentRepository,
        ObjectMapper objectMapper
    ) {
        this.settingService = settingService;
        this.collectionRepository = collectionRepository;
        this.collectionPointRepository = collectionPointRepository;
        this.collectionSegmentRepository = collectionSegmentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createCollectionIfAbsent() throws Exception {
        String collectionName = getCollectionName();
        if (collectionRepository.findByName(collectionName) == null) {
            LocalDateTime now = LocalDateTime.now();
            RagCollection entity = new RagCollection();
            entity.setName(collectionName);
            entity.setVectorSize(getVectorSize());
            entity.setDistance("COSINE");
            entity.setIndexType("EXACT");
            entity.setStatus("ACTIVE");
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            collectionRepository.save(entity);
        }
        ensureMutableSegment();
        startBackfillIfNecessary();
    }

    @Override
    public void upsert(
        List<RagVectorPointModel> points
    ) throws Exception {
        RagCollection collection = getCollectionOrThrow();
        ensureMutableSegment(collection);
        startBackfillIfNecessary(collection);
        LocalDateTime now = LocalDateTime.now();
        EzyVectorFileStorage storage = newVectorFileStorage();
        synchronized (writeLock) {
            List<EzyVectorFileStorage.VectorRecord> records =
                new ArrayList<>(points.size());
            for (RagVectorPointModel point : points) {
                RagCollectionPoint entity = collectionPointRepository
                    .findByCollectionIdAndPointId(
                        collection.getId(),
                        point.getId()
                    );
                if (entity == null) {
                    entity = new RagCollectionPoint();
                    entity.setCollectionId(collection.getId());
                    entity.setPointId(point.getId());
                    entity.setStatus("ACTIVE");
                    entity.setVersion(1L);
                    entity.setCreatedAt(now);
                } else {
                    entity.setVersion(entity.getVersion() + 1L);
                }
                entity.setVector(point.getVector());
                entity.setPayload(toPayloadJson(point.getPayload()));
                entity.setUpdatedAt(now);
                collectionPointRepository.save(entity);
                records.add(
                    new EzyVectorFileStorage.VectorRecord(
                        entity.getId(),
                        point.getId(),
                        point.getVector()
                    )
                );
            }
            storage.upsertAll(
                collection.getId(),
                collection.getVectorSize(),
                records
            );
        }
    }

    @Override
    public List<RagVectorSearchResultModel> search(
        float[] vector,
        int limit
    ) throws Exception {
        RagCollection collection = getCollectionOrThrow();
        startBackfillIfNecessary(collection);
        List<EzyVectorFileStorage.SearchResult> hits =
            newVectorFileStorage().search(
                collection.getId(),
                collection.getVectorSize(),
                vector,
                limit
            );
        List<RagVectorSearchResultModel> results =
            new ArrayList<>(hits.size());
        for (EzyVectorFileStorage.SearchResult hit : hits) {
            RagCollectionPoint point = collectionPointRepository
                .findByCollectionIdAndPointId(
                    collection.getId(),
                    hit.getId()
                );
            results.add(
                RagVectorSearchResultModel.builder()
                    .chunkId(hit.getId())
                    .score(hit.getScore())
                    .payload(
                        point == null
                            ? null
                            : toPayloadMap(point.getPayload())
                    )
                    .build()
            );
        }
        return results;
    }

    @Override
    public int getVectorSize() {
        return settingService.getIntValue(
            SETTING_NAME_MYSQL_VECTOR_SIZE,
            DEFAULT_MYSQL_VECTOR_SIZE
        );
    }

    @Override
    public String getProviderName() {
        return RagVectorDatabaseServiceName.MYSQL.toString();
    }

    public String getCollectionName() {
        return settingService.getTextValue(
            SETTING_NAME_MYSQL_COLLECTION_NAME,
            DEFAULT_MYSQL_COLLECTION_NAME
        );
    }

    private RagCollection getCollectionOrThrow() {
        RagCollection collection = collectionRepository
            .findByName(getCollectionName());
        if (collection == null) {
            throw new IllegalStateException(
                "You need to setup MySQL vector database first"
            );
        }
        return collection;
    }

    private void ensureMutableSegment() {
        RagCollection collection = collectionRepository
            .findByName(getCollectionName());
        if (collection != null) {
            ensureMutableSegment(collection);
        }
    }

    private void ensureMutableSegment(RagCollection collection) {
        RagCollectionSegment segment = collectionSegmentRepository
            .findByCollectionIdAndSegmentNo(collection.getId(), 1L);
        if (segment != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        segment = new RagCollectionSegment();
        segment.setCollectionId(collection.getId());
        segment.setSegmentNo(1L);
        segment.setSegmentType("MUTABLE");
        segment.setStatus("ACTIVE");
        segment.setIndexVersion(1L);
        segment.setCreatedAt(now);
        segment.setUpdatedAt(now);
        collectionSegmentRepository.save(segment);
    }

    private void startBackfillIfNecessary() throws Exception {
        RagCollection collection = collectionRepository
            .findByName(getCollectionName());
        if (collection != null) {
            startBackfillIfNecessary(collection);
        }
    }

    private void startBackfillIfNecessary(
        RagCollection collection
    ) throws Exception {
        EzyVectorFileStorage storage = newVectorFileStorage();
        long backfillProgress =
            storage.getBackfillProgress(collection.getId());
        List<RagCollectionPoint> points = collectionPointRepository
            .findListByCollectionIdAndIdGreaterThan(
                collection.getId(),
                backfillProgress,
                EzyNext.fromLimit(1)
            );
        if (points.isEmpty()
            || !backfillingCollectionIds.add(collection.getId())) {
            return;
        }
        Thread thread = new Thread(
            () -> backfillCollection(collection),
            "ezyrag-vector-backfill-" + collection.getId()
        );
        thread.setDaemon(true);
        thread.start();
    }

    private void backfillCollection(RagCollection collection) {
        try {
            EzyVectorFileStorage storage = newVectorFileStorage();
            long lastId = storage.getBackfillProgress(collection.getId());
            while (true) {
                List<RagCollectionPoint> points = collectionPointRepository
                    .findListByCollectionIdAndIdGreaterThan(
                        collection.getId(),
                        lastId,
                        EzyNext.fromLimit(500)
                    );
                if (points.isEmpty()) {
                    return;
                }
                synchronized (writeLock) {
                    List<EzyVectorFileStorage.VectorRecord> records =
                        new ArrayList<>(points.size());
                    for (RagCollectionPoint point : points) {
                        records.add(
                            new EzyVectorFileStorage.VectorRecord(
                                point.getId(),
                                point.getPointId(),
                                point.getVector()
                            )
                        );
                        lastId = point.getId();
                    }
                    storage.upsertAll(
                        collection.getId(),
                        collection.getVectorSize(),
                        records
                    );
                }
                storage.saveBackfillProgress(collection.getId(), lastId);
            }
        } catch (Exception e) {
            logger.warn(
                "backfill vector collection: {} failed",
                collection.getId(),
                e
            );
        } finally {
            backfillingCollectionIds.remove(collection.getId());
        }
    }

    private EzyVectorFileStorage newVectorFileStorage() {
        return new EzyVectorFileStorage(
            settingService.getTextValue(
                SETTING_NAME_VECTOR_DATA_DIR,
                DEFAULT_VECTOR_DATA_DIR
            )
        );
    }

    private String toPayloadJson(
        Map<String, Object> payload
    ) throws Exception {
        return payload == null
            ? null
            : objectMapper.writeValueAsString(payload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toPayloadMap(
        String json
    ) throws Exception {
        return isBlank(json)
            ? null
            : objectMapper.readValue(json, Map.class);
    }
}
