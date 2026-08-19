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

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.util.EzyMapBuilder;
import com.tvd12.ezyhttp.client.HttpClient;
import com.tvd12.ezyhttp.client.request.GetRequest;
import com.tvd12.ezyhttp.client.request.PostRequest;
import com.tvd12.ezyhttp.client.request.PutRequest;
import com.tvd12.ezyhttp.client.request.RequestEntity;
import com.tvd12.ezyhttp.core.constant.ContentTypes;
import com.tvd12.ezyhttp.core.exception.HttpNotFoundException;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.constant.VectorDatabaseProvider;
import org.youngmonkeys.ezyrag.model.VectorPointModel;
import org.youngmonkeys.ezyrag.model.VectorSearchResultModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

@AllArgsConstructor
public class QdrantVectorDatabaseService implements VectorDatabaseService {

    private HttpClient httpClient;

    @Override
    public void createCollectionIfAbsent(
        String baseUrl,
        String apiKey,
        String collectionName,
        int vectorSize
    ) throws Exception {
        if (collectionExists(baseUrl, apiKey, collectionName)) {
            return;
        }
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put(
                "vectors",
                EzyMapBuilder.mapBuilder()
                    .put("size", vectorSize)
                    .put("distance", "Cosine")
                    .toMap()
            )
            .toMap();
        httpClient.call(
            new PutRequest()
                .setURL(getCollectionUrl(baseUrl, collectionName))
                .setEntity(requestEntity(apiKey, requestBody))
        );
    }

    private boolean collectionExists(
        String baseUrl,
        String apiKey,
        String collectionName
    ) throws Exception {
        try {
            httpClient.call(
                new GetRequest()
                    .setURL(getCollectionUrl(baseUrl, collectionName))
                    .setEntity(requestEntity(apiKey, null))
            );
            return true;
        } catch (HttpNotFoundException e) {
            return false;
        }
    }

    @Override
    public void upsert(
        String baseUrl,
        String apiKey,
        String collectionName,
        List<VectorPointModel> points
    ) throws Exception {
        List<Map<String, Object>> requestPoints = new ArrayList<>(points.size());
        for (VectorPointModel point : points) {
            requestPoints.add(
                EzyMapBuilder.mapBuilder()
                    .put("id", point.getId())
                    .put("vector", point.getVector())
                    .put("payload", point.getPayload())
                    .toMap()
            );
        }
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put("points", requestPoints)
            .toMap();
        httpClient.call(
            new PutRequest()
                .setURL(getPointsUrl(baseUrl, collectionName) + "?wait=true")
                .setEntity(requestEntity(apiKey, requestBody))
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<VectorSearchResultModel> search(
        String baseUrl,
        String apiKey,
        String collectionName,
        float[] vector,
        int limit
    ) throws Exception {
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put("vector", vector)
            .put("limit", limit)
            .put("with_payload", true)
            .toMap();
        Map<String, Object> responseBody = httpClient.call(
            new PostRequest()
                .setURL(getPointsUrl(baseUrl, collectionName) + "/search")
                .setEntity(requestEntity(apiKey, requestBody))
        );
        List<Map<String, Object>> result = (List<Map<String, Object>>) responseBody
            .get("result");
        List<VectorSearchResultModel> searchResults = new ArrayList<>(result.size());
        for (Map<String, Object> point : result) {
            searchResults.add(
                VectorSearchResultModel.builder()
                    .id(String.valueOf(point.get("id")))
                    .score(((Number) point.get("score")).floatValue())
                    .payload((Map<String, Object>) point.get("payload"))
                    .build()
            );
        }
        return searchResults;
    }

    private RequestEntity requestEntity(
        String apiKey,
        Map<String, Object> body
    ) {
        RequestEntity.Builder builder = RequestEntity.builder()
            .contentType(ContentTypes.APPLICATION_JSON);
        if (!isBlank(apiKey)) {
            builder.header("api-key", apiKey);
        }
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private String getCollectionUrl(String baseUrl, String collectionName) {
        return baseUrl + "/collections/" + collectionName;
    }

    private String getPointsUrl(String baseUrl, String collectionName) {
        return getCollectionUrl(baseUrl, collectionName) + "/points";
    }

    public String getProviderName() {
        return VectorDatabaseProvider.QDRANT.toString();
    }
}
