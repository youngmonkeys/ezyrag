package org.youngmonkeys.ezyrag.socket.plugin.loader;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.loader.RagDataLoaderManager;

@EzySingleton
public class SocketRagDataLoaderManager extends RagDataLoaderManager {

    public SocketRagDataLoaderManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
