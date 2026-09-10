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

package org.youngmonkeys.ezyrag.admin.converter;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyarticle.sdk.entity.PostStatus;
import org.youngmonkeys.ezyarticle.sdk.model.SavePostModel;
import org.youngmonkeys.ezyrag.admin.request.AdminChunkDataRequest;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveEzyVectorConnectionPropertiesRequest;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveQdrantConnectionPropertiesRequest;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveRagVectorCollectionRequest;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagEzyVectorConnectionPropertiesModel;
import org.youngmonkeys.ezyrag.model.RagQdrantConnectionPropertiesModel;
import org.youngmonkeys.ezyrag.model.SaveRagVectorCollectionModel;

import static org.youngmonkeys.ezyai.constant.EzyAIConstants.POST_TYPE_KNOWLEDGE_DATA;

@EzySingleton
public class AdminEzyRagRequestToModelConverter {

    public SavePostModel toSavePostModel(
        AdminChunkDataRequest request
    ) {
        return SavePostModel.builder()
            .postType(POST_TYPE_KNOWLEDGE_DATA)
            .title(request.getTitle())
            .content(request.getData())
            .status(PostStatus.PUBLISHED.toString())
            .build();
    }

    public SaveRagVectorCollectionModel toModel(
        AdminSaveRagVectorCollectionRequest request
    ) {
        return SaveRagVectorCollectionModel.builder()
            .vectorDbService(request.getVectorDbService())
            .name(request.getName())
            .displayName(request.getDisplayName())
            .baseUrl(request.getBaseUrl())
            .vectorSize(request.getVectorSize())
            .distance(request.getDistance())
            .status(request.getStatus())
            .build();
    }

    public RagQdrantConnectionPropertiesModel toModel(
        AdminSaveQdrantConnectionPropertiesRequest request
    ) {
        return RagQdrantConnectionPropertiesModel
            .builder()
            .baseUrl(request.getBaseUrl())
            .apiKey(request.getApiKey())
            .build();
    }

    public RagEzyVectorConnectionPropertiesModel toModel(
        AdminSaveEzyVectorConnectionPropertiesRequest request
    ) {
        return RagEzyVectorConnectionPropertiesModel
            .builder()
            .baseUrl(request.getBaseUrl())
            .apiKey(request.getApiKey())
            .build();
    }

    public RagDataSourceModel toDataSourceModel(
        AdminChunkDataRequest request
    ) {
        return RagDataSourceModel
            .builder()
            .sourceType(request.getSourceType())
            .sourceId(request.getSourceId())
            .data(request.getData())
            .build();
    }
}
