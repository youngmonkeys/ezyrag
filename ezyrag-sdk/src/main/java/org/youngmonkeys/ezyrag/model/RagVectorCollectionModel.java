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

package org.youngmonkeys.ezyrag.model;

import lombok.Builder;
import lombok.Getter;

import java.util.function.Supplier;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.DEFAULT_DISTANCE;

@Builder
@Getter
public class RagVectorCollectionModel {
    private long id;
    private String name;
    private String displayName;
    private String baseUrl;
    private long vectorSize;
    private String distance;
    private String indexType;
    private String status;
    private long pointsCount;
    private String config;
    private long createdAt;
    private long updatedAt;

    public String getBaseUrl(
        Supplier<String> defaulUrlSupplier
    ) {
        return isBlank(baseUrl)
            ? defaulUrlSupplier.get()
            : baseUrl;
    }

    public String getDistance() {
        return isBlank(distance) ? DEFAULT_DISTANCE : distance;
    }
}
