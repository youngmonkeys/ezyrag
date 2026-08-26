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

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.exception.ResourceNotFoundException;
import org.youngmonkeys.ezyrag.converter.EzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.converter.EzyRagModelToEntityConverter;
import org.youngmonkeys.ezyrag.entity.RagVectorCollection;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;
import org.youngmonkeys.ezyrag.model.SaveRagVectorCollectionModel;
import org.youngmonkeys.ezyrag.repo.RagVectorCollectionRepository;

@AllArgsConstructor
public class RagVectorCollectionService {

    private final RagVectorCollectionRepository collectionRepository;
    private final EzyRagEntityToModelConverter entityToModelConverter;
    private final EzyRagModelToEntityConverter modelToEntityConverter;

    public long addRagVectorCollection(
        SaveRagVectorCollectionModel model
    ) {
        RagVectorCollection entity = modelToEntityConverter
            .toVectorCollectionEntity(model);
        collectionRepository.save(entity);
        return entity.getId();
    }

    public void updateRagVectorCollection(
        long bankId,
        SaveRagVectorCollectionModel model
    ) {
        RagVectorCollection entity =
            getRagVectorCollectionEntityByIdOrThrow(bankId);
        modelToEntityConverter
            .mergeToVectorCollectionEntity(model, entity);
        collectionRepository.save(entity);
    }

    public RagVectorCollectionModel getCollectionById(
        long collectionId
    ) {
        return entityToModelConverter.toModel(
            collectionRepository.findById(collectionId)
        );
    }

    public RagVectorCollectionModel getCollectionByName(
        String collectionName
    ) {
        return entityToModelConverter.toModel(
            collectionRepository.findByName(collectionName)
        );
    }

    private RagVectorCollection getRagVectorCollectionEntityByIdOrThrow(
        long bankId
    ) {
        RagVectorCollection entity = collectionRepository
            .findById(bankId);
        if (entity == null) {
            throw new ResourceNotFoundException("bank");
        }
        return entity;
    }
}
