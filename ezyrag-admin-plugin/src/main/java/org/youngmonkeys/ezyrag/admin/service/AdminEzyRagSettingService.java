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

import com.tvd12.ezyhttp.server.core.annotation.Service;
import org.youngmonkeys.ezyplatform.admin.service.AdminSettingService;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.PATTERN_HIDDEN_PASSWORD;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_OPENAI_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_BASE_URL;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_COLLECTION_NAME;

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
        setPasswordValue(SETTING_NAME_OPENAI_API_KEY, apiKey);
    }

    public void setQdrantBaseUrl(String baseUrl) {
        settingService.setTextValue(
            SETTING_NAME_QDRANT_BASE_URL,
            baseUrl
        );
    }

    public void setQdrantApiKey(String apiKey) {
        setPasswordValue(SETTING_NAME_QDRANT_API_KEY, apiKey);
    }

    public void setQdrantCollectionName(String collectionName) {
        settingService.setTextValue(
            SETTING_NAME_QDRANT_COLLECTION_NAME,
            collectionName
        );
    }

    private void setPasswordValue(String settingName, String value) {
        if (isBlank(value)) {
            settingService.removeSetting(settingName);
            return;
        }
        if (!value.matches(PATTERN_HIDDEN_PASSWORD)) {
            settingService.setPasswordValue(settingName, value);
        }
    }
}
