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
import com.tvd12.ezyhttp.server.core.annotation.*;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveVectorDatabaseServiceRequest;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.validator.AdminRagVectorDatabaseServiceValidator;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("rag")
@AllArgsConstructor
public class AdminApiVectorDatabaseServiceController {

    private final AdminRagVectorDatabaseServiceValidator vectorDatabaseServiceValidator;
    private final AdminEzyRagSettingService ezyRagSettingService;

    @Description("Update a vector database service's connection settings")
    @DoPut("/vector-database-services/{serviceName}")
    public ResponseEntity vectorDatabaseServicesServiceNamePut(
        @PathVariable String serviceName,
        @RequestBody AdminSaveVectorDatabaseServiceRequest request
    ) {
        vectorDatabaseServiceValidator.validateServiceName(serviceName);
        if (RagVectorDatabaseServiceName.QDRANT.equalsValue(serviceName)) {
            ezyRagSettingService.setQdrantBaseUrl(request.getBaseUrl());
            ezyRagSettingService.setQdrantApiKey(request.getApiKey());
            ezyRagSettingService.setQdrantCollectionName(
                request.getCollectionName()
            );
        }
        return ResponseEntity.noContent();
    }
}
