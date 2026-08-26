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
import com.tvd12.ezyhttp.server.core.annotation.RequestParam;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.loader.AdminRagDataLoaderManager;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.vd.AdminRagVectorDatabaseServiceManager;

import java.util.List;

import static com.tvd12.ezyfox.io.EzyLists.newArrayList;
import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.VIEW_VARIABLE_ADDITIONAL_MESSAGE_KEYS;

@Controller
@Authenticated
@EzyFeature("rag")
@AllArgsConstructor
public class AdminDataChunkController {

    private final AdminRagDataLoaderManager dataLoaderManager;
    private final AdminRagVectorDatabaseServiceManager vectorDatabaseServiceManager;
    private final AdminEzyRagSettingService ezyRagSettingService;

    @DoGet("/data-chunks")
    public View dataChunksGet(
        @RequestParam("vectorDbServiceName") String vectorDbServiceName,
        @RequestParam("collectionName") String collectionName
    ) {
        List<String> datasourceTypes = dataLoaderManager
            .getSortedSourceTypes();
        String defaultVectorDatabaseServiceName = ezyRagSettingService
            .getVectorDatabaseServiceName();
        String selectedVectorDbServiceName = isBlank(vectorDbServiceName)
            ? defaultVectorDatabaseServiceName
            : vectorDbServiceName;
        String selectedCollectionName = isBlank(collectionName)
            ? ezyRagSettingService.getDefaultCollectionNameByVectorDbServiceName(
                selectedVectorDbServiceName
            )
            : collectionName;
        return View.builder()
            .template("ezyrag/chunk/list")
            .addVariable("vectorDbServiceName", selectedVectorDbServiceName)
            .addVariable("collectionName", selectedCollectionName)
            .addVariable(
                "defaultEmbeddingServiceName",
                ezyRagSettingService.getEmbeddingServiceName()
            )
            .addVariable(
                "vectorDatabaseServiceNames",
                vectorDatabaseServiceManager
                    .getSortedVectorDatabaseServiceNames()
            )
            .addVariable(
                "defaultVectorDatabaseServiceName",
                defaultVectorDatabaseServiceName
            )
            .addVariable("dataSourceTypes", datasourceTypes)
            .appendValuesToVariable(
                VIEW_VARIABLE_ADDITIONAL_MESSAGE_KEYS,
                newArrayList(datasourceTypes, String::toLowerCase)
            )
            .build();
    }
}
