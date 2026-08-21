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

package org.youngmonkeys.ezyrag.web.controller.api;

import com.tvd12.ezyfox.util.EzyMapBuilder;
import com.tvd12.ezyhttp.core.exception.HttpNotFoundException;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPost;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.model.RagVectorPointModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.web.vd.WebRagMySqlVectorDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.youngmonkeys.ezyplatform.util.Numbers.toIntOrZeroFromObject;
import static org.youngmonkeys.ezyplatform.util.Numbers.toLongOrZeroFromObject;

@Controller("/collections")
@AllArgsConstructor
public class WebApiQdrantCollectionController {

    private final WebRagMySqlVectorDatabaseService vectorDatabaseService;

    @DoPut("/{collectionName}")
    public Map<String, Object> collectionNamePut(
        @PathVariable String collectionName,
        @RequestBody Map<String, Object> request
    ) throws Exception {
        vectorDatabaseService.createCollectionIfAbsent();
        return okResult(true);
    }

    @DoGet("/{collectionName}")
    public Map<String, Object> collectionNameGet(
        @PathVariable String collectionName
    ) throws Exception {
        checkCollectionName(collectionName);
        return okResult(
            EzyMapBuilder.mapBuilder()
                .put(
                    "config",
                    EzyMapBuilder.mapBuilder()
                        .put(
                            "params",
                            EzyMapBuilder.mapBuilder()
                                .put(
                                    "vectors",
                                    EzyMapBuilder.mapBuilder()
                                        .put("size", vectorDatabaseService.getVectorSize())
                                        .toMap()
                                )
                                .toMap()
                        )
                        .toMap()
                )
                .toMap()
        );
    }

    @SuppressWarnings("unchecked")
    @DoPut("/{collectionName}/points")
    public Map<String, Object> collectionNamePointsPut(
        @PathVariable String collectionName,
        @RequestBody Map<String, Object> request
    ) throws Exception {
        checkCollectionName(collectionName);
        List<Map<String, Object>> points =
            (List<Map<String, Object>>) request.get("points");
        vectorDatabaseService.upsert(toPointModels(points));
        return okResult(true);
    }

    @SuppressWarnings("unchecked")
    @DoPost("/{collectionName}/points/search")
    public Map<String, Object> collectionNamePointsSearchPost(
        @PathVariable String collectionName,
        @RequestBody Map<String, Object> request
    ) throws Exception {
        checkCollectionName(collectionName);
        float[] vector = toVector((List<Object>) request.get("vector"));
        int limit = toIntOrZeroFromObject(request.get("limit"));
        List<RagVectorSearchResultModel> results = vectorDatabaseService.search(
            vector,
            limit > 0 ? limit : 10
        );
        return okResult(toResultMaps(results));
    }

    private void checkCollectionName(String collectionName) {
        if (!vectorDatabaseService.getCollectionName().equals(collectionName)) {
            throw new HttpNotFoundException(
                singletonMap("collection", "notFound")
            );
        }
    }

    @SuppressWarnings("unchecked")
    private List<RagVectorPointModel> toPointModels(
        List<Map<String, Object>> points
    ) {
        List<RagVectorPointModel> models = new ArrayList<>(points.size());
        for (Map<String, Object> point : points) {
            models.add(
                RagVectorPointModel.builder()
                    .id(toLongOrZeroFromObject(point.get("id")))
                    .vector(toVector((List<Object>) point.get("vector")))
                    .payload((Map<String, Object>) point.get("payload"))
                    .build()
            );
        }
        return models;
    }

    private List<Map<String, Object>> toResultMaps(
        List<RagVectorSearchResultModel> results
    ) {
        List<Map<String, Object>> resultMaps = new ArrayList<>(results.size());
        for (RagVectorSearchResultModel result : results) {
            resultMaps.add(
                EzyMapBuilder.mapBuilder()
                    .put("id", result.getChunkId())
                    .put("score", result.getScore())
                    .put("payload", result.getPayload())
                    .toMap()
            );
        }
        return resultMaps;
    }

    private float[] toVector(List<Object> values) {
        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); ++i) {
            vector[i] = ((Number) values.get(i)).floatValue();
        }
        return vector;
    }

    private Map<String, Object> okResult(Object result) {
        return EzyMapBuilder.mapBuilder()
            .put("result", result)
            .put("status", "ok")
            .toMap();
    }
}
