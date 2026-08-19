package org.youngmonkeys.ezyrag.embbeding;

public interface RagEmbeddingService {

    String getServiceName();

    float[] embed(String apiKey, String text) throws Exception;
}
