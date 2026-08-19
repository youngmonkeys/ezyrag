package org.youngmonkeys.ezyrag.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class VectorPointModel {
    private final String id;
    private final float[] vector;
    private final Map<String, Object> payload;
}
