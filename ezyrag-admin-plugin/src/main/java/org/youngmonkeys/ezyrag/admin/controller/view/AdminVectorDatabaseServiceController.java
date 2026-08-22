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
import com.tvd12.ezyhttp.server.core.annotation.PathVariable;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.service.AdminEzyRagSettingService;
import org.youngmonkeys.ezyrag.admin.validator.AdminRagVectorDatabaseServiceValidator;
import org.youngmonkeys.ezyrag.admin.vd.AdminRagVectorDatabaseServiceManager;
import org.youngmonkeys.ezyrag.constant.RagVectorDatabaseServiceName;
import org.youngmonkeys.ezyrag.model.RagEzyVectorConnectionPropertiesModel;
import org.youngmonkeys.ezyrag.model.RagQdrantConnectionPropertiesModel;

import java.util.HashMap;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.EMPTY_STRING;
import static com.tvd12.ezyfox.io.EzyStrings.isNotBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.DEFAULT_HIDDEN_PASSWORD;
import static org.youngmonkeys.ezyrag.admin.validator.AdminRagVectorDatabaseServiceValidator.MIN_VECTOR_SIZE;

@Controller
@Authenticated
@EzyFeature("rag")
@AllArgsConstructor
public class AdminVectorDatabaseServiceController {

    private final AdminRagVectorDatabaseServiceManager vectorDatabaseServiceManager;
    private final AdminRagVectorDatabaseServiceValidator vectorDatabaseServiceValidator;
    private final AdminEzyRagSettingService ezyRagSettingService;

    @DoGet("/vector-database-services")
    public View vectorDatabaseServicesGet() {
        return View.builder()
            .template("ezyrag/vector-database-service/list")
            .addVariable(
                "vectorDatabaseServiceNames",
                vectorDatabaseServiceManager
                    .getSortedVectorDatabaseServiceNames()
            )
            .addVariable(
                "defaultVectorDatabaseServiceName",
                ezyRagSettingService.getVectorDatabaseService()
            )
            .build();
    }

    @DoGet("/vector-database-services/{serviceName}")
    public View vectorDatabaseServiceDetailsGet(
        @PathVariable String serviceName
    ) {
        vectorDatabaseServiceValidator.validateServiceName(serviceName);
        View.Builder viewBuilder = newViewBuilder()
            .template(
                "ezyrag/vector-database-service/" +
                    serviceName.toLowerCase()
            )
            .addVariable("vectorDatabaseServiceName", serviceName)
            .addVariable(
                "qdrantVectorSize",
                ezyRagSettingService.getQdrantVectorSize()
            )
            .addVariable("minVectorSize", MIN_VECTOR_SIZE);
        Map<String, Runnable> viewDecoratorByServiceName = new HashMap<>();
        viewDecoratorByServiceName.put(
            RagVectorDatabaseServiceName.EZYVECTOR.toString(),
            () -> decorateEzyVectorView(viewBuilder)
        );
        viewDecoratorByServiceName.put(
            RagVectorDatabaseServiceName.QDRANT.toString(),
            () -> decorateQdrantView(viewBuilder)
        );
        Runnable decorator = viewDecoratorByServiceName.get(serviceName);
        if (decorator != null) {
            decorator.run();
        }
        return viewBuilder.build();
    }

    private void decorateEzyVectorView(
        View.Builder viewBuilder
    ) {
        RagEzyVectorConnectionPropertiesModel ezyVectorConnectionProperties =
            ezyRagSettingService.getEzyVectorConnectionPropertiesInDb();
        viewBuilder.addVariable(
                "ezyVectorConnection",
                ezyVectorConnectionProperties
            )
            .addVariable(
                "ezyVectorApiKey",
                isNotBlank(ezyVectorConnectionProperties.getApiKey())
                    ? DEFAULT_HIDDEN_PASSWORD
                    : EMPTY_STRING
            )
            .addVariable(
                "ezyVectorVectorSize",
                ezyRagSettingService.getEzyVectorVectorSize()
            );
    }

    private void decorateQdrantView(
        View.Builder viewBuilder
    ) {
        RagQdrantConnectionPropertiesModel qdrantConnectionProperties =
            ezyRagSettingService.getConnectionPropertiesInDb();
        viewBuilder
            .addVariable(
                "qdrantConnection",
                qdrantConnectionProperties
            )
            .addVariable(
                "qdrantApiKeyValue",
                isNotBlank(qdrantConnectionProperties.getApiKey())
                    ? DEFAULT_HIDDEN_PASSWORD
                    : EMPTY_STRING
            )
            .addVariable(
                "qdrantVectorSize",
                ezyRagSettingService.getQdrantVectorSize()
            );
    }

    private View.Builder newViewBuilder() {
        return View.builder()
            .addVariable("currentMenu", "ezyrag.vector_database_services")
            .addVariable("currentParentTitle", "vector_database_services")
            .addVariable("currentParentURL", "/ezyrag/vector-database-services");
    }
}
