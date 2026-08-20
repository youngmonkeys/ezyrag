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

package org.youngmonkeys.ezyrag.client;

import com.tvd12.ezyfox.security.EzySHA256;
import com.tvd12.ezyfox.util.EzyMapBuilder;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyai.knowledge.KnowledgeData;
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyrag.builder.RagKnowledgeDataBuilder;
import org.youngmonkeys.ezyrag.builder.RagKnowledgeDataBuilderManager;
import org.youngmonkeys.ezyrag.chunker.RagDataChunker;
import org.youngmonkeys.ezyrag.chunker.RagDataChunkerManager;
import org.youngmonkeys.ezyrag.cleaner.RagTextCleanerManager;
import org.youngmonkeys.ezyrag.embbeding.RagEmbeddingService;
import org.youngmonkeys.ezyrag.embbeding.RagEmbeddingServiceManager;
import org.youngmonkeys.ezyrag.loader.RagDataLoader;
import org.youngmonkeys.ezyrag.loader.RagDataLoaderManager;
import org.youngmonkeys.ezyrag.model.RagChunkedResultModel;
import org.youngmonkeys.ezyrag.model.RagDataChunkEmbeddingModel;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagDocumentModel;
import org.youngmonkeys.ezyrag.model.RagEmbeddingData;
import org.youngmonkeys.ezyrag.model.RagInputData;
import org.youngmonkeys.ezyrag.model.RagSaveDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagVectorPointModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.processor.RagQueryProcessorManager;
import org.youngmonkeys.ezyrag.retriever.RagDataRetriever;
import org.youngmonkeys.ezyrag.retriever.RagDataRetrieverManager;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;
import org.youngmonkeys.ezyrag.service.RagDataChunkMetaService;
import org.youngmonkeys.ezyrag.service.RagDataChunkService;
import org.youngmonkeys.ezyrag.vd.RagVectorDatabaseService;
import org.youngmonkeys.ezyrag.vd.RagVectorDatabaseServiceManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

@AllArgsConstructor
public class EzyRagClient {

    private final RagDataChunkerManager dataChunkerManager;
    private final RagDataLoaderManager dataLoaderManager;
    private final RagDataRetrieverManager dataRetrieverManager;
    private final RagEmbeddingServiceManager embeddingServiceManager;
    private final RagKnowledgeDataBuilderManager knowledgeDataBuilderManager;
    private final RagQueryProcessorManager queryProcessorManager;
    private final RagTextCleanerManager textCleanerManager;
    private final RagVectorDatabaseServiceManager vectorDatabaseServiceManager;
    private final RagDataChunkService dataChunkService;
    private final RagDataChunkMetaService dataChunkMetaService;
    private final EzyRagSettingService settingService;

    @SuppressWarnings("MethodLength")
    public void chunkData(
        RagDataSourceModel dataSource
    ) throws Exception {
        String sourceType = dataSource.getSourceType();
        RagDataLoader dataLoader = dataLoaderManager
            .getDataLoaderBySourceTypeOrThrow(sourceType);
        RagEmbeddingService embeddingService = getEmbeddingService();
        RagVectorDatabaseService vectorDatabaseService =
            getVectorDatabaseService();
        int vectorSize = vectorDatabaseService.getVectorSize();
        RagDataChunker chunker = getDataChunker();
        Iterator<RagInputData> iterator = dataLoader
            .load(dataSource);
        long sourceId = dataSource.getSourceId();
        int chunkIndex = 0;
        Map<String, Object> dataSourceMetadata = dataSource
            .toMetadata();
        while (iterator.hasNext()) {
            RagInputData inputData = iterator.next();
            String text = textCleanerManager.cleanText(
                inputData.getText()
            );
            List<RagChunkedResultModel> chunkedResults =
                chunker.chunk(text);
            for (RagChunkedResultModel chunkedResult : chunkedResults) {
                RagDataChunkEmbeddingModel chunkEmbedding =
                    dataChunkService
                        .getEmbeddingBySourceTypeAndSourceIdAndIndex(
                            sourceType,
                            sourceId,
                            chunkIndex + 1
                        );
                String content = chunkedResult.getContent();
                String contentHash = EzySHA256.cryptUtf(content);
                Map<String, Object> metadata = EzyMapBuilder
                    .mapBuilder()
                    .putAll(dataSourceMetadata)
                    .putAll(chunkedResult.getMetadata())
                    .toMap();
                long chunkId;
                String contentHashInDb = null;
                float[] embedding = null;
                RagSaveDataChunkModel saveDataChunk = RagSaveDataChunkModel
                    .builder()
                    .sourceType(sourceType)
                    .sourceId(sourceId)
                    .chunkIndex(chunkIndex + 1)
                    .content(content)
                    .contentHash(contentHash)
                    .build();
                if (chunkEmbedding == null) {
                    chunkId = dataChunkService.addDataChunk(saveDataChunk);
                } else {
                    chunkId = chunkEmbedding.getId();
                    contentHashInDb = chunkEmbedding.getContentHash();
                    embedding = chunkEmbedding.getEmbedding();
                    dataChunkService.updateDataChunk(
                        chunkId,
                        saveDataChunk
                    );
                }
                dataChunkMetaService.saveDataChunkMeta(
                    chunkId,
                    metadata
                );
                boolean sameHash = contentHash.equals(contentHashInDb);
                if (embedding == null || !sameHash) {
                    embedding = embeddingService.embed(
                        RagEmbeddingData.builder()
                            .data(content)
                            .dataType(CommonContentType.TEXT.toString())
                            .build(),
                        vectorSize
                    );
                    dataChunkService.updateEmbeddingById(
                        chunkId,
                        embedding
                    );
                }
                Map<String, Object> payload = EzyMapBuilder
                    .mapBuilder()
                    .put("sourceType", sourceType)
                    .put("sourceId", sourceId)
                    .put("chunkIndex", chunkIndex + 1)
                    .toMap();
                vectorDatabaseService.upsert(
                    Collections.singletonList(
                        RagVectorPointModel.builder()
                            .id(chunkId)
                            .vector(embedding)
                            .payload(payload)
                            .build()
                    )
                );
                ++chunkIndex;
            }
        }
        dataChunkService.deleteDataChunkBySourceTypeAndSourceIdAndIndexGt(
            sourceType,
            sourceId,
            chunkIndex
        );
    }

    public List<RagVectorSearchResultModel> searchDataList(
        String query,
        int limit
    ) throws Exception {
        RagEmbeddingService embeddingService = getEmbeddingService();
        RagVectorDatabaseService vectorDatabaseService =
            getVectorDatabaseService();
        String processedQuery = queryProcessorManager
            .processQuery(query);
        float[] vector = embeddingService.embed(
            RagEmbeddingData.builder()
                .data(processedQuery)
                .dataType(CommonContentType.TEXT.toString())
                .build(),
            vectorDatabaseService.getVectorSize()
        );
        return vectorDatabaseService.search(vector, limit);
    }

    public List<KnowledgeData> getKnowledgeDataList(
        String query,
        int limit
    ) throws Exception {
        RagDataRetriever retriever = getDataRetriever();
        RagKnowledgeDataBuilder knowledgeDataBuilder =
            getKnowledgeDataBuilder();
        List<RagVectorSearchResultModel> result = searchDataList(
            query,
            limit
        );
        List<RagDocumentModel> documents = retriever
            .retrieve(result);
        return knowledgeDataBuilder.build(documents);
    }

    private RagKnowledgeDataBuilder getKnowledgeDataBuilder() {
        String knowledgeDataBuilderName = settingService
            .getKnowledgeDataBuilder();
        if (isBlank(knowledgeDataBuilderName)) {
            throw new IllegalStateException(
                "Knowledge data builder has not been set up"
            );
        }
        RagKnowledgeDataBuilder knowledgeDataBuilder =
            knowledgeDataBuilderManager
                .getKnowledgeDataBuilderByName(
                    knowledgeDataBuilderName
                );
        if (knowledgeDataBuilder == null) {
            throw new IllegalStateException(
                "There is no knowledge data builder: " +
                    knowledgeDataBuilderName
            );
        }
        return knowledgeDataBuilder;
    }

    private RagDataRetriever getDataRetriever() {
        String dataRetrieverName = settingService
            .getDataRetriever();
        if (isBlank(dataRetrieverName)) {
            throw new IllegalStateException(
                "Data retriever has not been set up"
            );
        }
        RagDataRetriever dataRetriever = dataRetrieverManager
            .getDataRetrieverByName(dataRetrieverName);
        if (dataRetriever == null) {
            throw new IllegalStateException(
                "There is no data retriever: " +
                    dataRetrieverName
            );
        }
        return dataRetriever;
    }

    private RagDataChunker getDataChunker() {
        String dataChunkerName = settingService
            .getDataChunker();

        if (isBlank(dataChunkerName)) {
            throw new IllegalStateException(
                "Data chunker has not been set up"
            );
        }
        RagDataChunker chunker = dataChunkerManager
            .getDataChunkerByName(dataChunkerName);
        if (chunker == null) {
            throw new IllegalStateException(
                "There is no chunker: " +
                    dataChunkerName
            );
        }
        return chunker;
    }

    private RagEmbeddingService getEmbeddingService() {
        String embeddingServiceName = settingService
            .getEmbeddingService();
        if (isBlank(embeddingServiceName)) {
            throw new IllegalStateException(
                "Embedding service has not been set up"
            );
        }
        RagEmbeddingService embeddingService =
            embeddingServiceManager.getEmbeddingServiceByName(
                embeddingServiceName
            );
        if (embeddingService == null) {
            throw new IllegalStateException(
                "There is no embedding service: " +
                    embeddingServiceName
            );
        }
        return embeddingService;
    }

    private RagVectorDatabaseService getVectorDatabaseService() {
        String vectorDatabaseServiceName = settingService
            .getVectorDatabaseService();
        if (isBlank(vectorDatabaseServiceName)) {
            throw new IllegalStateException(
                "Vector database service has not been set up"
            );
        }
        RagVectorDatabaseService vectorDatabaseService =
            vectorDatabaseServiceManager
                .getVectorDatabaseServiceByName(
                    vectorDatabaseServiceName
                );
        if (vectorDatabaseService == null) {
            throw new IllegalStateException(
                "There is no vector database service: " +
                    vectorDatabaseServiceName
            );
        }
        return vectorDatabaseService;
    }
}
