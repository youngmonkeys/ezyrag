package org.youngmonkeys.ezyrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagQdrantConnectionPropertiesModel {
    private String baseUrl;
    private String apiKey;
    private String collectionName;
    private int vectorSize;
}
