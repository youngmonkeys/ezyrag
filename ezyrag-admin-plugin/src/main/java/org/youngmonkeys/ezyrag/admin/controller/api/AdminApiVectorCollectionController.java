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
import com.tvd12.ezyhttp.server.core.annotation.DoDelete;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.annotation.DoPost;
import com.tvd12.ezyhttp.server.core.annotation.DoPut;
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.admin.validator.AdminCommonValidator;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyplatform.response.AddedIdResponse;
import org.youngmonkeys.ezyrag.admin.controller.service.AdminRagVectorCollectionControllerService;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagRequestToModelConverter;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveRagVectorCollectionRequest;
import org.youngmonkeys.ezyrag.admin.response.AdminRagVectorCollectionDetailsResponse;
import org.youngmonkeys.ezyrag.admin.response.AdminRagVectorCollectionResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.service.AdminRagVectorCollectionService;
import org.youngmonkeys.ezyrag.admin.validator.AdminRagVectorCollectionValidator;
import org.youngmonkeys.ezyrag.admin.vd.AdminRagVectorDatabaseServiceManager;
import org.youngmonkeys.ezyrag.entity.RagVectorCollectionStatus;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;
import org.youngmonkeys.ezyrag.pagination.DefaultRagVectorCollectionFilter;
import org.youngmonkeys.ezyrag.vd.RagVectorDatabaseService;

import static org.youngmonkeys.ezyplatform.util.StringConverters.trimOrNull;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("rag")
@AllArgsConstructor
public class AdminApiVectorCollectionController {

    private final AdminRagVectorDatabaseServiceManager
        vectorDatabaseServiceManager;
    private final AdminEzyRagSettingService ezyRagSettingService;
    private final AdminRagVectorCollectionService vectorCollectionService;
    private final AdminRagVectorCollectionControllerService
        vectorCollectionControllerService;
    private final AdminCommonValidator commonValidator;
    private final AdminRagVectorCollectionValidator vectorCollectionValidator;
    private final AdminEzyRagRequestToModelConverter requestToModelConverter;

    @Description("Get the vector collections with pagination")
    @DoGet("/vector-collections")
    public PaginationModel<AdminRagVectorCollectionResponse> vectorCollectionsGet(
        @RequestParam(value = "vectorDbServiceName") String vectorDbServiceName,
        @RequestParam(value = "keyword") String keyword,
        @RequestParam(value = "status") String status,
        @RequestParam(value = "sortOrder") String sortOrder,
        @RequestParam(value = "nextPageToken") String nextPageToken,
        @RequestParam(value = "prevPageToken") String prevPageToken,
        @RequestParam(value = "lastPage") boolean lastPage,
        @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        commonValidator.validatePageSize(limit);
        return vectorCollectionControllerService.getVectorCollections(
            DefaultRagVectorCollectionFilter.builder()
                .vectorDbService(trimOrNull(vectorDbServiceName))
                .likeKeyword(trimOrNull(keyword))
                .status(trimOrNull(status))
                .build(),
            sortOrder,
            nextPageToken,
            prevPageToken,
            lastPage,
            limit
        );
    }

    @Description("Get vector collection detail")
    @DoGet("/vector-collections/{id}")
    public AdminRagVectorCollectionDetailsResponse vectorCollectionsIdGet(
        @PathVariable long collectionId
    ) {
        return vectorCollectionControllerService
            .getVectorCollectionById(collectionId);
    }

    @Description("Add vector collection")
    @DoPost("/vector-collections/add")
    public AddedIdResponse vectorCollectionsAddPost(
        @RequestBody AdminSaveRagVectorCollectionRequest request
    ) throws Exception {
        vectorCollectionValidator.validate(request);
        RagVectorCollectionModel model = vectorCollectionService
            .addVectorCollection(requestToModelConverter.toModel(request));
        ezyRagSettingService
            .setDefaultCollectionNameByVectorDbServiceNameIfAbsent(
                request.getVectorDbService(),
                request.getName()
            );
        RagVectorDatabaseService service = vectorDatabaseServiceManager
            .getVectorDatabaseServiceByName(request.getVectorDbService());
        if (service != null) {
            service.createCollectionIfAbsent(model);
        }
        return new AddedIdResponse(model.getId());
    }

    @Description("Update vector collection")
    @DoPut("/vector-collections/{id}")
    public ResponseEntity vectorCollectionsIdPut(
        @PathVariable long collectionId,
        @RequestBody AdminSaveRagVectorCollectionRequest request
    ) throws Exception {
        vectorCollectionValidator.validate(collectionId, request);
        RagVectorCollectionModel model = vectorCollectionService
            .updateVectorCollection(
                collectionId,
                requestToModelConverter.toModel(request)
            );
        RagVectorDatabaseService service = vectorDatabaseServiceManager
            .getVectorDatabaseServiceByName(request.getVectorDbService());
        if (service != null) {
            service.createCollectionIfAbsent(model);
        }
        return ResponseEntity.noContent();
    }

    @Description("Delete vector collection")
    @DoDelete("/vector-collections/{id}")
    public ResponseEntity vectorCollectionsIdDelete(
        @PathVariable long collectionId
    ) {
        vectorCollectionService.deleteVectorCollectionById(collectionId);
        return ResponseEntity.noContent();
    }

    @Description("Activate vector collection")
    @DoPut("/vector-collections/{id}/activate")
    public ResponseEntity vectorCollectionsIdActivatePut(
        @PathVariable long collectionId
    ) {
        vectorCollectionService.updateVectorCollectionStatus(
            collectionId,
            RagVectorCollectionStatus.ACTIVATED.toString()
        );
        return ResponseEntity.noContent();
    }

    @Description("Deactivate vector collection")
    @DoPut("/vector-collections/{id}/deactivate")
    public ResponseEntity vectorCollectionsIdDeactivatePut(
        @PathVariable long collectionId
    ) {
        vectorCollectionService.updateVectorCollectionStatus(
            collectionId,
            RagVectorCollectionStatus.INACTIVATED.toString()
        );
        return ResponseEntity.noContent();
    }

    @Description("Set the default vector collection of a vector db service")
    @DoPut(
        "/vector-database-services/{serviceName}" +
            "/vector-collections/{collectionName}/set-as-default"
    )
    public ResponseEntity settingsAiChatServicesProfilesSetAsDefaultPut(
        @PathVariable String serviceName,
        @PathVariable String collectionName
    ) {
        ezyRagSettingService
            .setDefaultCollectionNameByVectorDbServiceName(
                serviceName,
                collectionName
            );
        return ResponseEntity.noContent();
    }
}
