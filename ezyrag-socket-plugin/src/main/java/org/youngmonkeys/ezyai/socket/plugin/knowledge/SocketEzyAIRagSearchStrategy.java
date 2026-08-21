package org.youngmonkeys.ezyai.socket.plugin.knowledge;

import com.tvd12.ezyfox.bean.EzyBeanContext;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyfoxserver.context.EzyPluginContext;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyai.knowledge.KnowledgeData;
import org.youngmonkeys.ezyai.knowledge.KnowledgeSearchStrategy;
import org.youngmonkeys.ezyrag.socket.plugin.knowledge.SocketEzyRagKnowledgeSearchStrategy;

import java.util.List;

@EzySingleton
@AllArgsConstructor
public class SocketEzyAIRagSearchStrategy
    implements KnowledgeSearchStrategy {

    private final EzyPluginContext pluginContext;

    @Override
    public List<KnowledgeData> searchKnowledgeDataList(
        String query,
        int limit
    ) {
        SocketEzyRagKnowledgeSearchStrategy strategy = pluginContext
            .getParent()
            .getPluginContext("ezyrag")
            .getProperty(EzyBeanContext.class)
            .getBeanCast(SocketEzyRagKnowledgeSearchStrategy.class);
        return strategy.searchKnowledgeDataList(
            query,
            limit
        );
    }
}
