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
import com.tvd12.ezyhttp.server.core.annotation.DoPost;
import com.tvd12.ezyhttp.server.core.annotation.RequestBody;
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.admin.validator.AdminCommonValidator;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyrag.admin.client.AdminEzyRagClient;
import org.youngmonkeys.ezyrag.admin.controller.service.AdminRagDataChunkControllerService;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagRequestToModelConverter;
import org.youngmonkeys.ezyrag.admin.request.AdminChunkDataRequest;
import org.youngmonkeys.ezyrag.admin.response.AdminRagDataChunkResponse;
import org.youngmonkeys.ezyrag.pagination.DefaultRagDataChunkFilter;

import static org.youngmonkeys.ezyplatform.util.StringConverters.trimOrNull;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("rag")
@AllArgsConstructor
public class AdminApiDataChunkController {

    private final AdminEzyRagClient ragClient;
    private final AdminRagDataChunkControllerService dataChunkControllerService;
    private final AdminCommonValidator commonValidator;
    private final AdminEzyRagRequestToModelConverter requestToModelConverter;

    @Description("Get the data chunks with pagination")
    @DoGet("/data-chunks")
    public PaginationModel<AdminRagDataChunkResponse> dataChunksGet(
        @RequestParam(value = "sourceType") String sourceType,
        @RequestParam(value = "sourceId", defaultValue = "0") long sourceId,
        @RequestParam(value = "keyword") String keyword,
        @RequestParam(value = "sortOrder") String sortOrder,
        @RequestParam(value = "nextPageToken") String nextPageToken,
        @RequestParam(value = "prevPageToken") String prevPageToken,
        @RequestParam(value = "lastPage") boolean lastPage,
        @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        commonValidator.validatePageSize(limit);
        return dataChunkControllerService.getDataChunks(
            DefaultRagDataChunkFilter.builder()
                .sourceType(trimOrNull(sourceType))
                .sourceId(sourceId > 0 ? sourceId : null)
                .likeKeyword(trimOrNull(keyword))
                .build(),
            sortOrder,
            nextPageToken,
            prevPageToken,
            lastPage,
            limit
        );
    }

    @DoPost("/chunk-data")
    public ResponseEntity chunkPost(
        @RequestBody AdminChunkDataRequest request
    ) throws Exception {
        ragClient.chunkData(
            requestToModelConverter.toDataSourceModel(
                request
            )
        );
        return ResponseEntity.noContent();
    }
}
