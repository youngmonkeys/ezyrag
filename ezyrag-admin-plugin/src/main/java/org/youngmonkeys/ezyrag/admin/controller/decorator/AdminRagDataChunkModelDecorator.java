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
import org.youngmonkeys.ezyplatform.rx.Reactive;
import org.youngmonkeys.ezyrag.admin.converter.AdminEzyRagModelToResponseConverter;
import org.youngmonkeys.ezyrag.admin.response.AdminRagDataChunkResponse;
import org.youngmonkeys.ezyrag.admin.service.AdminRagDataChunkMetaService;
import org.youngmonkeys.ezyrag.admin.service.AdminRagVectorCollectionService;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tvd12.ezyfox.io.EzyLists.newArrayList;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.ZERO_LONG;

@EzySingleton
@AllArgsConstructor
public class AdminRagDataChunkModelDecorator {

    private final AdminRagDataChunkMetaService dataChunkMetaService;
    private final AdminRagVectorCollectionService vectorCollectionService;
    private final AdminEzyRagModelToResponseConverter modelToResponseConverter;

    @SuppressWarnings("LineLength")
    public PaginationModel<AdminRagDataChunkResponse> decorateToDataChunkPaginationResponse(
        PaginationModel<RagDataChunkModel> pagination
    ) {
        List<RagDataChunkModel> models = pagination.getItems();
        List<Long> chunkIds = newArrayList(
            pagination.getItems(),
            RagDataChunkModel::getId
        );
        Set<Long> vectorCollectionIds = models
            .stream()
            .map(RagDataChunkModel::getCollectionId)
            .filter(it -> it > ZERO_LONG)
            .collect(Collectors.toSet());
        return Reactive.multiple()
            .register("vectorCollectionById", () ->
                vectorCollectionService.getVectorCollectionMapByIds(
                    vectorCollectionIds
                )
            )
            .register("metadataByChunkId", () ->
                dataChunkMetaService
                    .getDataChunkMetaMapByIds(
                        chunkIds
                    )
            )
            .blockingGet(map -> {
                Map<Long, RagVectorCollectionModel> vectorCollectionById = map
                    .get("vectorCollectionById");
                Map<Long, Map<String, String>> metadataByChunkId = map
                    .get("metadataByChunkId");
                return pagination.map(it ->
                    modelToResponseConverter.toDataChunkResponse(
                        it,
                        vectorCollectionById.getOrDefault(
                            it.getCollectionId(),
                            RagVectorCollectionModel.builder().build()
                        ),
                        metadataByChunkId.getOrDefault(
                            it.getId(),
                            Collections.emptyMap()
                        )
                    )
                );
            });
    }
}
