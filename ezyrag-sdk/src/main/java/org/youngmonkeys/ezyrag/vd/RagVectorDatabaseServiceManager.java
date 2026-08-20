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

package org.youngmonkeys.ezyrag.vd;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.concurrent.EzyLazyInitializer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RagVectorDatabaseServiceManager {

    private final EzyLazyInitializer<Map<String, RagVectorDatabaseService>>
        dataRetrieverBySourceType;

    @SuppressWarnings("unchecked")
    public RagVectorDatabaseServiceManager(
        EzySingletonFactory singletonFactory
    ) {
        this.dataRetrieverBySourceType = new EzyLazyInitializer<>(() ->
            ((List<RagVectorDatabaseService>) singletonFactory
                .getSingletonsOf(RagVectorDatabaseService.class)
            )
                .stream()
                .collect(
                    Collectors.toMap(
                        RagVectorDatabaseService::getProviderName,
                        it -> it,
                        (o, n) -> o
                    )
                )
        );
    }

    public RagVectorDatabaseService getEmbeddingServiceByName(
        String serviceName
    ) {
        return dataRetrieverBySourceType.get().get(serviceName);
    }

    public List<String> getSortedVectorDatabaseServiceNames() {
        return dataRetrieverBySourceType
            .get()
            .keySet()
            .stream()
            .sorted()
            .collect(Collectors.toList());
    }
}
