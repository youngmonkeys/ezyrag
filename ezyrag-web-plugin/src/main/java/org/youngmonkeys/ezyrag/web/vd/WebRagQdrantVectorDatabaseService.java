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

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.client.HttpClient;
import org.youngmonkeys.ezyplatform.web.service.WebSettingService;
import org.youngmonkeys.ezyrag.vd.RagQdrantVectorDatabaseService;
import org.youngmonkeys.ezyrag.web.service.WebRagVectorCollectionService;

@EzySingleton
public class WebRagQdrantVectorDatabaseService
    extends RagQdrantVectorDatabaseService {

    public WebRagQdrantVectorDatabaseService(
        HttpClient httpClient,
        WebSettingService settingService,
        WebRagVectorCollectionService vectorCollectionService
    ) {
        super(
            httpClient,
            settingService,
            vectorCollectionService
        );
    }
}
