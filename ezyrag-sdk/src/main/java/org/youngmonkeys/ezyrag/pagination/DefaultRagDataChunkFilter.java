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

package org.youngmonkeys.ezyrag.pagination;

import com.tvd12.ezydata.database.query.EzyQueryConditionBuilder;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

@Getter
@Builder
public class DefaultRagDataChunkFilter implements RagDataChunkFilter {
    private final Collection<Long> chunkIds;
    private final String sourceType;
    private final Long sourceId;
    private final String likeKeyword;
    private final Collection<String> keywords;
    private final String keywordPrefix;

    @Override
    public void decorateQueryStringBeforeWhere(
        StringBuilder queryString
    ) {
        if (keywordPrefix != null || keywords != null) {
            queryString.append(" INNER JOIN DataIndex k ON e.id = k.dataId");
        }
    }

    @Override
    public String matchingCondition() {
        EzyQueryConditionBuilder answer = new EzyQueryConditionBuilder();
        if (chunkIds != null) {
            answer.and("e.id IN :chunkIds");
        }
        if (sourceType != null) {
            answer.and("e.sourceType = :sourceType");
        }
        if (sourceId != null) {
            answer.and("e.sourceId = :sourceId");
        }
        if (keywordPrefix != null || keywords != null) {
            answer.and("k.dataType = 'ezyrag_data_chunks'");
            if (keywordPrefix != null) {
                answer.and("k.keyword LIKE CONCAT(:keywordPrefix, '%')");
            }
            if (keywords != null) {
                answer.and("k.keyword IN :keywords");
            }
        }
        if (likeKeyword != null) {
            answer.and(
                "(e.content LIKE CONCAT('%',:likeKeyword,'%') " +
                    "OR e.contentHash LIKE CONCAT('%',:likeKeyword,'%'))"
            );
        }
        return answer.build();
    }
}
