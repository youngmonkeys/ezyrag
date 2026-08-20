package org.youngmonkeys.ezyrag.converter;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.time.ClockProxy;
import org.youngmonkeys.ezyrag.entity.RagDataChunkEntity;
import org.youngmonkeys.ezyrag.model.SaveRagDataChunkModel;

@AllArgsConstructor
public class EzyRagModelToEntityConverter {

    private final ClockProxy clock;

    public RagDataChunkEntity toEntity(
        SaveRagDataChunkModel model
    ) {
        RagDataChunkEntity entity = new RagDataChunkEntity();
        entity.setSourceType(model.getSourceType());
        entity.setSourceId(model.getSourceId());
        entity.setChunkIndex(model.getChunkIndex());
        mergeToEntity(model, entity);
        entity.setCreatedAt(entity.getUpdatedAt());
        return entity;
    };

    public void mergeToEntity(
        SaveRagDataChunkModel model,
        RagDataChunkEntity entity
    ) {
        entity.setContent(model.getContent());
        entity.setContentHash(model.getContentHash());
        entity.setEmbedding(model.getEmbedding());
        entity.setMetadata(model.getMetadata());
        entity.setUpdatedAt(clock.nowDateTime());
    }
}
