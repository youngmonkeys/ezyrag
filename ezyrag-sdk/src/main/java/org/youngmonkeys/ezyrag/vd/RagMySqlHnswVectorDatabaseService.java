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
import org.youngmonkeys.ezyrag.entity.RagCollection;
import org.youngmonkeys.ezyrag.entity.RagCollectionPoint;
import org.youngmonkeys.ezyrag.model.RagVectorPointModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.repo.RagCollectionPointRepository;
import org.youngmonkeys.ezyrag.repo.RagCollectionRepository;
import org.youngmonkeys.ezyrag.vd.hnsw.HnswIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RagMySqlHnswVectorDatabaseService
    extends RagMySqlVectorDatabaseService {

    private final RagCollectionRepository collectionRepository;
    private final RagCollectionPointRepository collectionPointRepository;
    private final HnswIndex index = new HnswIndex();
    private final Map<Long, Map<String, Object>> payloadById =
        new ConcurrentHashMap<>();
    private final Object loadLock = new Object();
    private volatile boolean indexLoaded = false;

    public RagMySqlHnswVectorDatabaseService(
        MutableSettingService settingService,
        RagCollectionRepository collectionRepository,
        RagCollectionPointRepository collectionPointRepository,
        ObjectMapper objectMapper
    ) {
        super(
            settingService,
            collectionRepository,
            collectionPointRepository,
            objectMapper
        );
        this.collectionRepository = collectionRepository;
        this.collectionPointRepository = collectionPointRepository;
    }

    @Override
    public void createCollectionIfAbsent() throws Exception {
        super.createCollectionIfAbsent();
        ensureIndexLoaded();
    }

    @Override
    public void upsert(
        List<RagVectorPointModel> points
    ) throws Exception {
        ensureIndexLoaded();
        super.upsert(points);
        for (RagVectorPointModel point : points) {
            index.insert(point.getId(), point.getVector());
            payloadById.put(point.getId(), point.getPayload());
        }
    }

    @Override
    public List<RagVectorSearchResultModel> search(
        float[] vector,
        int limit
    ) throws Exception {
        ensureIndexLoaded();
        List<HnswIndex.SearchResult> hits = index.search(
            vector,
            limit,
            Math.max(limit * 4, 64)
        );
        List<RagVectorSearchResultModel> results =
            new ArrayList<>(hits.size());
        for (HnswIndex.SearchResult hit : hits) {
            results.add(
                RagVectorSearchResultModel.builder()
                    .chunkId(hit.getId())
                    .score(hit.getScore())
                    .payload(payloadById.get(hit.getId()))
                    .build()
            );
        }
        return results;
    }

    private void ensureIndexLoaded() throws Exception {
        if (indexLoaded) {
            return;
        }
        synchronized (loadLock) {
            if (indexLoaded) {
                return;
            }
            RagCollection collection = collectionRepository
                .findByName(getCollectionName());
            if (collection == null) {
                return;
            }
            List<RagCollectionPoint> points = collectionPointRepository
                .findListByCollectionId(collection.getId());
            for (RagCollectionPoint point : points) {
                index.insert(point.getPointId(), point.getVector());
                payloadById.put(
                    point.getPointId(),
                    toPayloadMap(point.getPayload())
                );
            }
            indexLoaded = true;
        }
    }
}
