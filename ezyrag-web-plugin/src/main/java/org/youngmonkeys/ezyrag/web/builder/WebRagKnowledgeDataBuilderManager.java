package org.youngmonkeys.ezyrag.web.builder;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.builder.RagKnowledgeDataBuilderManager;

@EzySingleton
public class WebRagKnowledgeDataBuilderManager
    extends RagKnowledgeDataBuilderManager {

    public WebRagKnowledgeDataBuilderManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
