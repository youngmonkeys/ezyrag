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
