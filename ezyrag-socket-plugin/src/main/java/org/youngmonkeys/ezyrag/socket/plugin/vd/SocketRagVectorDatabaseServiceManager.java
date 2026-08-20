package org.youngmonkeys.ezyrag.socket.plugin.vd;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.vd.RagVectorDatabaseServiceManager;

@EzySingleton
public class SocketRagVectorDatabaseServiceManager
    extends RagVectorDatabaseServiceManager {

    public SocketRagVectorDatabaseServiceManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
