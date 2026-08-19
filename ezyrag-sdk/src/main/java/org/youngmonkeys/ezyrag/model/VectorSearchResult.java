package org.youngmonkeys.ezyrag.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class VectorSearchResult {
    private final String id;
    private final float score;
    private final Map<String, Object> payload;
}
