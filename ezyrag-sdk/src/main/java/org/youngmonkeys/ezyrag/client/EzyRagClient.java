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
import org.youngmonkeys.ezyrag.model.DataChunkModel;
import org.youngmonkeys.ezyrag.model.DataSourceModel;
import org.youngmonkeys.ezyrag.model.RagDocumentModel;
import org.youngmonkeys.ezyrag.model.RagInputData;
import org.youngmonkeys.ezyrag.model.VectorSearchResultModel;
import org.youngmonkeys.ezyrag.processor.RagQueryProcessorManager;
import org.youngmonkeys.ezyrag.retriever.RagDataRetriever;
import org.youngmonkeys.ezyrag.retriever.RagDataRetrieverManager;
import org.youngmonkeys.ezyrag.service.DataChunkService;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;
import org.youngmonkeys.ezyrag.vd.VectorDatabaseService;
import org.youngmonkeys.ezyrag.vd.VectorDatabaseServiceManager;

import java.util.Iterator;
import java.util.List;

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
    private final DataChunkService dataChunkService;
    private final EzyRagSettingService settingService;

    public void storeData(
        DataSourceModel dataSource
    ) throws Exception {
        RagDataLoader dataLoader = dataLoaderManager
            .getDataLoaderBySourceType(dataSource.getSourceType());
        Iterator<RagInputData> iterator = dataLoader
            .load(dataSource);
        RagEmbeddingService embeddingService = embeddingServiceManager
            .getEmbeddingServiceByName(settingService.getEmbeddingService());
        VectorDatabaseService vectorDatabaseService =
            vectorDatabaseServiceManager
                .getEmbeddingServiceByName(
                    settingService.getVectorDatabaseService()
                );
        while (iterator.hasNext()) {
            RagInputData inputData = iterator.next();
            String text = textCleanerManager.cleanText(
                (String) inputData.getData()
            );
            RagDataChunker chunker = dataChunkerManager
                .getDataChunkerByName(settingService.getDataChunker());
            List<DataChunkModel> chunks = chunker.chunk(text);
            for (DataChunkModel chunk : chunks) {
                float[] vector = embeddingService
                    .embed(chunk.getContent());
                dataChunkService.save(chunk);
                vectorDatabaseService.upsert(
                    settingService.getQdrantCollectionName(),

                );
            }
        }
    }

    public List<KnowledgeData> getKnowledgeDataList(
        String query,
        int limit
    ) {
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
        List<VectorSearchResultModel> result = vectorDatabaseService.search(vector, limit);
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
