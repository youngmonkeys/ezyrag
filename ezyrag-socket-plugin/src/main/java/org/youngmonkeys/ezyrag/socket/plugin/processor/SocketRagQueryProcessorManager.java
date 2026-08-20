package org.youngmonkeys.ezyrag.socket.plugin.processor;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.processor.RagQueryProcessorManager;

@EzySingleton
public class SocketRagQueryProcessorManager
    extends RagQueryProcessorManager {

    public SocketRagQueryProcessorManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
