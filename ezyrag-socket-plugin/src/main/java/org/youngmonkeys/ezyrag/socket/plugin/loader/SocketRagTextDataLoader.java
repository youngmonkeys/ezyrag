package org.youngmonkeys.ezyrag.socket.plugin.loader;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.loader.RagDataLoader;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagInputData;

import java.util.Iterator;
import java.util.stream.Stream;

@EzySingleton
public class SocketRagTextDataLoader
    implements RagDataLoader {

    @Override
    public Iterator<RagInputData> load(
        RagDataSourceModel model
    ) {
        return Stream
            .of(model)
            .map(it ->
                RagInputData.builder()
                    .text(it.getContent())
                    .build()
            )
            .iterator();
    }

    @Override
    public String getDataSourceType() {
        return "text";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
