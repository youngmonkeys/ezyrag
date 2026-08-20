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
import org.youngmonkeys.ezyrag.constant.EmbeddingServiceName;
import org.youngmonkeys.ezyrag.constant.RagDataChunkerName;
import org.youngmonkeys.ezyrag.constant.RagDataRetrieverName;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;

import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_EXCERPT_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_MAX_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_PARAGRAPH_SEPARATOR;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.DEFAULT_KNOWLEDGE_CHUNK_SENTENCE_BOUNDARY_PATTERN;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_EXCERPT_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_MAX_LENGTH;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_PARAGRAPH_SEPARATOR;
import static org.youngmonkeys.ezyai.constant.EzyAIConstants.SETTING_NAME_KNOWLEDGE_CHUNK_SENTENCE_BOUNDARY_PATTERN;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_DATA_CHUNKER_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_DATA_RETRIEVER_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_EMBEDDING_SERVICE_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_KNOWLEDGE_DATA_BUILDER_NAME;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_OPENAI_API_KEY;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.SETTING_NAME_VECTOR_DATABASE_SERVICE_NAME;

public class EzyRagSettingService {

    private final DefaultSettingService settingService;

    public EzyRagSettingService(
        DefaultSettingService settingService
    ) {
        this.settingService = settingService;
    }

    public String getOpenAiApiKey() {
        return settingService.getPasswordValue(
            SETTING_NAME_OPENAI_API_KEY
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
            SETTING_NAME_KNOWLEDGE_DATA_BUILDER_NAME
        );
    }

    public String getDataChunker() {
        return settingService.getTextValue(
            SETTING_NAME_DATA_CHUNKER_NAME,
            RagDataChunkerName.HIERARCHICAL.toString()
        );
    }

    public String getEmbeddingService() {
        return settingService.getTextValue(
            SETTING_NAME_EMBEDDING_SERVICE_NAME,
            EmbeddingServiceName.OPENAI.toString()
        );
    }

    public String getDataRetriever() {
        return settingService.getTextValue(
            SETTING_NAME_DATA_RETRIEVER_NAME,
            RagDataRetrieverName.DATABASE.toString()
        );
    }

    public String getVectorDatabaseService() {
        return settingService.getTextValue(
            SETTING_NAME_VECTOR_DATABASE_SERVICE_NAME,
            RagVectorDatabaseServiceName.QDRANT.toString()
        );
    }
}
