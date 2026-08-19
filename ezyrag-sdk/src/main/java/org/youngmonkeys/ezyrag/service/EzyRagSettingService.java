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

package org.youngmonkeys.ezyrag.service;

import org.youngmonkeys.ezyplatform.service.DefaultSettingService;

import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_EXCERPT_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_MAX_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_PARAGRAPH_SEPARATOR;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_SENTENCE_BOUNDARY_PATTERN;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_EXCERPT_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_MAX_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_PARAGRAPH_SEPARATOR;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_SENTENCE_BOUNDARY_PATTERN;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_DATA_CHUNKER;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_EMBEDDING_SERVICE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_QDRANT_BASE_URL;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_QDRANT_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_VECTOR_DATABASE_SERVICE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_DATA_CHUNKER;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_DATA_LOADER;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_DATA_RETRIEVER;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_EMBEDDING_SERVICE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_KNOWLEDGE_DATA_BUILDER;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_OPENAI_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_BASE_URL;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_QDRANT_COLLECTION_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_VECTOR_DATABASE_SERVICE;

public class EzyRagSettingService {

    private final DefaultSettingService settingService;

    public EzyRagSettingService(DefaultSettingService settingService) {
        this.settingService = settingService;
    }

    public String getOpenAiApiKey() {
        return settingService.getPasswordValue(
            SETTING_NAME_OPENAI_API_KEY
        );
    }

    public String getQdrantBaseUrl() {
        return settingService.getTextValue(
            SETTING_NAME_QDRANT_BASE_URL,
            DEFAULT_QDRANT_BASE_URL
        );
    }

    public String getQdrantApiKey() {
        return settingService.getPasswordValue(
            SETTING_NAME_QDRANT_API_KEY
        );
    }

    public String getQdrantCollectionName() {
        return settingService.getTextValue(
            SETTING_NAME_QDRANT_COLLECTION_NAME,
            DEFAULT_QDRANT_COLLECTION_NAME
        );
    }

    public int getKnowledgeChunkMaxLength() {
        return settingService.getIntValue(
            SETTING_NAME_KNOWLEDGE_CHUNK_MAX_LENGTH,
            DEFAULT_KNOWLEDGE_CHUNK_MAX_LENGTH
        );
    }

    public int getKnowledgeChunkExcerptLength() {
        return settingService.getIntValue(
            SETTING_NAME_KNOWLEDGE_CHUNK_EXCERPT_LENGTH,
            DEFAULT_KNOWLEDGE_CHUNK_EXCERPT_LENGTH
        );
    }

    public String getKnowledgeChunkParagraphSeparator() {
        return settingService.getTextValue(
            SETTING_NAME_KNOWLEDGE_CHUNK_PARAGRAPH_SEPARATOR,
            DEFAULT_KNOWLEDGE_CHUNK_PARAGRAPH_SEPARATOR
        );
    }

    public String getKnowledgeChunkSentenceBoundaryPattern() {
        return settingService.getTextValue(
            SETTING_NAME_KNOWLEDGE_CHUNK_SENTENCE_BOUNDARY_PATTERN,
            DEFAULT_KNOWLEDGE_CHUNK_SENTENCE_BOUNDARY_PATTERN
        );
    }

    public String getKnowledgeDataBuilder() {
        return settingService.getTextValue(
            SETTING_NAME_KNOWLEDGE_DATA_BUILDER
        );
    }

    public String getDataChunker() {
        return settingService.getTextValue(
            SETTING_NAME_DATA_CHUNKER,
            DEFAULT_DATA_CHUNKER
        );
    }

    public String getEmbeddingService() {
        return settingService.getTextValue(
            SETTING_NAME_EMBEDDING_SERVICE,
            DEFAULT_EMBEDDING_SERVICE
        );
    }

    public String getDataLoader() {
        return settingService.getTextValue(
            SETTING_NAME_DATA_LOADER
        );
    }

    public String getDataRetriever() {
        return settingService.getTextValue(
            SETTING_NAME_DATA_RETRIEVER
        );
    }

    public String getVectorDatabaseService() {
        return settingService.getTextValue(
            SETTING_NAME_VECTOR_DATABASE_SERVICE,
            DEFAULT_VECTOR_DATABASE_SERVICE
        );
    }
}
