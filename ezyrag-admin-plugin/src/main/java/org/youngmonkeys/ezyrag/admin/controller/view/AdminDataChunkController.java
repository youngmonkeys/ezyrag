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

package org.youngmonkeys.ezyrag.admin.controller.view;

import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.server.core.annotation.Authenticated;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.loader.AdminRagDataLoaderManager;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;

@Controller
@Authenticated
@EzyFeature("rag")
@AllArgsConstructor
public class AdminDataChunkController {

    private final AdminRagDataLoaderManager dataLoaderManager;
    private final AdminEzyRagSettingService ezyRagSettingService;

    @DoGet("/data-chunks")
    public View dataChunksGet() {
        return View.builder()
            .template("ezyrag/chunk/list")
            .addVariable(
                "defaultEmbeddingServiceName",
                ezyRagSettingService.getEmbeddingService()
            )
            .addVariable(
                "dataSourceTypes",
                dataLoaderManager.getSortedSourceTypes()
            )
            .build();
    }
}
