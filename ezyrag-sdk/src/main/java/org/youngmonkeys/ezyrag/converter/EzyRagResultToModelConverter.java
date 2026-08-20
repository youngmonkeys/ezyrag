package org.youngmonkeys.ezyrag.converter;

import org.youngmonkeys.ezyrag.model.RagDataChunkEmbeddingModel;
import org.youngmonkeys.ezyrag.result.RagDataChunkEmbeddingResult;

public class EzyRagResultToModelConverter {

    public RagDataChunkEmbeddingModel toModel(
        RagDataChunkEmbeddingResult result
    ) {
        if (result == null) {
            return null;
        }
        return RagDataChunkEmbeddingModel.builder()
            .id(result.getId())
            .contentHash(result.getContentHash())
            .embedding(result.getEmbedding())
            .build();
    }
}
