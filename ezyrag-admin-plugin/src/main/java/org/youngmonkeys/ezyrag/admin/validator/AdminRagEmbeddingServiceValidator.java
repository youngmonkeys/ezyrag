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

package org.youngmonkeys.ezyrag.admin.validator;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.core.exception.HttpBadRequestException;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.embbeding.AdminRagEmbeddingServiceManager;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveOpenAIEmbeddingServiceRequest;

import java.util.HashMap;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static java.util.Collections.singletonMap;

@EzySingleton
@AllArgsConstructor
public class AdminRagEmbeddingServiceValidator {

    private final AdminRagEmbeddingServiceManager embeddingServiceManager;

    public void validateServiceName(String serviceName) {
        if (
            embeddingServiceManager
                .getEmbeddingServiceByName(serviceName) == null
        ) {
            throw new HttpBadRequestException(
                singletonMap("serviceName", "invalid")
            );
        }
    }

    public void validate(
        AdminSaveOpenAIEmbeddingServiceRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        if (request == null) {
            throw new HttpBadRequestException(
                singletonMap("request", "required")
            );
        }
        if (isBlank(request.getApiKey())) {
            errors.put("apiKey", "required");
        }
        if (!errors.isEmpty()) {
            throw new HttpBadRequestException(errors);
        }
    }
}
