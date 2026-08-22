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

package org.youngmonkeys.ezyrag.admin.controller.api;

import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.core.annotation.Description;
import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.Api;
import com.tvd12.ezyhttp.server.core.annotation.Authenticated;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.embbeding.AdminRagEmbeddingServiceManager;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveOpenAIEmbeddingServiceRequest;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.validator.AdminRagEmbeddingServiceValidator;

import java.util.List;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("rag")
@AllArgsConstructor
public class AdminApiEmbeddingServiceController {

    private final AdminEzyRagSettingService ezyRagSettingService;
    private final AdminRagEmbeddingServiceManager embeddingServiceManager;
    private final AdminRagEmbeddingServiceValidator embeddingServiceValidator;

    @Description("Set an embedding service as default")
    @DoPut("/embedding-services/{serviceName}/set-as-default")
    public ResponseEntity embeddingServicesServiceNameSetAsDefaultPut(
        @PathVariable String serviceName
    ) {
        embeddingServiceValidator.validateServiceName(serviceName);
        ezyRagSettingService.setEmbeddingServiceName(serviceName);
        return ResponseEntity.noContent();
    }

    @Description("Update an embedding service's settings")
    @DoPut("/embedding-services/{serviceName}/settings")
    public ResponseEntity embeddingServicesServiceNameSettingsPut(
        @PathVariable String serviceName,
        @RequestBody AdminSaveOpenAIEmbeddingServiceRequest request
    ) {
        embeddingServiceValidator.validateServiceName(serviceName);
        embeddingServiceValidator.validate(request);
        ezyRagSettingService.setOpenAiApiKey(request.getApiKey());
        ezyRagSettingService.setOpenAiEmbeddingModel(request.getModel());
        return ResponseEntity.noContent();
    }

    @Description("Update an embedding service's model names")
    @DoGet("/embedding-services/{serviceName}/model-names")
    public List<String> embeddingServicesServiceNameModelNamesGet(
        @PathVariable String serviceName
    ) {
        return embeddingServiceManager
            .getEmbeddingServiceByName(serviceName)
            .getModelNames();
    }
}
