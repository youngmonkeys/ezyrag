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
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyrag.admin.controller.decorator.AdminRagDataChunkModelDecorator;
import org.youngmonkeys.ezyrag.admin.pagination.AdminRagDataChunkPaginationParameterConverter;
import org.youngmonkeys.ezyrag.admin.response.AdminRagDataChunkResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminPaginationRagDataChunkService;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkFilter;

import static org.youngmonkeys.ezyplatform.pagination.PaginationModelFetchers.getPaginationModelBySortOrder;

@Service
@AllArgsConstructor
public class AdminRagDataChunkControllerService {

    private final AdminRagDataChunkModelDecorator dataChunkModelDecorator;
    private final AdminPaginationRagDataChunkService paginationDataChunkService;
    private final AdminRagDataChunkPaginationParameterConverter
        paginationParameterConverter;

    public PaginationModel<AdminRagDataChunkResponse> getDataChunks(
        RagDataChunkFilter filter,
        String sortOrder,
        String nextPageToken,
        String prevPageToken,
        boolean lastPage,
        int limit
    ) {
        PaginationModel<RagDataChunkModel> pagination =
            getPaginationModelBySortOrder(
                paginationDataChunkService,
                paginationParameterConverter,
                filter,
                sortOrder,
                nextPageToken,
                prevPageToken,
                lastPage,
                limit
            );
        return dataChunkModelDecorator.decorateToDataChunkPaginationResponse(
            pagination
        );
    }
}
