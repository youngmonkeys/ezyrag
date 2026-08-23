/*
 * Copyright 2026 youngmonkeys.org
 * 
 * Licensed under the ezyplatform, Version 1.0.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://youngmonkeys.org/licenses/ezyplatform-1.0.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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

    @Override
    public int getPriority() {
        return Short.MIN_VALUE;
    }
}
