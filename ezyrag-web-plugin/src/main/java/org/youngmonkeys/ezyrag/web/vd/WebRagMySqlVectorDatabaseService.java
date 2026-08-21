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

package org.youngmonkeys.ezyrag.web.vd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyplatform.web.service.WebSettingService;
import org.youngmonkeys.ezyrag.vd.RagMySqlVectorDatabaseService;
import org.youngmonkeys.ezyrag.web.repo.WebRagCollectionPointRepository;
import org.youngmonkeys.ezyrag.web.repo.WebRagCollectionRepository;

@EzySingleton
public class WebRagMySqlVectorDatabaseService
    extends RagMySqlVectorDatabaseService {

    public WebRagMySqlVectorDatabaseService(
        WebSettingService settingService,
        WebRagCollectionRepository collectionRepository,
        WebRagCollectionPointRepository collectionPointRepository,
        ObjectMapper objectMapper
    ) {
        super(
            settingService,
            collectionRepository,
            collectionPointRepository,
            objectMapper
        );
    }
}
