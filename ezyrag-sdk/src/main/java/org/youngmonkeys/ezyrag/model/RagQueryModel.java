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

@Builder
@Getter
public class RagQueryModel {
    private final String query;
    private final int processedTime;

    public RagQueryModel toProcessedQuery(
        String processedQuery
    ) {
        return RagQueryModel
            .builder()
            .query(processedQuery)
            .processedTime(processedTime + 1)
            .build();
    }
}
