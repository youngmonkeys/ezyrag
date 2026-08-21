package org.youngmonkeys.ezyrag.knowledge;

import com.tvd12.ezyfox.util.EzyLoggable;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyai.knowledge.KnowledgeData;
import org.youngmonkeys.ezyai.knowledge.KnowledgeSearchStrategy;
import org.youngmonkeys.ezyrag.client.EzyRagClient;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class EzyRagKnowledgeSearchStrategy
    extends EzyLoggable
    implements KnowledgeSearchStrategy {

    private final EzyRagClient ezyRagClient;

    @Override
    public List<KnowledgeData> searchKnowledgeDataList(
        String query,
        int limit
    ) {
        try {
            return ezyRagClient.getKnowledgeDataList(query, limit);
        } catch (Exception e) {
            logger.warn("search: {} limit; {} error", query, limit, e);
            return Collections.emptyList();
        }
    }
}
