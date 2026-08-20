package org.youngmonkeys.ezyrag.admin.retriever;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.retriever.RagDataRetrieverManager;

@EzySingleton
public class AdminRagDataRetrieverManager
    extends RagDataRetrieverManager {

    public AdminRagDataRetrieverManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
