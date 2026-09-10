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

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfox.util.EzyMapBuilder;
import com.tvd12.ezyhttp.client.HttpClient;
import com.tvd12.ezyhttp.client.request.GetRequest;
import com.tvd12.ezyhttp.client.request.PostRequest;
import com.tvd12.ezyhttp.client.request.PutRequest;
import com.tvd12.ezyhttp.client.request.RequestEntity;
import com.tvd12.ezyhttp.core.constant.ContentTypes;
import com.tvd12.ezyhttp.core.exception.HttpConflictException;
import org.youngmonkeys.ezyplatform.service.MutableSettingService;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;
import org.youngmonkeys.ezyrag.model.RagQdrantConnectionPropertiesModel;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;
import org.youngmonkeys.ezyrag.model.RagVectorPointModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.model.VectorCollectionModel;
import org.youngmonkeys.ezyrag.service.RagVectorCollectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.ZERO_LONG;
import static org.youngmonkeys.ezyplatform.util.Numbers.toLongOrZeroFromObject;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_CONNECTION_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_CONNECTION_PROPERTIES;

public class RagQdrantVectorDatabaseService
    extends EzyLoggable
    implements RagVectorDatabaseService {

    private final HttpClient httpClient;
    private final MutableSettingService settingService;
    private final RagVectorCollectionService vectorCollectionService;

    public RagQdrantVectorDatabaseService(
        HttpClient httpClient,
        MutableSettingService settingService,
        RagVectorCollectionService vectorCollectionService
    ) {
        this.httpClient = httpClient;
        this.settingService = settingService;
        this.vectorCollectionService = vectorCollectionService;
        settingService.watchLastUpdatedTime(
            SETTING_NAME_QDRANT_CONNECTION_PROPERTIES,
            () -> settingService.cacheValueIfNotNull(
                SETTING_NAME_QDRANT_CONNECTION_PROPERTIES,
                readConnectionProperties()
            )
        );
    }

    private RagQdrantConnectionPropertiesModel readConnectionProperties() {
        RagQdrantConnectionPropertiesModel model =
            settingService
                .getObjectValue(
                    SETTING_NAME_QDRANT_CONNECTION_PROPERTIES,
                    RagQdrantConnectionPropertiesModel.class
                );
        if (model != null) {
            model.setApiKey(
                settingService.getPasswordValue(
                    SETTING_NAME_QDRANT_CONNECTION_API_KEY
                )
            );
        }
        return model;
    }

    @Override
    public void createCollectionIfAbsent(
        RagVectorCollectionModel collection
    ) throws Exception {
        RagQdrantConnectionPropertiesModel properties =
            getConnectionProperties();
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put(
                "vectors",
                EzyMapBuilder.mapBuilder()
                    .put("size", collection.getVectorSize())
                    .put("distance", "Cosine")
                    .toMap()
            )
            .toMap();
        String collectionName = collection.getName();
        try {
            httpClient.call(
                new PutRequest()
                    .setURL(
                        getCollectionUrl(
                            collection.getBaseUrl(properties::getBaseUrl),
                            collectionName
                        )
                    )
                    .setEntity(
                        requestEntity(
                            properties.getApiKey(),
                            requestBody
                        )
                    )
            );
        } catch (HttpConflictException e) {
            logger.info("collection: {} existed", collectionName);
        }
        refreshQdrantVectorSize(
            collection.getId(),
            collectionName,
            properties
        );
    }

    @Override
    public void upsert(
        VectorCollectionModel collection,
        List<RagVectorPointModel> points
    ) throws Exception {
        RagQdrantConnectionPropertiesModel properties =
            getConnectionProperties();
        List<Map<String, Object>> requestPoints = new ArrayList<>(points.size());
        for (RagVectorPointModel point : points) {
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
                .setURL(
                    getPointsUrl(
                        collection.getBaseUrl(properties::getBaseUrl),
                        collection.getName()
                    ) + "?wait=true"
                )
                .setEntity(
                    requestEntity(
                        properties.getApiKey(),
                        requestBody
                    )
                )
        );
    }

    @Override
    public void deletePoints(
        VectorCollectionModel collection,
        List<Long> pointIds
    ) throws Exception {
        RagQdrantConnectionPropertiesModel properties =
            getConnectionProperties();
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put("points", pointIds)
            .toMap();
        httpClient.call(
            new PostRequest()
                .setURL(
                    getPointsUrl(
                        collection.getBaseUrl(properties::getBaseUrl),
                        collection.getName()
                    ) + "/delete?wait=true"
                )
                .setEntity(
                    requestEntity(
                        properties.getApiKey(),
                        requestBody
                    )
                )
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RagVectorSearchResultModel> search(
        VectorCollectionModel collection,
        float[] vector,
        int limit
    ) throws Exception {
        RagQdrantConnectionPropertiesModel properties =
            getConnectionProperties();
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put("vector", vector)
            .put("limit", limit)
            .put("with_payload", true)
            .toMap();
        Map<String, Object> responseBody = httpClient.call(
            new PostRequest()
                .setURL(
                    getPointsUrl(
                        collection.getBaseUrl(properties::getBaseUrl),
                        collection.getName()
                    ) + "/search"
                )
                .setEntity(
                    requestEntity(
                        properties.getApiKey(),
                        requestBody
                    )
                )
        );
        List<Map<String, Object>> result =
            (List<Map<String, Object>>) responseBody
                .get("result");
        List<RagVectorSearchResultModel> searchResults =
            new ArrayList<>(result.size());
        for (Map<String, Object> point : result) {
            searchResults.add(
                RagVectorSearchResultModel.builder()
                    .chunkId(toLongOrZeroFromObject(point.get("id")))
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

    private String getCollectionUrl(
        String baseUrl,
        String collectionName
    ) {
        return baseUrl + "/collections/" + collectionName;
    }

    private String getPointsUrl(
        String baseUrl,
        String collectionName
    ) {
        return getCollectionUrl(baseUrl, collectionName) + "/points";
    }

    @SuppressWarnings("unchecked")
    private void refreshQdrantVectorSize(
        long collectionId,
        String collectionName,
        RagQdrantConnectionPropertiesModel properties
    ) throws Exception {
        Map<String, Object> responseBody = httpClient.call(
            new GetRequest()
                .setURL(
                    getCollectionUrl(
                        properties.getBaseUrl(),
                        collectionName
                    )
                )
                .setEntity(
                    requestEntity(
                        properties.getApiKey(),
                        null
                    )
                )
        );
        Map<String, Object> result =
            (Map<String, Object>) responseBody.get("result");
        Map<String, Object> config =
            result == null
                ? null
                : (Map<String, Object>) result.get("config");
        Map<String, Object> params =
            config == null
                ? null
                : (Map<String, Object>) config.get("params");
        Map<String, Object> vectors =
            params == null
                ? null
                : (Map<String, Object>) params.get("vectors");
        long vectorSize = getVectorSize(vectors);
        if (vectorSize <= ZERO_LONG) {
            return;
        }
        vectorCollectionService.updateVectorSize(
            collectionId,
            vectorSize
        );
    }

    @SuppressWarnings("unchecked")
    private long getVectorSize(Map<String, Object> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return ZERO_LONG;
        }
        Object size = vectors.get("size");
        if (size instanceof Number) {
            return ((Number) size).longValue();
        }
        for (Object item : vectors.values()) {
            if (item instanceof Map) {
                Object namedVectorSize =
                    ((Map<String, Object>) item).get("size");
                if (namedVectorSize instanceof Number) {
                    return ((Number) namedVectorSize).longValue();
                }
            }
        }
        return ZERO_LONG;
    }

    private RagQdrantConnectionPropertiesModel getConnectionProperties() {
        RagQdrantConnectionPropertiesModel properties = settingService
            .getCachedValue(SETTING_NAME_QDRANT_CONNECTION_PROPERTIES);
        if (properties == null) {
            throw new IllegalStateException(
                "You need to setup Qdrant first"
            );
        }
        return properties;
    }

    public String getServiceName() {
        return RagVectorDatabaseServiceName.QDRANT.toString();
    }
}
