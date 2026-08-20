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
import org.youngmonkeys.ezyrag.vd.VectorDatabaseService;
import org.youngmonkeys.ezyrag.vd.VectorDatabaseServiceManager;

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
    private final VectorDatabaseServiceManager vectorDatabaseServiceManager;
    private final RagDataChunkService dataChunkService;
    private final RagDataChunkMetaService dataChunkMetaService;
    private final EzyRagSettingService settingService;

    public void storeData(
        RagDataSourceModel dataSource
    ) throws Exception {
        String sourceType = dataSource.getSourceType();
        RagDataLoader dataLoader = dataLoaderManager
            .getDataLoaderBySourceType(sourceType);
        if (dataLoader == null) {
            throw new IllegalArgumentException(
                "There is no DataLoader mapping to source type: " +
                    sourceType
            );
        }
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
        String vectorDatabaseServiceName = settingService
            .getVectorDatabaseService();
        if (isBlank(vectorDatabaseServiceName)) {
            throw new IllegalStateException(
                "Vector database service has not been set up"
            );
        }
        VectorDatabaseService vectorDatabaseService =
            vectorDatabaseServiceManager
                .getEmbeddingServiceByName(
                    vectorDatabaseServiceName
                );
        if (vectorDatabaseService == null) {
            throw new IllegalStateException(
                "There is no vector database service: " +
                    vectorDatabaseServiceName
            );
        }
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
        Iterator<RagInputData> iterator = dataLoader
            .load(dataSource);
        long sourceId = dataSource.getSourceId();
        int chunkIndex = 0;
        Map<String, Object> dataSourceMetadata = dataSource
            .toMetadata();
        while (iterator.hasNext()) {
            RagInputData inputData = iterator.next();
            String text = textCleanerManager.cleanText(
                (String) inputData.getData()
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
                    embedding = embeddingService.embed(content);
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
                    settingService.getQdrantCollectionName(),
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

    public List<KnowledgeData> getKnowledgeDataList(
        String query,
        int limit
    ) throws Exception {
        String processedQuery = queryProcessorManager
            .processQuery(query);
        RagEmbeddingService embeddingService = embeddingServiceManager
            .getEmbeddingServiceByName(settingService.getEmbeddingService());
        float[] vector = embeddingService.embed(processedQuery);
        VectorDatabaseService vectorDatabaseService =
            vectorDatabaseServiceManager
                .getEmbeddingServiceByName(
                    settingService.getVectorDatabaseService()
                );
        List<RagVectorSearchResultModel> result = vectorDatabaseService
            .search(
                settingService.getQdrantCollectionName(),
                vector,
                limit
            );
        RagDataRetriever retriever = dataRetrieverManager
            .getDataRetrieverByName(settingService.getDataRetriever());
        List<RagDocumentModel> documents = retriever
            .retrieve(result);
        RagKnowledgeDataBuilder knowledgeDataBuilder =
            knowledgeDataBuilderManager
                .getKnowledgeDataBuilderByName(
                    settingService.getKnowledgeDataBuilder()
                );
        return knowledgeDataBuilder.build(documents);
    }
}
