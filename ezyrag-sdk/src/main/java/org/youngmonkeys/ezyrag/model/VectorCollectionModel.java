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

@Getter
@Builder
public class VectorCollectionModel {
    private String name;
    private String baseUrl;
    private long vectorSize;

    public String getBaseUrl(
        Supplier<String> defaulUrlSupplier
    ) {
        return isBlank(baseUrl)
            ? defaulUrlSupplier.get()
            : baseUrl;
    }
}
