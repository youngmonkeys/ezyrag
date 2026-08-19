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

package org.youngmonkeys.ezyrag.service;

import org.youngmonkeys.ezyplatform.service.DefaultSettingService;

import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_QDRANT_BASE_URL;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_QDRANT_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_OPENAI_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_BASE_URL;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_COLLECTION_NAME;

public class EzyRagSettingService {

    private final DefaultSettingService settingService;

    public EzyRagSettingService(DefaultSettingService settingService) {
        this.settingService = settingService;
    }

    public String getOpenAiApiKey() {
        return settingService.getPasswordValue(
            SETTING_NAME_OPENAI_API_KEY
        );
    }

    public String getQdrantBaseUrl() {
        return settingService.getTextValue(
            SETTING_NAME_QDRANT_BASE_URL,
            DEFAULT_QDRANT_BASE_URL
        );
    }

    public String getQdrantApiKey() {
        return settingService.getPasswordValue(
            SETTING_NAME_QDRANT_API_KEY
        );
    }

    public String getQdrantCollectionName() {
        return settingService.getTextValue(
            SETTING_NAME_QDRANT_COLLECTION_NAME,
            DEFAULT_QDRANT_COLLECTION_NAME
        );
    }
}
