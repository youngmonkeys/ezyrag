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
import org.youngmonkeys.ezyrag.vd.hnsw.HnswIndex;

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
    private final Map<Long, HnswIndex> hnswIndexByCollectionId =
        new ConcurrentHashMap<>();
    private final Set<Long> readyHnswCollectionIds =
        ConcurrentHashMap.newKeySet();
    private final Set<Long> buildingHnswCollectionIds =
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
            entity.setIndexType("HNSW");
            entity.setStatus("ACTIVE");
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            collectionRepository.save(entity);
        }
        ensureMutableSegment();
        startBackfillIfNecessary();
        startHnswBuildIfNecessary();
    }

    @Override
    public void upsert(
        List<RagVectorPointModel> points
    ) throws Exception {
        RagCollection collection = getCollectionOrThrow();
        ensureMutableSegment(collection);
        startBackfillIfNecessary(collection);
        startHnswBuildIfNecessary(collection);
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
            updateHnswIndex(collection, records);
        }
        startHnswBuildIfNecessary(collection);
    }

    @Override
    public List<RagVectorSearchResultModel> search(
        float[] vector,
        int limit
    ) throws Exception {
        RagCollection collection = getCollectionOrThrow();
        startBackfillIfNecessary(collection);
        startHnswBuildIfNecessary(collection);
        HnswIndex hnswIndex = getReadyHnswIndex(collection);
        if (hnswIndex != null) {
            List<HnswIndex.SearchResult> hits = hnswIndex.search(
                vector,
                limit,
                Math.max(limit * 8, 64)
            );
            return toSearchResults(collection, hits);
        }
        List<EzyVectorFileStorage.SearchResult> hits =
            newVectorFileStorage().search(
                collection.getId(),
                collection.getVectorSize(),
                vector,
                limit
            );
        return toExactSearchResults(collection, hits);
    }

    private List<RagVectorSearchResultModel> toSearchResults(
        RagCollection collection,
        List<HnswIndex.SearchResult> hits
    ) throws Exception {
        List<RagVectorSearchResultModel> results =
            new ArrayList<>(hits.size());
        for (HnswIndex.SearchResult hit : hits) {
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

    private List<RagVectorSearchResultModel> toExactSearchResults(
        RagCollection collection,
        List<EzyVectorFileStorage.SearchResult> hits
    ) throws Exception {
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
        segment = new RagCollectionSegment();
        segment.setCollectionId(collection.getId());
        segment.setSegmentNo(1L);
        segment.setSegmentType("MUTABLE");
        segment.setStatus("ACTIVE");
        segment.setIndexVersion(1L);
        LocalDateTime now = LocalDateTime.now();
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

    private void startHnswBuildIfNecessary() throws Exception {
        RagCollection collection = collectionRepository
            .findByName(getCollectionName());
        if (collection != null) {
            startHnswBuildIfNecessary(collection);
        }
    }

    private void startHnswBuildIfNecessary(
        RagCollection collection
    ) throws Exception {
        long collectionId = collection.getId();
        if (readyHnswCollectionIds.contains(collectionId)) {
            return;
        }
        EzyVectorFileStorage storage = newVectorFileStorage();
        if (storage.isHnswPresent(collectionId)) {
            try {
                hnswIndexByCollectionId.put(
                    collectionId,
                    HnswIndex.load(storage.getHnswPath(collectionId))
                );
                readyHnswCollectionIds.add(collectionId);
                return;
            } catch (Exception e) {
                logger.warn(
                    "load hnsw index for vector collection: {} failed",
                    collectionId,
                    e
                );
            }
        }
        if (!buildingHnswCollectionIds.add(collectionId)) {
            return;
        }
        hnswIndexByCollectionId.put(collectionId, new HnswIndex());
        Thread thread = new Thread(
            () -> buildHnswIndex(collection),
            "ezyrag-vector-hnsw-build-" + collectionId
        );
        thread.setDaemon(true);
        thread.start();
    }

    private HnswIndex getReadyHnswIndex(
        RagCollection collection
    ) throws Exception {
        long collectionId = collection.getId();
        if (!readyHnswCollectionIds.contains(collectionId)) {
            return null;
        }
        HnswIndex index = hnswIndexByCollectionId.get(collectionId);
        if (index != null) {
            return index;
        }
        EzyVectorFileStorage storage = newVectorFileStorage();
        if (!storage.isHnswPresent(collectionId)) {
            return null;
        }
        try {
            index = HnswIndex.load(storage.getHnswPath(collectionId));
            hnswIndexByCollectionId.put(collectionId, index);
            return index;
        } catch (Exception e) {
            readyHnswCollectionIds.remove(collectionId);
            logger.warn(
                "load hnsw index for vector collection: {} failed",
                collectionId,
                e
            );
            startHnswBuildIfNecessary(collection);
            return null;
        }
    }

    private void updateHnswIndex(
        RagCollection collection,
        List<EzyVectorFileStorage.VectorRecord> records
    ) throws Exception {
        HnswIndex index = hnswIndexByCollectionId.get(collection.getId());
        if (index == null) {
            return;
        }
        for (EzyVectorFileStorage.VectorRecord record : records) {
            index.insert(record.getPointId(), record.getVector());
        }
        if (readyHnswCollectionIds.contains(collection.getId())) {
            index.save(newVectorFileStorage().getHnswPath(collection.getId()));
        }
    }

    private void buildHnswIndex(RagCollection collection) {
        try {
            HnswIndex index = hnswIndexByCollectionId.get(collection.getId());
            if (index == null) {
                index = new HnswIndex();
                hnswIndexByCollectionId.put(collection.getId(), index);
            }
            long lastId = 0L;
            while (true) {
                List<RagCollectionPoint> points = collectionPointRepository
                    .findListByCollectionIdAndIdGreaterThan(
                        collection.getId(),
                        lastId,
                        EzyNext.fromLimit(500)
                    );
                if (points.isEmpty()) {
                    break;
                }
                for (RagCollectionPoint point : points) {
                    index.insert(point.getPointId(), point.getVector());
                    lastId = point.getId();
                }
            }
            index.save(newVectorFileStorage().getHnswPath(collection.getId()));
            readyHnswCollectionIds.add(collection.getId());
        } catch (Exception e) {
            logger.warn(
                "build hnsw index for vector collection: {} failed",
                collection.getId(),
                e
            );
        } finally {
            buildingHnswCollectionIds.remove(collection.getId());
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
