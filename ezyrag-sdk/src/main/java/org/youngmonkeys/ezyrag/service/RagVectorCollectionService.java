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
import org.youngmonkeys.ezyplatform.service.SettingService;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;
import org.youngmonkeys.ezyrag.converter.EzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.converter.EzyRagModelToEntityConverter;
import org.youngmonkeys.ezyrag.converter.EzyRagResultToModelConverter;
import org.youngmonkeys.ezyrag.entity.RagVectorCollection;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;
import org.youngmonkeys.ezyrag.model.SaveRagVectorCollectionModel;
import org.youngmonkeys.ezyrag.model.VectorCollectionModel;
import org.youngmonkeys.ezyrag.repo.RagVectorCollectionRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_VECTOR_DATABASE_SERVICE_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.settingNameDefaultCollectionNameOfVectorDbService;

@AllArgsConstructor
public class RagVectorCollectionService {

    private final SettingService settingService;
    private final RagVectorCollectionRepository collectionRepository;
    private final EzyRagEntityToModelConverter entityToModelConverter;
    private final EzyRagModelToEntityConverter modelToEntityConverter;
    private final EzyRagResultToModelConverter resultToModelConverter;

    public RagVectorCollectionModel addVectorCollection(
        SaveRagVectorCollectionModel model
    ) {
        RagVectorCollection entity = modelToEntityConverter
            .toVectorCollectionEntity(model);
        collectionRepository.save(entity);
        return entityToModelConverter.toModel(entity);
    }

    public RagVectorCollectionModel updateVectorCollection(
        long collectionId,
        SaveRagVectorCollectionModel model
    ) {
        RagVectorCollection entity =
            getRagVectorCollectionEntityByIdOrThrow(collectionId);
        modelToEntityConverter
            .mergeToVectorCollectionEntity(model, entity);
        collectionRepository.save(entity);
        return entityToModelConverter.toModel(entity);
    }

    public void updateVectorSize(
        long collectionId,
        long vectorSize
    ) {
        RagVectorCollection entity =
            getRagVectorCollectionEntityByIdOrThrow(collectionId);
        entity.setVectorSize(vectorSize);
        modelToEntityConverter.mergeUpdatedAtToCollectionEntity(
            entity
        );
        collectionRepository.save(entity);
    }

    public void updateVectorCollectionStatus(
        long collectionId,
        String status
    ) {
        RagVectorCollection entity =
            getRagVectorCollectionEntityByIdOrThrow(collectionId);
        entity.setStatus(status);
        modelToEntityConverter.mergeUpdatedAtToCollectionEntity(
            entity
        );
        collectionRepository.save(entity);
    }

    public void deleteVectorCollectionById(
        long collectionId
    ) {
        collectionRepository.delete(collectionId);
    }

    public RagVectorCollectionModel getCollectionById(
        long collectionId
    ) {
        return entityToModelConverter.toModel(
            collectionRepository.findById(collectionId)
        );
    }

    public RagVectorCollectionModel getCollectionByVectorDbServiceNameAndName(
        String vectorDbServiceName,
        String collectionName
    ) {
        return entityToModelConverter.toModel(
            collectionRepository
                .findByVectorDbServiceAndName(
                    vectorDbServiceName,
                    collectionName
                )
        );
    }

    public VectorCollectionModel getVectorCollectionByDbServiceNameAndCollectionName(
        String vectorDbServiceName,
        String collectionName
    ) {
        return resultToModelConverter.toModel(
            collectionRepository
                .findCollectionByVectorDbServiceAndName(
                    vectorDbServiceName,
                    collectionName
                )
        );
    }

    public VectorCollectionModel getDefaultVectorCollection() {
        String serviceName = settingService.getTextValue(
            SETTING_NAME_VECTOR_DATABASE_SERVICE_NAME,
            RagVectorDatabaseServiceName.EZYVECTOR.toString()
        );
        if (isBlank(serviceName)) {
            return null;
        }
        String collectionName = settingService.getTextValue(
            settingNameDefaultCollectionNameOfVectorDbService(
                serviceName
            )
        );
        if (isBlank(collectionName)) {
            return null;
        }
        return getVectorCollectionByDbServiceNameAndCollectionName(
            collectionName,
            serviceName
        );
    }

    public Map<Long, RagVectorCollectionModel> getVectorCollectionMapByIds(
        Collection<Long> collectionIds
    ) {
        if (collectionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return collectionRepository
            .findListByIds(collectionIds)
            .stream()
            .collect(
                Collectors.toMap(
                    RagVectorCollection::getId,
                    entityToModelConverter::toModel
                )
            );
    }

    private RagVectorCollection getRagVectorCollectionEntityByIdOrThrow(
        long collectionId
    ) {
        RagVectorCollection entity = collectionRepository
            .findById(collectionId);
        if (entity == null) {
            throw new ResourceNotFoundException("vectorCollection");
        }
        return entity;
    }
}
