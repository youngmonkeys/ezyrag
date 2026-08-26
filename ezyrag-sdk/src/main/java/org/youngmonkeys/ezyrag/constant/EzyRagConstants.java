/*
 * Copyright 2026 youngmonkeys.org
 * 
 * Licensed under the ezyplatform, Version 1.0.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://youngmonkeys.org/licenses/ezyplatform-1.0.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.youngmonkeys.ezyrag.constant;

public final class EzyRagConstants {

    public static final String DEFAULT_OPENAI_EMBEDDING_MODEL =
        "text-embedding-3-small";
    public static final int DEFAULT_VECTOR_SIZE = 384;
    public static final String DEFAULT_DISTANCE = "Cosine";

    public static final String SETTING_NAME_OPENAI_API_KEY =
        "ezyrag_openai_api_key";
    public static final String SETTING_NAME_OPENAI_EMBEDDING_MODEL =
        "ezyrag_openai_embedding_model";
    public static final String SETTING_NAME_QDRANT_CONNECTION_PROPERTIES =
        "ezyrag_qdrant_connection_properties";
    public static final String SETTING_NAME_QDRANT_CONNECTION_API_KEY =
        "ezyrag_qdrant_connection_api_key";
    public static final String SETTING_NAME_EZY_VECTOR_CONNECTION_API_KEY =
        "ezyrag_ezy_vector_connection_api_key";
    public static final String SETTING_NAME_EZY_VECTOR_CONNECTION_PROPERTIES =
        "ezyrag_ezy_vector_connection_properties";
    public static final String SETTING_NAME_KNOWLEDGE_DATA_BUILDER_NAME =
        "ezyrag_knowledge_data_builder_name";
    public static final String SETTING_NAME_DATA_CHUNKER_NAME =
        "ezyrag_knowledge_data_chunker_name";
    public static final String SETTING_NAME_EMBEDDING_SERVICE_NAME =
        "ezyrag_embedding_service_name";
    public static final String SETTING_NAME_DATA_RETRIEVER_NAME =
        "ezyrag_data_retriever_name";
    public static final String SETTING_NAME_VECTOR_DATABASE_SERVICE_NAME =
        "ezyrag_vector_database_service_name";

    public static final String META_KEY_TITLE = "title";
    public static final String META_KEY_SLUG = "slug";
    public static final String META_KEY_EXCERPT = "excerpt";
    public static final String META_KEY_DATA_URL = "dataUrl";
    public static final String META_KEY_PRODUCT_CODE = "productCode";
    public static final String META_KEY_PRICE = "price";
    public static final String META_KEY_CURRENCY_ISO_CODE = "currencyIsoCode";

    private EzyRagConstants() {}

    public static String settingNameDefaultCollectionNameOfVectorDbService(
        String vectorDbServiceName
    ) {
        return "ezyrag_default_collection_name_of_vector_db_service_" +
            vectorDbServiceName;
    }
}
