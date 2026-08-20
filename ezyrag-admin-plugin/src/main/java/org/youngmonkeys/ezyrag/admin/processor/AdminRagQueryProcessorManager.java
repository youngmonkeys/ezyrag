package org.youngmonkeys.ezyrag.admin.processor;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.processor.RagQueryProcessorManager;

@EzySingleton
public class AdminRagQueryProcessorManager
    extends RagQueryProcessorManager {

    public AdminRagQueryProcessorManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
