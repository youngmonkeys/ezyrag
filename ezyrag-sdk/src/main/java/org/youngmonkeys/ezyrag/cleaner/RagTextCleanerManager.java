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

package org.youngmonkeys.ezyrag.cleaner;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.concurrent.EzyLazyInitializer;
import org.youngmonkeys.ezyrag.model.RagCleanTextModel;

import java.util.List;

public class RagTextCleanerManager {

    private final EzyLazyInitializer<List<RagTextCleaner>>
        knowledgeDataFetchers;

    @SuppressWarnings("unchecked")
    public RagTextCleanerManager(
        EzySingletonFactory singletonFactory
    ) {
        this.knowledgeDataFetchers = new EzyLazyInitializer<>(() ->
            ((List<RagTextCleaner>) singletonFactory
                .getSingletonsOf(RagTextCleaner.class)
            )
        );
    }

    public String cleanText(
        String text
    ) {
        RagCleanTextModel cleanTextModel = RagCleanTextModel.builder()
            .text(text)
            .build();
        for (RagTextCleaner cleaner : knowledgeDataFetchers.get()) {
            cleanTextModel = cleaner.cleanText(cleanTextModel);
        }
        return cleanTextModel.getText();
    }
}
