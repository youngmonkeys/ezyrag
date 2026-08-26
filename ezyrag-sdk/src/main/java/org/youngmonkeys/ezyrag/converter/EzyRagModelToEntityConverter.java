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

package org.youngmonkeys.ezyrag.converter;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.time.ClockProxy;
import org.youngmonkeys.ezyrag.entity.RagDataChunk;
import org.youngmonkeys.ezyrag.entity.RagVectorCollection;
import org.youngmonkeys.ezyrag.entity.RagVectorCollectionStatus;
import org.youngmonkeys.ezyrag.model.RagSaveDataChunkModel;
import org.youngmonkeys.ezyrag.model.SaveRagVectorCollectionModel;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

@AllArgsConstructor
public class EzyRagModelToEntityConverter {

    private final ClockProxy clock;

    public RagDataChunk toRagDataChunkEntity(
        RagSaveDataChunkModel model
    ) {
        RagDataChunk entity = new RagDataChunk();
        entity.setSourceType(model.getSourceType());
        entity.setSourceId(model.getSourceId());
        entity.setChunkIndex(model.getChunkIndex());
        entity.setCollectionId(model.getCollectionId());
        mergeToRagDataChunkEntity(model, entity);
        entity.setCreatedAt(entity.getUpdatedAt());
        return entity;
    }

    public void mergeToRagDataChunkEntity(
        RagSaveDataChunkModel model,
        RagDataChunk entity
    ) {
        entity.setContent(model.getContent());
        entity.setContentHash(model.getContentHash());
        entity.setUpdatedAt(clock.nowDateTime());
    }

    public RagVectorCollection toVectorCollectionEntity(
        SaveRagVectorCollectionModel model
    ) {
        RagVectorCollection entity = new RagVectorCollection();
        mergeToVectorCollectionEntity(model, entity);
        entity.setCreatedAt(entity.getUpdatedAt());
        return entity;
    }

    public void mergeToVectorCollectionEntity(
        SaveRagVectorCollectionModel model,
        RagVectorCollection entity
    ) {
        entity.setName(model.getName());
        entity.setDisplayName(model.getDisplayName());
        entity.setVectorSize(model.getVectorSize());
        entity.setDistance(model.getDistance());
        String status = model.getStatus();
        if (isBlank(status)) {
            status = RagVectorCollectionStatus.ACTIVATED.toString();
        }
        entity.setStatus(status);
        entity.setUpdatedAt(clock.nowDateTime());
    }

    public void mergeUpdatedAtToCollectionEntity(
        RagVectorCollection entity
    ) {
        entity.setUpdatedAt(clock.nowDateTime());
    }
}
