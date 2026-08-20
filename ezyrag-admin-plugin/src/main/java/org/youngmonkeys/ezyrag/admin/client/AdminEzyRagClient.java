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
