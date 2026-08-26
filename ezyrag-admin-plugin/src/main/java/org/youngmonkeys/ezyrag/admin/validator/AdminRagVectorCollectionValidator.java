/*
 * Copyright 2023 youngmonkeys.org
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

package org.youngmonkeys.ezyrag.admin.validator;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.core.exception.HttpBadRequestException;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveRagVectorCollectionRequest;
import org.youngmonkeys.ezyrag.admin.service.AdminRagVectorCollectionService;
import org.youngmonkeys.ezyrag.model.RagVectorCollectionModel;

import java.util.HashMap;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

@EzySingleton
@AllArgsConstructor
public class AdminRagVectorCollectionValidator {

    private final AdminRagVectorCollectionService vectorCollectionService;

    public void validate(
        AdminSaveRagVectorCollectionRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        validate(errors, request);
        if (errors.isEmpty()
            && vectorCollectionService
            .getCollectionByVectorDbServiceNameAndName(
                request.getVectorDbService(),
                request.getName()
            ) != null
        ) {
            errors.put("name", "duplicated");
        }
        if (!errors.isEmpty()) {
            throw new HttpBadRequestException(errors);
        }
    }

    public void validate(
        long collectionId,
        AdminSaveRagVectorCollectionRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        validate(errors, request);
        if (errors.isEmpty()) {
            RagVectorCollectionModel other = vectorCollectionService
                .getCollectionByVectorDbServiceNameAndName(
                    request.getVectorDbService(),
                    request.getName()
                );
            if (other != null && collectionId != other.getId()) {
                errors.put("name", "duplicated");
            }
        }
        if (!errors.isEmpty()) {
            throw new HttpBadRequestException(errors);
        }
    }

    private void validate(
        Map<String, String> errors,
        AdminSaveRagVectorCollectionRequest request
    ) {
        if (isBlank(request.getVectorDbService())) {
            errors.put("vectorDbService", "required");
        }
        if (isBlank(request.getName())) {
            errors.put("name", "required");
        }
    }
}
