package org.youngmonkeys.ezyrag.socket.plugin.cleaner;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.cleaner.RagTextCleanerManager;

@EzySingleton
public class SocketRagTextCleanerManager
    extends RagTextCleanerManager {

    public SocketRagTextCleanerManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
