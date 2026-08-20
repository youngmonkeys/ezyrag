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

package org.youngmonkeys.ezyrag.builder;

import org.youngmonkeys.ezyai.knowledge.KnowledgeData;
import org.youngmonkeys.ezyrag.constant.RagKnowledgeDataBuilderName;
import org.youngmonkeys.ezyrag.model.RagDocumentModel;

import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyLists.newArrayList;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_CURRENCY_ISO_CODE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_DATA_URL;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_EXCERPT;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_PRICE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_PRODUCT_CODE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_SLUG;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_TITLE;

public class RagDefaultKnowledgeDataBuilder
    implements RagKnowledgeDataBuilder {

    @Override
    public List<KnowledgeData> build(
        List<RagDocumentModel> documents
    ) {
        return newArrayList(documents, this::toKnowledgeData);
    }

    private KnowledgeData toKnowledgeData(
        RagDocumentModel document
    ) {
        Map<String, String> metadata = document.getMetadata();
        return KnowledgeData.builder()
            .id(document.getSourceId())
            .type(document.getSourceType())
            .title(metadata.get(META_KEY_TITLE))
            .slug(metadata.get(META_KEY_SLUG))
            .content(document.getContent())
            .excerpt(metadata.get(META_KEY_EXCERPT))
            .chunked(true)
            .dataUrl(metadata.get(META_KEY_DATA_URL))
            .productCode(metadata.get(META_KEY_PRODUCT_CODE))
            .price(metadata.get(META_KEY_PRICE))
            .currencyIsoCode(metadata.get(META_KEY_CURRENCY_ISO_CODE))
            .build();
    }

    @Override
    public String getName() {
        return RagKnowledgeDataBuilderName.DEFAULT.toString();
    }
}
