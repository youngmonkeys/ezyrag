package org.youngmonkeys.ezyrag.constant;

public final class EzyRagConstants {

    public static final String DEFAULT_QDRANT_BASE_URL = "http://localhost:6333";
    public static final String DEFAULT_QDRANT_COLLECTION_NAME = "ezyrag_data_chunks";
    public static final int DEFAULT_EMBEDDING_VECTOR_SIZE = 1536;

    public static final String SETTING_NAME_OPENAI_API_KEY =
        "ezyrag_openai_api_key";
    public static final String SETTING_NAME_QDRANT_BASE_URL =
        "ezyrag_qdrant_base_url";
    public static final String SETTING_NAME_QDRANT_API_KEY =
        "ezyrag_qdrant_api_key";
    public static final String SETTING_NAME_QDRANT_COLLECTION_NAME =
        "ezyrag_qdrant_collection_name";

    private EzyRagConstants() {}
}
