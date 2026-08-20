package org.youngmonkeys.ezyrag.converter;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.time.ClockProxy;
import org.youngmonkeys.ezyrag.entity.RagDataChunkEntity;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;

@AllArgsConstructor
public class EzyRagEntityToModelConverter {

    private final ClockProxy clock;

    public RagDataChunkModel toModel(
        RagDataChunkEntity entity
    ) {
        if (entity == null) {
            return null;
        }
        return RagDataChunkModel.builder()
            .createdAt(clock.toTimestamp(entity.getCreatedAt()))
            .build();
    }
}
