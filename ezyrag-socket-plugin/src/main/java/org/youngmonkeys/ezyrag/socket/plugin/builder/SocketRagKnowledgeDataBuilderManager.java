package org.youngmonkeys.ezyrag.socket.plugin.builder;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.builder.RagKnowledgeDataBuilderManager;

@EzySingleton
public class SocketRagKnowledgeDataBuilderManager
    extends RagKnowledgeDataBuilderManager {

    public SocketRagKnowledgeDataBuilderManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
