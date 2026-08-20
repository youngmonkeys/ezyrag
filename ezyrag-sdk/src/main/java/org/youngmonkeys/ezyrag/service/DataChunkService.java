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
import org.youngmonkeys.ezyrag.converter.EzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.converter.EzyRagModelToEntityConverter;
import org.youngmonkeys.ezyrag.converter.EzyRagResultToModelConverter;
import org.youngmonkeys.ezyrag.entity.RagDataChunkEntity;
import org.youngmonkeys.ezyrag.model.RagDataChunkEmbeddingModel;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.SaveRagDataChunkModel;
import org.youngmonkeys.ezyrag.repo.DataChunkRepository;

@AllArgsConstructor
public class DataChunkService {

    private final DataChunkRepository dataChunkRepository;
    private final EzyRagEntityToModelConverter entityToModelConverter;
    private final EzyRagModelToEntityConverter modelToEntityConverter;
    private final EzyRagResultToModelConverter resultToModelConverter;

    public long addDataChunk(
        SaveRagDataChunkModel model
    ) {
        RagDataChunkEntity entity = modelToEntityConverter
            .toEntity(model);
        dataChunkRepository.save(entity);
        return entity.getId();
    }

    public void updateEmbeddingById(
        long chunkId,
        float[] embedding
    ) {
        dataChunkRepository.updateEmbeddingById(
            chunkId,
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
}
