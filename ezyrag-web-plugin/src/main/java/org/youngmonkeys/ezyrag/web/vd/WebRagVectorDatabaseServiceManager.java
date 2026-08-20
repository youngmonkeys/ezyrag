package org.youngmonkeys.ezyrag.web.vd;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.vd.RagVectorDatabaseServiceManager;

@EzySingleton
public class WebRagVectorDatabaseServiceManager
    extends RagVectorDatabaseServiceManager {

    public WebRagVectorDatabaseServiceManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
