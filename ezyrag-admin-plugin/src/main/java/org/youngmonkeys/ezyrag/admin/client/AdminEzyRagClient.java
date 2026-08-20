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

package org.youngmonkeys.ezyrag.admin.client;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.admin.builder.AdminRagKnowledgeDataBuilderManager;
import org.youngmonkeys.ezyrag.admin.chunker.AdminRagDataChunkerManager;
import org.youngmonkeys.ezyrag.admin.cleaner.AdminRagTextCleanerManager;
import org.youngmonkeys.ezyrag.admin.embbeding.AdminRagEmbeddingServiceManager;
import org.youngmonkeys.ezyrag.admin.loader.AdminRagDataLoaderManager;
import org.youngmonkeys.ezyrag.admin.processor.AdminRagQueryProcessorManager;
import org.youngmonkeys.ezyrag.admin.retriever.AdminRagDataRetrieverManager;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.service.AdminRagDataChunkMetaService;
import org.youngmonkeys.ezyrag.admin.service.AdminRagDataChunkService;
import org.youngmonkeys.ezyrag.admin.vd.AdminRagVectorDatabaseServiceManager;
import org.youngmonkeys.ezyrag.client.EzyRagClient;

@EzySingleton
public class AdminEzyRagClient extends EzyRagClient {

    public AdminEzyRagClient(
        AdminRagDataChunkerManager dataChunkerManager,
        AdminRagDataLoaderManager dataLoaderManager,
        AdminRagDataRetrieverManager dataRetrieverManager,
        AdminRagEmbeddingServiceManager embeddingServiceManager,
        AdminRagKnowledgeDataBuilderManager knowledgeDataBuilderManager,
        AdminRagQueryProcessorManager queryProcessorManager,
        AdminRagTextCleanerManager textCleanerManager,
        AdminRagVectorDatabaseServiceManager vectorDatabaseServiceManager,
        AdminRagDataChunkService dataChunkService,
        AdminRagDataChunkMetaService dataChunkMetaService,
        AdminEzyRagSettingService settingService
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
