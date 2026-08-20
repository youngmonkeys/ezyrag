package org.youngmonkeys.ezyrag.web.loader;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.loader.RagDataLoaderManager;

@EzySingleton
public class WebRagDataLoaderManager extends RagDataLoaderManager {

    public WebRagDataLoaderManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
