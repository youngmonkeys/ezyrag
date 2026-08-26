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
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;

@AllArgsConstructor
public class EzyRagEntityToModelConverter {

    private final ClockProxy clock;

    public RagVectorCollectionModel toModel(
        RagVectorCollection entity
    ) {
        if (entity == null) {
            return null;
        }
        return RagVectorCollectionModel.builder()
            .id(entity.getId())
            .name(entity.getName())
            .displayName(entity.getDisplayName())
            .vectorSize(entity.getVectorSize())
            .distance(entity.getDistance())
            .status(entity.getStatus())
            .createdAt(clock.toTimestamp(entity.getCreatedAt()))
            .updatedAt(clock.toTimestamp(entity.getUpdatedAt()))
            .build();
    }

    public RagDataChunkModel toModel(
        RagDataChunk entity
    ) {
        if (entity == null) {
            return null;
        }
        return RagDataChunkModel.builder()
            .id(entity.getId())
            .sourceType(entity.getSourceType())
            .sourceId(entity.getSourceId())
            .chunkIndex(entity.getChunkIndex())
            .content(entity.getContent())
            .contentHash(entity.getContentHash())
            .embedding(entity.getEmbedding())
            .createdAt(clock.toTimestamp(entity.getCreatedAt()))
            .updatedAt(clock.toTimestamp(entity.getUpdatedAt()))
            .build();
    }
}
