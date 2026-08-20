package org.youngmonkeys.ezyrag.converter;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.time.ClockProxy;
import org.youngmonkeys.ezyrag.entity.RagDataChunk;
import org.youngmonkeys.ezyrag.model.RagSaveDataChunkModel;

@AllArgsConstructor
public class EzyRagModelToEntityConverter {

    private final ClockProxy clock;

    public RagDataChunk toEntity(
        RagSaveDataChunkModel model
    ) {
        RagDataChunk entity = new RagDataChunk();
        entity.setSourceType(model.getSourceType());
        entity.setSourceId(model.getSourceId());
        entity.setChunkIndex(model.getChunkIndex());
        mergeToEntity(model, entity);
        entity.setCreatedAt(entity.getUpdatedAt());
        return entity;
    };

    public void mergeToEntity(
        RagSaveDataChunkModel model,
        RagDataChunk entity
    ) {
        entity.setContent(model.getContent());
        entity.setContentHash(model.getContentHash());
        entity.setEmbedding(model.getEmbedding());
        entity.setMetadata(model.getMetadata());
        entity.setUpdatedAt(clock.nowDateTime());
    }
}
