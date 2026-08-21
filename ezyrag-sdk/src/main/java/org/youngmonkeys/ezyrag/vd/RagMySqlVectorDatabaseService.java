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
import org.youngmonkeys.ezyplatform.service.MutableSettingService;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;
import org.youngmonkeys.ezyrag.entity.RagCollection;
import org.youngmonkeys.ezyrag.entity.RagCollectionPoint;
import org.youngmonkeys.ezyrag.model.RagVectorPointModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.repo.RagCollectionPointRepository;
import org.youngmonkeys.ezyrag.repo.RagCollectionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_MYSQL_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_MYSQL_VECTOR_SIZE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_MYSQL_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_MYSQL_VECTOR_SIZE;

public class RagMySqlVectorDatabaseService
    implements RagVectorDatabaseService {

    private final MutableSettingService settingService;
    private final RagCollectionRepository collectionRepository;
    private final RagCollectionPointRepository collectionPointRepository;
    private final ObjectMapper objectMapper;

    public RagMySqlVectorDatabaseService(
        MutableSettingService settingService,
        RagCollectionRepository collectionRepository,
        RagCollectionPointRepository collectionPointRepository,
        ObjectMapper objectMapper
    ) {
        this.settingService = settingService;
        this.collectionRepository = collectionRepository;
        this.collectionPointRepository = collectionPointRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createCollectionIfAbsent() throws Exception {
        String collectionName = getCollectionName();
        if (collectionRepository.findByName(collectionName) != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        RagCollection entity = new RagCollection();
        entity.setName(collectionName);
        entity.setVectorSize(getVectorSize());
        entity.setDistance("Cosine");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        collectionRepository.save(entity);
    }

    @Override
    public void upsert(
        List<RagVectorPointModel> points
    ) throws Exception {
        RagCollection collection = getCollectionOrThrow();
        LocalDateTime now = LocalDateTime.now();
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
                entity.setCreatedAt(now);
            }
            entity.setVector(point.getVector());
            entity.setPayload(toPayloadJson(point.getPayload()));
            entity.setUpdatedAt(now);
            collectionPointRepository.save(entity);
        }
    }

    @Override
    public List<RagVectorSearchResultModel> search(
        float[] vector,
        int limit
    ) throws Exception {
        RagCollection collection = getCollectionOrThrow();
        List<RagCollectionPoint> points = collectionPointRepository
            .findListByCollectionId(collection.getId());
        List<RagVectorSearchResultModel> results =
            new ArrayList<>(points.size());
        for (RagCollectionPoint point : points) {
            results.add(
                RagVectorSearchResultModel.builder()
                    .chunkId(point.getPointId())
                    .score(cosineSimilarity(vector, point.getVector()))
                    .payload(toPayloadMap(point.getPayload()))
                    .build()
            );
        }
        results.sort(
            (a, b) -> Float.compare(b.getScore(), a.getScore())
        );
        return results.size() > limit
            ? new ArrayList<>(results.subList(0, limit))
            : results;
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

    protected String getCollectionName() {
        return settingService.getTextValue(
            SETTING_NAME_MYSQL_COLLECTION_NAME,
            DEFAULT_MYSQL_COLLECTION_NAME
        );
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f;
        float normA = 0f;
        float normB = 0f;
        for (int i = 0; i < a.length; ++i) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0f || normB == 0f) {
            return 0f;
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private String toPayloadJson(
        Map<String, Object> payload
    ) throws Exception {
        return payload == null
            ? null
            : objectMapper.writeValueAsString(payload);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> toPayloadMap(
        String json
    ) throws Exception {
        return isBlank(json)
            ? null
            : objectMapper.readValue(json, Map.class);
    }
}
