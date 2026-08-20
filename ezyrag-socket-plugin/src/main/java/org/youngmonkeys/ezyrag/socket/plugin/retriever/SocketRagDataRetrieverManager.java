package org.youngmonkeys.ezyrag.socket.plugin.retriever;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.retriever.RagDataRetrieverManager;

@EzySingleton
public class SocketRagDataRetrieverManager
    extends RagDataRetrieverManager {

    public SocketRagDataRetrieverManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
