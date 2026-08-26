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
import org.youngmonkeys.ezyarticle.admin.service.AdminPostService;
import org.youngmonkeys.ezyarticle.sdk.entity.PostStatus;
import org.youngmonkeys.ezyarticle.sdk.model.SavePostModel;
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyrag.admin.client.AdminEzyRagClient;
import org.youngmonkeys.ezyrag.admin.controller.decorator.AdminRagDataChunkModelDecorator;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagRequestToModelConverter;
import org.youngmonkeys.ezyrag.admin.pagination.AdminRagDataChunkPaginationParameterConverter;
import org.youngmonkeys.ezyrag.admin.request.AdminChunkDataRequest;
import org.youngmonkeys.ezyrag.admin.response.AdminRagDataChunkResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminPaginationRagDataChunkService;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.VectorCollectionModel;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkFilter;

import static org.youngmonkeys.ezyai.constant.EzyAIConstants.POST_TYPE_KNOWLEDGE_DATA;
import static org.youngmonkeys.ezyarticle.sdk.constant.TableNames.TABLE_NAME_POST;
import static org.youngmonkeys.ezyplatform.pagination.PaginationModelFetchers.getPaginationModelBySortOrder;

@Service
@AllArgsConstructor
public class AdminRagDataChunkControllerService {

    private final AdminEzyRagClient ragClient;
    private final AdminPostService postService;
    private final AdminPaginationRagDataChunkService paginationDataChunkService;
    private final AdminRagDataChunkModelDecorator dataChunkModelDecorator;
    private final AdminRagDataChunkPaginationParameterConverter
        paginationParameterConverter;
    private final AdminEzyRagRequestToModelConverter requestToModelConverter;

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
        return dataChunkModelDecorator
            .decorateToDataChunkPaginationResponse(pagination);
    }

    public void chunkData(
        long adminId,
        VectorCollectionModel collection,
        AdminChunkDataRequest request
    ) throws Exception {
        boolean isTextSourceType = CommonContentType
            .TEXT
            .toString()
            .equalsIgnoreCase(request.getSourceType());
        RagDataSourceModel dataSource = isTextSourceType
            ? toKnowledgeDataPostDataSourceModel(adminId, request)
            : requestToModelConverter.toDataSourceModel(request);
        ragClient.chunkData(collection, dataSource);
    }

    private RagDataSourceModel toKnowledgeDataPostDataSourceModel(
        long adminId,
        AdminChunkDataRequest request
    ) {
        long postId = postService.addPostFromAdmin(
            adminId,
            SavePostModel.builder()
                .postType(POST_TYPE_KNOWLEDGE_DATA)
                .title(request.getTitle())
                .content(request.getData())
                .status(PostStatus.PUBLISHED.toString())
                .build()
        );
        return RagDataSourceModel
            .builder()
            .sourceType(TABLE_NAME_POST)
            .sourceId(postId)
            .build();
    }
}
