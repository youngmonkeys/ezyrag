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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RagQueryProcessorManager {

    private final EzyLazyInitializer<Map<String, RagQueryProcessor>>
        queryProcessorsByName;

    @SuppressWarnings("unchecked")
    public RagQueryProcessorManager(
        EzySingletonFactory singletonFactory
    ) {
        this.queryProcessorsByName = new EzyLazyInitializer<>(() ->
            ((List<RagQueryProcessor>) singletonFactory
                .getSingletonsOf(RagQueryProcessor.class)
            )
                .stream()
                .collect(
                    Collectors.toMap(
                        RagQueryProcessor::getName,
                        it -> it,
                        (o, n) -> o
                    )
                )
        );
    }

    public RagQueryProcessor getQueryProcessorByName(
        String processorName
    ) {
        return queryProcessorsByName.get().get(processorName);
    }
}
