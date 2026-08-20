package org.youngmonkeys.ezyrag.socket.plugin.chunker;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.chunker.RagDataChunkerManager;

@EzySingleton
public class SocketRagDataChunkerManager extends RagDataChunkerManager {

    public SocketRagDataChunkerManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
