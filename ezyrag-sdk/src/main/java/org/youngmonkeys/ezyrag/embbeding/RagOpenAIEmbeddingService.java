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

package org.youngmonkeys.ezyrag.embbeding;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfox.util.EzyMapBuilder;
import com.tvd12.ezyhttp.client.HttpClient;
import com.tvd12.ezyhttp.client.request.GetRequest;
import com.tvd12.ezyhttp.client.request.PostRequest;
import com.tvd12.ezyhttp.client.request.RequestEntity;
import com.tvd12.ezyhttp.core.constant.ContentTypes;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyrag.constant.EmbeddingServiceName;
import org.youngmonkeys.ezyrag.model.RagEmbeddingData;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_OPENAI_EMBEDDING_MODEL;


@AllArgsConstructor
public class RagOpenAIEmbeddingService
    extends EzyLoggable
    implements RagEmbeddingService {

    private final HttpClient httpClient;
    private final EzyRagSettingService ezyRagSettingService;

    @Override
    public float[] embed(
        RagEmbeddingData data,
        int vectorSize
    ) throws Exception {
        String apiKey = ezyRagSettingService.getOpenAiApiKey();
        Map<String, Object> requestBody = EzyMapBuilder.mapBuilder()
            .put("model", getEmbeddingModel())
            .put("input", getEmbeddingInput(data))
            .put("dimensions", vectorSize)
            .toMap();
        Map<String, Object> responseBody = httpClient.call(
            new PostRequest()
                .setURL(getEmbeddingApiUrl())
                .setEntity(
                    RequestEntity.builder()
                        .contentType(ContentTypes.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey)
                        .body(requestBody)
                        .build()
                )
        );
        return extractEmbedding(responseBody);
    }

    protected String getEmbeddingModel() {
        return ezyRagSettingService.getOpenAiEmbeddingModel();
    }

    protected String getEmbeddingApiUrl() {
        return "https://api.openai.com/v1/embeddings";
    }

    protected Object getEmbeddingInput(RagEmbeddingData data) {
        String dataType = data.getDataType();
        if (!CommonContentType.TEXT.toString().equals(dataType)) {
            throw new IllegalArgumentException(
                "Unsupported embedding data type: " + dataType
            );
        }
        Object input = data.getData();
        if (!(input instanceof String)) {
            throw new IllegalArgumentException(
                "Embedding data must be a string"
            );
        }
        return input;
    }

    @SuppressWarnings("unchecked")
    private float[] extractEmbedding(Map<String, Object> responseBody) {
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody
            .get("data");
        List<Number> embedding = (List<Number>) data.get(0).get("embedding");
        float[] result = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); ++i) {
            result[i] = embedding.get(i).floatValue();
        }
        return result;
    }

    @Override
    public String getServiceName() {
        return EmbeddingServiceName.OPENAI.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getModelNames() {
        try {
            String apiKey = ezyRagSettingService.getOpenAiApiKey();
            Map<String, Object> responseBody = httpClient.call(
                new GetRequest()
                    .setURL(getModelsApiUrl())
                    .setEntity(
                        RequestEntity.builder()
                            .header("Authorization", "Bearer " + apiKey)
                            .build()
                    )
            );
            List<Map<String, Object>> data =
                (List<Map<String, Object>>) responseBody.get("data");
            List<String> modelNames = new ArrayList<>();
            for (Map<String, Object> model : data) {
                String modelId = (String) model.get("id");
                if (modelId != null && modelId.contains("embedding")) {
                    modelNames.add(modelId);
                }
            }
            return modelNames;
        } catch (Exception e) {
            logger.info("Cannot get OpenAI model names", e);
            return Collections.singletonList(
                DEFAULT_OPENAI_EMBEDDING_MODEL
            );
        }
    }

    protected String getModelsApiUrl() {
        return "https://api.openai.com/v1/models";
    }
}

