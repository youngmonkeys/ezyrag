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

package org.youngmonkeys.ezyrag.service;

import org.youngmonkeys.ezyrag.converter.EzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.entity.RagDataChunk;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkFilter;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkPaginationParameter;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkPaginationParameterConverter;
import org.youngmonkeys.ezyrag.pagination.IdDescRagDataChunkPaginationParameter;
import org.youngmonkeys.ezyrag.repo.PaginationRagDataChunkRepository;
import org.youngmonkeys.ezyplatform.service.CommonPaginationService;

public class PaginationRagDataChunkService extends CommonPaginationService<
    RagDataChunkModel,
    RagDataChunkFilter,
    RagDataChunkPaginationParameter,
    Long,
    RagDataChunk> {

    private final EzyRagEntityToModelConverter entityToModelConverter;

    public PaginationRagDataChunkService(
        PaginationRagDataChunkRepository repository,
        EzyRagEntityToModelConverter entityToModelConverter,
        RagDataChunkPaginationParameterConverter paginationParameterConverter
    ) {
        super(repository, paginationParameterConverter);
        this.entityToModelConverter = entityToModelConverter;
    }


    @Override
    protected RagDataChunkModel convertEntity(RagDataChunk entity) {
        return entityToModelConverter.toModel(entity);
    }

    @Override
    protected RagDataChunkPaginationParameter defaultPaginationParameter() {
        return new IdDescRagDataChunkPaginationParameter();
    }
}
