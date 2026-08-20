package org.youngmonkeys.ezyrag.socket.plugin.client;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.client.EzyRagClient;
import org.youngmonkeys.ezyrag.socket.plugin.builder.SocketRagKnowledgeDataBuilderManager;
import org.youngmonkeys.ezyrag.socket.plugin.chunker.SocketRagDataChunkerManager;
import org.youngmonkeys.ezyrag.socket.plugin.cleaner.SocketRagTextCleanerManager;
import org.youngmonkeys.ezyrag.socket.plugin.embbeding.SocketRagEmbeddingServiceManager;
import org.youngmonkeys.ezyrag.socket.plugin.loader.SocketRagDataLoaderManager;
import org.youngmonkeys.ezyrag.socket.plugin.processor.SocketRagQueryProcessorManager;
import org.youngmonkeys.ezyrag.socket.plugin.retriever.SocketRagDataRetrieverManager;
import org.youngmonkeys.ezyrag.socket.plugin.service.SocketEzyRagSettingService;
import org.youngmonkeys.ezyrag.socket.plugin.service.SocketRagDataChunkMetaService;
import org.youngmonkeys.ezyrag.socket.plugin.service.SocketRagDataChunkService;
import org.youngmonkeys.ezyrag.socket.plugin.vd.SocketRagVectorDatabaseServiceManager;

@EzySingleton
public class SocketEzyRagClient extends EzyRagClient {

    public SocketEzyRagClient(
        SocketRagDataChunkerManager dataChunkerManager,
        SocketRagDataLoaderManager dataLoaderManager,
        SocketRagDataRetrieverManager dataRetrieverManager,
        SocketRagEmbeddingServiceManager embeddingServiceManager,
        SocketRagKnowledgeDataBuilderManager knowledgeDataBuilderManager,
        SocketRagQueryProcessorManager queryProcessorManager,
        SocketRagTextCleanerManager textCleanerManager,
        SocketRagVectorDatabaseServiceManager vectorDatabaseServiceManager,
        SocketRagDataChunkService dataChunkService,
        SocketRagDataChunkMetaService dataChunkMetaService,
        SocketEzyRagSettingService settingService
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
