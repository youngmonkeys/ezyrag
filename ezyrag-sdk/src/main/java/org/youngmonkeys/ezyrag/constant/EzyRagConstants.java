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
