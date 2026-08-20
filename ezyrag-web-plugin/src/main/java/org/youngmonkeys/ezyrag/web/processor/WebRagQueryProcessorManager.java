package org.youngmonkeys.ezyrag.web.processor;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.processor.RagQueryProcessorManager;

@EzySingleton
public class WebRagQueryProcessorManager
    extends RagQueryProcessorManager {

    public WebRagQueryProcessorManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
