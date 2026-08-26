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

import org.youngmonkeys.ezyplatform.service.CommonPaginationService;
import org.youngmonkeys.ezyrag.converter.EzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.entity.RagVectorCollection;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;
import org.youngmonkeys.ezyrag.pagination.IdDescRagVectorCollectionPaginationParameter;
import org.youngmonkeys.ezyrag.pagination.RagVectorCollectionFilter;
import org.youngmonkeys.ezyrag.pagination.RagVectorCollectionPaginationParameter;
import org.youngmonkeys.ezyrag.pagination.RagVectorCollectionPaginationParameterConverter;
import org.youngmonkeys.ezyrag.repo.PaginationRagVectorCollectionRepository;

public class PaginationRagVectorCollectionService extends CommonPaginationService<
    RagVectorCollectionModel,
    RagVectorCollectionFilter,
    RagVectorCollectionPaginationParameter,
    Long,
    RagVectorCollection> {

    private final EzyRagEntityToModelConverter entityToModelConverter;

    public PaginationRagVectorCollectionService(
        PaginationRagVectorCollectionRepository repository,
        EzyRagEntityToModelConverter entityToModelConverter,
        RagVectorCollectionPaginationParameterConverter paginationParameterConverter
    ) {
        super(repository, paginationParameterConverter);
        this.entityToModelConverter = entityToModelConverter;
    }


    @Override
    protected RagVectorCollectionModel convertEntity(
        RagVectorCollection entity
    ) {
        return entityToModelConverter.toModel(entity);
    }

    @Override
    protected RagVectorCollectionPaginationParameter defaultPaginationParameter() {
        return new IdDescRagVectorCollectionPaginationParameter();
    }
}
