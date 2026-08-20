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
import org.youngmonkeys.ezyrag.admin.response.AdminRagDataChunkResponse;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;

import java.util.Map;

@EzySingleton
public class AdminEzyRagModelToResponseConverter {

    public AdminRagDataChunkResponse toDataChunkResponse(
        RagDataChunkModel model,
        Map<String, String> metadata
    ) {
        return AdminRagDataChunkResponse.builder()
            .id(model.getId())
            .sourceType(model.getSourceType())
            .sourceId(model.getSourceId())
            .chunkIndex(model.getChunkIndex())
            .content(model.getContent())
            .contentHash(model.getContentHash())
            .hasEmbedding(model.getEmbedding() != null)
            .metadata(metadata)
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .build();
    }
}
