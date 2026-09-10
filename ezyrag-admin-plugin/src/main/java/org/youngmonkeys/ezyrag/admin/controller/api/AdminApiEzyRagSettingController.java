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

package org.youngmonkeys.ezyrag.admin.controller.api;

import com.tvd12.ezyfox.annotation.EzyFeature;
import com.tvd12.ezyhttp.core.annotation.Description;
import com.tvd12.ezyhttp.core.response.ResponseEntity;
import com.tvd12.ezyhttp.server.core.annotation.*;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveEzyRagSettingsRequest;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;

@Api
@Authenticated
@Controller("/api/v1")
@EzyFeature("settings_management")
@AllArgsConstructor
public class AdminApiEzyRagSettingController {

    private final AdminEzyRagSettingService ezyRagSettingService;

    @Description("Update ezyrag settings")
    @DoPut("/settings")
    public ResponseEntity settingsPut(
        @RequestBody AdminSaveEzyRagSettingsRequest request
    ) {
        ezyRagSettingService.setKnowledgeDataBuilderName(
            request.getKnowledgeDataBuilderName()
        );
        ezyRagSettingService.setKnowledgeChunkMaxLength(
            request.getKnowledgeChunkMaxLength()
        );
        ezyRagSettingService.setDataChunkerName(
            request.getDataChunkerName()
        );
        ezyRagSettingService.setDataRetrieverName(
            request.getDataRetrieverName()
        );
        return ResponseEntity.noContent();
    }
}
