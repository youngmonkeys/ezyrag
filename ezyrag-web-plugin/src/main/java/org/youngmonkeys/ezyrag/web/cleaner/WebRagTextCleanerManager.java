package org.youngmonkeys.ezyrag.web.cleaner;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.cleaner.RagTextCleanerManager;

@EzySingleton
public class WebRagTextCleanerManager
    extends RagTextCleanerManager {

    public WebRagTextCleanerManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
