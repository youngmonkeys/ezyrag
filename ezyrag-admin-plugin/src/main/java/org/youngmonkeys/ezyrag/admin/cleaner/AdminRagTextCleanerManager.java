package org.youngmonkeys.ezyrag.admin.cleaner;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.cleaner.RagTextCleanerManager;

@EzySingleton
public class AdminRagTextCleanerManager
    extends RagTextCleanerManager {

    public AdminRagTextCleanerManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
