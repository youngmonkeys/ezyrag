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

package org.youngmonkeys.ezyrag.web.service;

import com.tvd12.ezyhttp.server.core.annotation.Service;
import org.youngmonkeys.ezyplatform.web.service.WebSettingService;
import org.youngmonkeys.ezyrag.service.RagVectorCollectionService;
import org.youngmonkeys.ezyrag.web.converter.WebEzyRagEntityToModelConverter;
import org.youngmonkeys.ezyrag.web.converter.WebEzyRagModelToEntityConverter;
import org.youngmonkeys.ezyrag.web.converter.WebEzyRagResultToModelConverter;
import org.youngmonkeys.ezyrag.web.repo.WebRagVectorCollectionRepository;

@Service
public class WebRagVectorCollectionService
    extends RagVectorCollectionService {

    public WebRagVectorCollectionService(
        WebSettingService settingService,
        WebRagVectorCollectionRepository collectionRepository,
        WebEzyRagEntityToModelConverter entityToModelConverter,
        WebEzyRagModelToEntityConverter modelToEntityConverter,
        WebEzyRagResultToModelConverter resultToModelConverter
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
