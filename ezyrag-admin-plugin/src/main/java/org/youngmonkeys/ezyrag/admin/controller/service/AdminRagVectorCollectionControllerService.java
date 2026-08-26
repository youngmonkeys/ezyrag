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

package org.youngmonkeys.ezyrag.admin.controller.service;

import com.tvd12.ezyhttp.server.core.annotation.Service;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyrag.admin.controller.decorator.AdminRagVectorCollectionModelDecorator;
import org.youngmonkeys.ezyrag.admin.pagination.AdminRagVectorCollectionPaginationParameterConverter;
import org.youngmonkeys.ezyrag.admin.response.AdminRagVectorCollectionDetailsResponse;
import org.youngmonkeys.ezyrag.admin.response.AdminRagVectorCollectionResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminPaginationRagVectorCollectionService;
import org.youngmonkeys.ezyrag.admin.service.AdminRagVectorCollectionService;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;
import org.youngmonkeys.ezyrag.pagination.RagVectorCollectionFilter;

import static org.youngmonkeys.ezyplatform.pagination.PaginationModelFetchers.getPaginationModelBySortOrder;

@Service
@AllArgsConstructor
public class AdminRagVectorCollectionControllerService {

    private final AdminRagVectorCollectionService vectorCollectionService;
    private final AdminRagVectorCollectionModelDecorator vectorCollectionModelDecorator;
    private final AdminPaginationRagVectorCollectionService paginationVectorCollectionService;
    private final AdminRagVectorCollectionPaginationParameterConverter
        vectorCollectionPaginationParameterConverter;

    public AdminRagVectorCollectionDetailsResponse getVectorCollectionById(
        long collectionId
    ) {
        RagVectorCollectionModel collection = vectorCollectionService
            .getCollectionById(collectionId);
        if (collection == null) {
            throw new ResourceNotFoundException("vectorCollection");
        }
        return vectorCollectionModelDecorator
            .decorateToVectorCollectionDetailsResponse(collection);
    }

    public PaginationModel<AdminRagVectorCollectionResponse> getVectorCollections(
        RagVectorCollectionFilter filter,
        String sortOrder,
        String nextPageToken,
        String prevPageToken,
        boolean lastPage,
        int limit
    ) {
        PaginationModel<RagVectorCollectionModel> pagination =
            getPaginationModelBySortOrder(
                paginationVectorCollectionService,
                vectorCollectionPaginationParameterConverter,
                filter,
                sortOrder,
                nextPageToken,
                prevPageToken,
                lastPage,
                limit
            );
        return vectorCollectionModelDecorator
            .decorateToVectorCollectionPaginationResponse(pagination);
    }
}
