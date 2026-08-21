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

package org.youngmonkeys.ezyrag.admin.controller.decorator;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagModelToResponseConverter;
import org.youngmonkeys.ezyrag.admin.response.AdminRagDataChunkResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminRagDataChunkMetaService;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EzySingleton
@AllArgsConstructor
public class AdminRagDataChunkModelDecorator {

    private final AdminRagDataChunkMetaService dataChunkMetaService;
    private final AdminEzyRagModelToResponseConverter modelToResponseConverter;

    @SuppressWarnings("LineLength")
    public PaginationModel<AdminRagDataChunkResponse> decorateToDataChunkPaginationResponse(
        PaginationModel<RagDataChunkModel> pagination
    ) {
        List<RagDataChunkModel> models = pagination.getItems();
        Map<Long, Map<String, String>> metaMap = dataChunkMetaService
            .getDataChunkMetaMapByIds(
                models.stream()
                    .map(RagDataChunkModel::getId)
                    .collect(Collectors.toList())
            );
        return pagination.map(
            model -> modelToResponseConverter
                .toDataChunkResponse(
                    model,
                    metaMap.get(model.getId())
                )
        );
    }
}
