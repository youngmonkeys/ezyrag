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

package org.youngmonkeys.ezyrag.processor;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.concurrent.EzyLazyInitializer;
import org.youngmonkeys.ezyrag.model.RagQueryModel;

import java.util.List;

public class RagQueryProcessorManager {

    private final EzyLazyInitializer<List<RagQueryProcessor>>
        queryProcessors;

    @SuppressWarnings("unchecked")
    public RagQueryProcessorManager(
        EzySingletonFactory singletonFactory
    ) {
        this.queryProcessors = new EzyLazyInitializer<>(() ->
            ((List<RagQueryProcessor>) singletonFactory
                .getSingletonsOf(RagQueryProcessor.class)
            )
        );
    }

    public String processQuery(
        String query
    ) {
        RagQueryModel answer = RagQueryModel.builder()
            .query(query)
            .build();
        for (RagQueryProcessor processor : queryProcessors.get()) {
            answer = processor.process(answer);
        }
        return answer.getQuery();
    }
}
