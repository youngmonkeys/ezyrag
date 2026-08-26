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

package org.youngmonkeys.ezyrag.socket.plugin.service;

import com.tvd12.ezyhttp.server.core.annotation.Service;
import org.youngmonkeys.ezyplatform.socket.service.SocketSettingService;
import org.youngmonkeys.ezyrag.service.RagVectorCollectionService;
import org.youngmonkeys.ezyrag.socket.plugin.converter.SocketEzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.socket.plugin.converter.SocketEzyRagModelToEntityConverter;
import org.youngmonkeys.ezyrag.socket.plugin.converter.SocketEzyRagResultToModelConverter;
import org.youngmonkeys.ezyrag.socket.plugin.repo.SocketRagVectorCollectionRepository;

@Service
public class SocketRagVectorCollectionService
    extends RagVectorCollectionService {

    public SocketRagVectorCollectionService(
        SocketSettingService settingService,
        SocketRagVectorCollectionRepository collectionRepository,
        SocketEzyRagEntityToModelConverter entityToModelConverter,
        SocketEzyRagModelToEntityConverter modelToEntityConverter,
        SocketEzyRagResultToModelConverter resultToModelConverter
    ) {
        super(
            settingService,
            collectionRepository,
            entityToModelConverter,
            modelToEntityConverter,
            resultToModelConverter
        );
    }
}
