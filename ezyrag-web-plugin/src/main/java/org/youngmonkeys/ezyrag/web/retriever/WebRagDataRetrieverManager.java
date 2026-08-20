package org.youngmonkeys.ezyrag.web.retriever;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.retriever.RagDataRetrieverManager;

@EzySingleton
public class WebRagDataRetrieverManager
    extends RagDataRetrieverManager {

    public WebRagDataRetrieverManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
