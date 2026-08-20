package org.youngmonkeys.ezyrag.web.chunker;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.chunker.RagDataChunkerManager;

@EzySingleton
public class WebRagDataChunkerManager extends RagDataChunkerManager {

    public WebRagDataChunkerManager(
        EzySingletonFactory singletonFactory
    ) {
        super(singletonFactory);
    }
}
