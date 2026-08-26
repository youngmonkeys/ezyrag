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
import org.youngmonkeys.ecommerce.entity.BankStatus;
import org.youngmonkeys.ecommerce.request.SaveBankRequest;
import org.youngmonkeys.ezyplatform.admin.validator.AdminCommonValidator;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyplatform.response.AddedIdResponse;
import org.youngmonkeys.ezyrag.admin.controller.service.AdminRagVectorCollectionControllerService;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagRequestToModelConverter;
import org.youngmonkeys.ezyrag.admin.response.AdminRagVectorCollectionResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminRagVectorCollectionService;
import org.youngmonkeys.ezyrag.admin.validator.AdminRagVectorCollectionValidator;
import org.youngmonkeys.ezyrag.entity.RagVectorCollection;
import org.youngmonkeys.ezyrag.pagination.DefaultRagVectorCollectionFilter;
import org.youngmonkeys.ezyrag.service.RagVectorCollectionService;

import static org.youngmonkeys.ezyplatform.util.StringConverters.trimOrNull;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("rag")
@AllArgsConstructor
public class AdminApiVectorCollectionController {

    private final AdminRagVectorCollectionService ragVectorCollectionService;
    private final AdminRagVectorCollectionControllerService
        vectorCollectionControllerService;
    private final AdminCommonValidator commonValidator;
    private final AdminRagVectorCollectionValidator vectorCollectionValidator;
    private final AdminEzyRagRequestToModelConverter requestToModelConverter;

    @Description("Get the data chunks with pagination")
    @DoGet("/vector-collections")
    public PaginationModel<AdminRagVectorCollectionResponse> vectorCollectionsGet(
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

    @Description("Add bank")
    @DoPost("/banks/add")
    public AddedIdResponse banksAddPost(
        @RequestBody SaveBankRequest request
    ) {
        vectorCollectionValidator.validate(request);
        long addedId = bankService.addBank(
            requestToModelConverter.toModel(request)
        );
        return new AddedIdResponse(addedId);
    }

    @Description("Update bank")
    @DoPut("/banks/{id}")
    public ResponseEntity banksIdPut(
        @PathVariable long bankId,
        @RequestBody SaveBankRequest request
    ) {
        bankValidator.validate(bankId, request);
        bankService.updateBank(
            bankId,
            requestToModelConverter.toModel(request)
        );
        return ResponseEntity.noContent();
    }

    @Description("Delete bank")
    @DoDelete("/banks/{id}")
    public ResponseEntity banksIdDelete(
        @PathVariable long bankId
    ) {
        ragVectorCollectionService.deleteBankById(bankId);
        return ResponseEntity.noContent();
    }

    @Description("Activate bank")
    @DoPut("/banks/{id}/activate")
    public ResponseEntity banksIdActivatePut(
        @PathVariable long bankId
    ) {
        ragVectorCollectionService.updateBankStatus(
            bankId,
            BankStatus.ACTIVATED.toString()
        );
        return ResponseEntity.noContent();
    }

    @Description("Deactivate bank")
    @DoPut("/banks/{id}/deactivate")
    public ResponseEntity banksIdDeactivatePut(
        @PathVariable long bankId
    ) {
        ragVectorCollectionService.updateBankStatus(
            bankId,
            BankStatus.INACTIVATED.toString()
        );
        return ResponseEntity.noContent();
    }
}
