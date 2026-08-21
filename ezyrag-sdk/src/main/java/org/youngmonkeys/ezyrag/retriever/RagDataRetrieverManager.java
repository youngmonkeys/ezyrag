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

package org.youngmonkeys.ezyrag.retriever;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.concurrent.EzyLazyInitializer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RagDataRetrieverManager {

    private final EzyLazyInitializer<Map<String, RagDataRetriever>>
        dataRetrieverBySourceType;

    @SuppressWarnings("unchecked")
    public RagDataRetrieverManager(
        EzySingletonFactory singletonFactory
    ) {
        this.dataRetrieverBySourceType = new EzyLazyInitializer<>(() ->
            ((List<RagDataRetriever>) singletonFactory
                .getSingletonsOf(RagDataRetriever.class)
            )
                .stream()
                .collect(
                    Collectors.toMap(
                        RagDataRetriever::getName,
                        it -> it,
                        (o, n) -> o
                    )
                )
        );
    }

    public RagDataRetriever getDataRetrieverByName(
        String name
    ) {
        return dataRetrieverBySourceType.get().get(name);
    }

    public List<String> getSortedDataRetrieverNames() {
        return dataRetrieverBySourceType
            .get()
            .keySet()
            .stream()
            .sorted()
            .collect(Collectors.toList());
    }
}
