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

package org.youngmonkeys.ezyrag.web.retriever;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.converter.EzyRagModelToModelConverter;
import org.youngmonkeys.ezyrag.retriever.RagDatabaseDataRetriever;
import org.youngmonkeys.ezyrag.service.RagDataChunkMetaService;
import org.youngmonkeys.ezyrag.service.RagDataChunkService;

@EzySingleton
public class WebRagDatabaseDataRetriever
    extends RagDatabaseDataRetriever {

    public WebRagDatabaseDataRetriever(
        RagDataChunkService dataChunkService,
        RagDataChunkMetaService dataChunkMetaService,
        EzyRagModelToModelConverter modelToModelConverter
    ) {
        super(dataChunkService, dataChunkMetaService, modelToModelConverter);
    }
}
