package org.youngmonkeys.ezyrag.converter;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.time.ClockProxy;
import org.youngmonkeys.ezyrag.entity.DataChunkEntity;
import org.youngmonkeys.ezyrag.model.SaveDataChunkModel;

@AllArgsConstructor
public class ModelToEntityConverter {

    private final ClockProxy clock;

    public DataChunkEntity toEntity(
        SaveDataChunkModel model
    ) {
        DataChunkEntity entity = new DataChunkEntity();
        mergeToEntity(model, entity);
        entity.setCreatedAt(entity.getUpdatedAt());
        return entity;
    };

    public void mergeToEntity(
        SaveDataChunkModel model,
        DataChunkEntity entity
    ) {
        entity.setContent(model.getContent());
        entity.setEmbedding(model.getEmbedding());
        entity.setMetadata(model.getMetadata());
        entity.setUpdatedAt(clock.nowDateTime());
    }
}
