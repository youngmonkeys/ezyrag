package org.youngmonkeys.ezyrag.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RagQueryModel {
    private final String query;
    private final int processedTime;

    public RagQueryModel toProcessedQuery(
        String processedQuery
    ) {
        return RagQueryModel
            .builder()
            .query(processedQuery)
            .processedTime(processedTime + 1)
            .build();
    }
}
