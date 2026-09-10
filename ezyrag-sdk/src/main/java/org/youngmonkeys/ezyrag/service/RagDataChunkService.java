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
import org.youngmonkeys.ezyrag.converter.EzyRagResultToModelConverter;
import org.youngmonkeys.ezyrag.entity.RagDataChunk;
import org.youngmonkeys.ezyrag.model.RagDataChunkEmbeddingModel;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagSaveDataChunkModel;
import org.youngmonkeys.ezyrag.repo.RagDataChunkRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.tvd12.ezyfox.io.EzyLists.newArrayList;

@AllArgsConstructor
public class RagDataChunkService {

    private final RagDataChunkRepository dataChunkRepository;
    private final EzyRagEntityToModelConverter entityToModelConverter;
    private final EzyRagModelToEntityConverter modelToEntityConverter;
    private final EzyRagResultToModelConverter resultToModelConverter;

    public long addDataChunk(
        RagSaveDataChunkModel model
    ) {
        RagDataChunk entity = modelToEntityConverter
            .toRagDataChunkEntity(model);
        dataChunkRepository.save(entity);
        return entity.getId();
    }

    public void updateDataChunk(
        long chunkId,
        RagSaveDataChunkModel model
    ) {
        RagDataChunk entity = getDataChunkEntityByIdOrThrow(chunkId);
        modelToEntityConverter.mergeToRagDataChunkEntity(model, entity);
        dataChunkRepository.save(entity);
    }

    public void updateEmbeddingById(
        long chunkId,
        String embeddingService,
        float[] embedding
    ) {
        dataChunkRepository.updateEmbeddingById(
            chunkId,
            embeddingService,
            embedding
        );
    }

    public void deleteDataChunkBySourceTypeAndSourceIdAndIndexGt(
        String sourceType,
        long sourceId,
        long indexGt
    ) {
        dataChunkRepository.deleteBySourceTypeAndSourceIdAndChunkIndexGt(
            sourceType,
            sourceId,
            indexGt
        );
    }

    public void deleteDataChunkById(long chunkId) {
        dataChunkRepository.delete(chunkId);
    }

    public RagDataChunkModel getDataChunkByIdOrThrow(long chunkId) {
        return entityToModelConverter.toModel(
            getDataChunkEntityByIdOrThrow(chunkId)
        );
    }

    public RagDataChunkModel getDataChunkBySourceTypeAndSourceIdAndIndex(
        String sourceType,
        long sourceId,
        long index
    ) {
        return entityToModelConverter.toModel(
            dataChunkRepository.findBySourceTypeAndSourceIdAndChunkIndex(
                sourceType,
                sourceId,
                index
            )
        );
    }

    public RagDataChunkEmbeddingModel getEmbeddingBySourceTypeAndSourceIdAndIndex(
        String sourceType,
        long sourceId,
        long index
    ) {
        return resultToModelConverter.toModel(
            dataChunkRepository
                .findIdAndContentHashAndEmbeddingBySourceTypeAndSourceIdAndChunkIndex(
                    sourceType,
                    sourceId,
                    index
                )
        );
    }

    public List<RagDataChunkModel> getDataChunksByIds(
        Collection<Long> chunkIds
    ) {
        if (chunkIds.isEmpty()) {
            return Collections.emptyList();
        }
        return newArrayList(
            dataChunkRepository.findListByIds(chunkIds),
            entityToModelConverter::toModel
        );
    }

    public List<RagDataChunkModel> getDataChunksBySourceTypeAndSourceId(
        String sourceType,
        long sourceId
    ) {
        return newArrayList(
            dataChunkRepository.findListBySourceTypeAndSourceId(
                sourceType,
                sourceId
            ),
            entityToModelConverter::toModel
        );
    }

    public List<RagDataChunkModel> getDataChunksBySourceTypeAndSourceIds(
        String sourceType,
        Collection<Long> sourceIds
    ) {
        if (sourceIds == null || sourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return newArrayList(
            dataChunkRepository.findListBySourceTypeAndSourceIdIn(
                sourceType,
                sourceIds
            ),
            entityToModelConverter::toModel
        );
    }

    private RagDataChunk getDataChunkEntityByIdOrThrow(
        long chunkId
    ) {
        RagDataChunk entity = dataChunkRepository
            .findById(chunkId);
        if (entity == null) {
            throw new ResourceNotFoundException("dataChunk");
        }
        return entity;
    }
}
