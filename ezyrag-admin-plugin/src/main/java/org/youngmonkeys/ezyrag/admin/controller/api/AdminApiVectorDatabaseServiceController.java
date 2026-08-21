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
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagRequestToModelConverter;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveMySqlConnectionPropertiesRequest;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveQdrantConnectionPropertiesRequest;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.validator.AdminRagVectorDatabaseServiceValidator;
import org.youngmonkeys.ezyrag.admin.vd.AdminRagMySqlVectorDatabaseService;
import org.youngmonkeys.ezyrag.admin.vd.AdminRagQdrantVectorDatabaseService;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("rag")
@AllArgsConstructor
public class AdminApiVectorDatabaseServiceController {

    private final AdminEzyRagSettingService ezyRagSettingService;
    private final AdminRagQdrantVectorDatabaseService qdrantVectorDatabaseService;
    private final AdminRagMySqlVectorDatabaseService mySqlVectorDatabaseService;
    private final AdminRagVectorDatabaseServiceValidator vectorDatabaseServiceValidator;
    private final AdminEzyRagRequestToModelConverter requestToModelConverter;

    @Description("Update a vector database service's connection settings")
    @DoPut("/vector-database-services/QDRANT/connection-properties")
    public ResponseEntity vectorDatabaseServicesServiceNamePut(
        @RequestBody AdminSaveQdrantConnectionPropertiesRequest request
    ) throws Exception {
        vectorDatabaseServiceValidator.validate(request);
        ezyRagSettingService.setQdrantConnectionProperties(
            requestToModelConverter.toModel(request)
        );
        ezyRagSettingService.setQdrantVectorSize(
            request.getVectorSize()
        );
        qdrantVectorDatabaseService.createCollectionIfAbsent();
        return ResponseEntity.noContent();
    }

    @Description("Update a vector database service's connection settings")
    @DoPut("/vector-database-services/MYSQL/connection-properties")
    public ResponseEntity vectorDatabaseServicesMySqlPut(
        @RequestBody AdminSaveMySqlConnectionPropertiesRequest request
    ) throws Exception {
        vectorDatabaseServiceValidator.validate(request);
        ezyRagSettingService.setMySqlConnectionProperties(
            requestToModelConverter.toModel(request)
        );
        ezyRagSettingService.setMySqlCollectionName(
            request.getCollectionName()
        );
        ezyRagSettingService.setMySqlVectorSize(
            request.getVectorSize()
        );
        mySqlVectorDatabaseService.createCollectionIfAbsent();
        return ResponseEntity.noContent();
    }
}
