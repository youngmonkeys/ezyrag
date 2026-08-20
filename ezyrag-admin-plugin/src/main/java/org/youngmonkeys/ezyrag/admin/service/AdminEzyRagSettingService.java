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

package org.youngmonkeys.ezyrag.admin.service;

import com.tvd12.ezyfox.util.EzyMapBuilder;
import com.tvd12.ezyhttp.server.core.annotation.Service;
import org.youngmonkeys.ezyplatform.admin.service.AdminSettingService;
import org.youngmonkeys.ezyrag.model.RagQdrantConnectionPropertiesModel;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import static org.youngmonkeys.ezyplatform.constant.CommonConstants.PATTERN_HIDDEN_PASSWORD;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_OPENAI_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_CONNECTION_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_CONNECTION_PROPERTIES;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_VECTOR_SIZE;

@Service
public class AdminEzyRagSettingService extends EzyRagSettingService {

    private final AdminSettingService settingService;

    public AdminEzyRagSettingService(
        AdminSettingService settingService
    ) {
        super(settingService);
        this.settingService = settingService;
    }

    public void setOpenAiApiKey(String apiKey) {
        if (!apiKey.matches(PATTERN_HIDDEN_PASSWORD)) {
            settingService.setPasswordValue(
                SETTING_NAME_OPENAI_API_KEY,
                apiKey
            );
        }
    }

    public void setQdrantConnectionProperties(
        RagQdrantConnectionPropertiesModel model
    ) {
        settingService.setObjectValue(
            SETTING_NAME_QDRANT_CONNECTION_PROPERTIES,
            EzyMapBuilder.mapBuilder()
                .put("baseUrl", model.getBaseUrl())
                .put("collectionName", model.getCollectionName())
                .toMap()
        );
        String apiKey = model.getApiKey();
        if (apiKey.matches(PATTERN_HIDDEN_PASSWORD)) {
            apiKey = settingService.getPasswordValue(
                SETTING_NAME_QDRANT_CONNECTION_API_KEY
            );
        } else {
            settingService.setPasswordValue(
                SETTING_NAME_QDRANT_CONNECTION_API_KEY,
                apiKey
            );
        }
        model.setApiKey(apiKey);
        settingService.cacheValueIfNotNull(
            SETTING_NAME_QDRANT_CONNECTION_PROPERTIES,
            model
        );
        settingService.setLastUpdateTime(
            SETTING_NAME_QDRANT_CONNECTION_PROPERTIES
        );
    }

    public void setQdrantVectorSize(int vectorSize) {
        settingService.setIntValue(
            SETTING_NAME_QDRANT_VECTOR_SIZE,
            vectorSize
        );
        settingService.cacheValueIfNotNull(
            SETTING_NAME_QDRANT_VECTOR_SIZE,
            vectorSize
        );
    }

    public RagQdrantConnectionPropertiesModel getConnectionPropertiesInDb() {
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
        return model != null
            ? model
            : new RagQdrantConnectionPropertiesModel();
    }
}
