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

package org.youngmonkeys.ezyrag.web.client;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.client.EzyRagClient;
import org.youngmonkeys.ezyrag.web.builder.WebRagKnowledgeDataBuilderManager;
import org.youngmonkeys.ezyrag.web.chunker.WebRagDataChunkerManager;
import org.youngmonkeys.ezyrag.web.cleaner.WebRagTextCleanerManager;
import org.youngmonkeys.ezyrag.web.embbeding.WebRagEmbeddingServiceManager;
import org.youngmonkeys.ezyrag.web.loader.WebRagDataLoaderManager;
import org.youngmonkeys.ezyrag.web.processor.WebRagQueryProcessorManager;
import org.youngmonkeys.ezyrag.web.retriever.WebRagDataRetrieverManager;
import org.youngmonkeys.ezyrag.web.service.WebEzyRagSettingService;
import org.youngmonkeys.ezyrag.web.service.WebRagDataChunkMetaService;
import org.youngmonkeys.ezyrag.web.service.WebRagDataChunkService;
import org.youngmonkeys.ezyrag.web.vd.WebRagVectorDatabaseServiceManager;

@EzySingleton
public class WebEzyRagClient extends EzyRagClient {

    public WebEzyRagClient(
        WebRagDataChunkerManager dataChunkerManager,
        WebRagDataLoaderManager dataLoaderManager,
        WebRagDataRetrieverManager dataRetrieverManager,
        WebRagEmbeddingServiceManager embeddingServiceManager,
        WebRagKnowledgeDataBuilderManager knowledgeDataBuilderManager,
        WebRagQueryProcessorManager queryProcessorManager,
        WebRagTextCleanerManager textCleanerManager,
        WebRagVectorDatabaseServiceManager vectorDatabaseServiceManager,
        WebRagDataChunkService dataChunkService,
        WebRagDataChunkMetaService dataChunkMetaService,
        WebEzyRagSettingService settingService
    ) {
        super(
            dataChunkerManager,
            dataLoaderManager,
            dataRetrieverManager,
            embeddingServiceManager,
            knowledgeDataBuilderManager,
            queryProcessorManager,
            textCleanerManager,
            vectorDatabaseServiceManager,
            dataChunkService,
            dataChunkMetaService,
            settingService
        );
    }
}
