package org.youngmonkeys.ezyrag.socket.plugin.embbeding;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.embbeding.RagEmbeddingServiceManager;

@EzySingleton
public class SocketRagEmbeddingServiceManager
    extends RagEmbeddingServiceManager {

    public SocketRagEmbeddingServiceManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
