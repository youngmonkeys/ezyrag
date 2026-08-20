package org.youngmonkeys.ezyrag.converter;

import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagDocumentModel;

import java.util.Map;

public class EzyRagModelToModelConverter {

    public RagDocumentModel toDocument(
        RagDataChunkModel chunk,
        Map<String, String> metadata
    ) {
        return RagDocumentModel.builder()
            .sourceType(chunk.getSourceType())
            .sourceId(chunk.getSourceId())
            .content(chunk.getContent())
            .metadata(metadata)
            .build();
    }
}
