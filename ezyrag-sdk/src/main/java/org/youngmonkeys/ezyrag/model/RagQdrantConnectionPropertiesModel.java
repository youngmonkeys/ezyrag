package org.youngmonkeys.ezyrag.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RagQdrantConnectionPropertiesModel {
    private String baseUrl;
    private String apiKey;
    private String collectionName;
    private int vectorSize;
}
