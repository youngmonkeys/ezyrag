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
import org.youngmonkeys.ezyai.knowledge.KnowledgeDataSource;
import org.youngmonkeys.ezyrag.socket.plugin.knowledge.SocketRagKnowledgeDataSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@EzySingleton
@AllArgsConstructor
public class SocketEzyAIRagKnowledgeDataSource implements KnowledgeDataSource {

    private final EzyPluginContext pluginContext;

    @Override
    public List<KnowledgeData> searchKnowledgeDataList(
        String query,
        Map<String, Object> parameters,
        int limit
    ) {
        return getDataSource().searchKnowledgeDataList(
            query,
            parameters,
            limit
        );
    }

    @Override
    public List<KnowledgeData> getKnowledgeDataListByTypeAndIds(
        String type,
        Collection<Long> ids,
        Map<String, Object> parameters
    ) {
        return getDataSource().getKnowledgeDataListByTypeAndIds(
            type,
            ids,
            parameters
        );
    }

    @Override
    public KnowledgeData getKnowledgeDataByTypeAndIdOrCode(
        String type,
        Long id,
        String code,
        Map<String, Object> parameters
    ) {
        return getDataSource().getKnowledgeDataByTypeAndIdOrCode(
            type,
            id,
            code,
            parameters
        );
    }

    @Override
    public String getName() {
        return getDataSource().getName();
    }

    private SocketRagKnowledgeDataSource getDataSource() {
        return pluginContext
            .getParent()
            .getPluginContext("ezyrag")
            .getProperty(EzyBeanContext.class)
            .getBeanCast(SocketRagKnowledgeDataSource.class);
    }
}
