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

package org.youngmonkeys.ezyrag.socket.plugin.knowledge;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.knowledge.RagKnowledgeDataSource;
import org.youngmonkeys.ezyrag.socket.plugin.builder.SocketRagKnowledgeDataBuilderManager;
import org.youngmonkeys.ezyrag.socket.plugin.converter.SocketEzyRagModelToModelConverter;
import org.youngmonkeys.ezyrag.socket.plugin.service.SocketEzyRagSettingService;
import org.youngmonkeys.ezyrag.socket.plugin.service.SocketRagDataChunkMetaService;
import org.youngmonkeys.ezyrag.socket.plugin.service.SocketRagDataChunkService;

@EzySingleton
public class SocketRagKnowledgeDataSource extends RagKnowledgeDataSource {

    public SocketRagKnowledgeDataSource(
        SocketEzyRagKnowledgeSearchStrategy ragKnowledgeSearchStrategy,
        SocketRagKnowledgeDataBuilderManager knowledgeDataBuilderManager,
        SocketEzyRagModelToModelConverter modelToModelConverter,
        SocketEzyRagSettingService ezyRagSettingService,
        SocketRagDataChunkMetaService dataChunkMetaService,
        SocketRagDataChunkService dataChunkService
    ) {
        super(
            ragKnowledgeSearchStrategy,
            knowledgeDataBuilderManager,
            modelToModelConverter,
            ezyRagSettingService,
            dataChunkMetaService,
            dataChunkService
        );
    }
}
