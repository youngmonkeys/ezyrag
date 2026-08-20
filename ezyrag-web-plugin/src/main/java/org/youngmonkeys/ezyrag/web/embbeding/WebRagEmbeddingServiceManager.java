package org.youngmonkeys.ezyrag.web.embbeding;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.embbeding.RagEmbeddingServiceManager;

@EzySingleton
public class WebRagEmbeddingServiceManager
    extends RagEmbeddingServiceManager {

    public WebRagEmbeddingServiceManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
