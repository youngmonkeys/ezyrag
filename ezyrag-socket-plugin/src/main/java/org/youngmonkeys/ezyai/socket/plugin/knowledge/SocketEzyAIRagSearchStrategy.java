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
