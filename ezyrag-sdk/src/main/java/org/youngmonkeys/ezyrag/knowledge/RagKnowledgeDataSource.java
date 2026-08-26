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

package org.youngmonkeys.ezyrag.knowledge;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyai.knowledge.KnowledgeData;
import org.youngmonkeys.ezyai.knowledge.KnowledgeDataSource;
import org.youngmonkeys.ezyrag.builder.RagKnowledgeDataBuilder;
import org.youngmonkeys.ezyrag.builder.RagKnowledgeDataBuilderManager;
import org.youngmonkeys.ezyrag.converter.EzyRagModelToModelConverter;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagDocumentModel;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;
import org.youngmonkeys.ezyrag.service.RagDataChunkMetaService;
import org.youngmonkeys.ezyrag.service.RagDataChunkService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tvd12.ezyfox.io.EzyLists.newArrayList;
import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.ZERO_LONG;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_PRODUCT_CODE;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_SLUG;

@AllArgsConstructor
public class RagKnowledgeDataSource implements KnowledgeDataSource {

    private final EzyRagKnowledgeSearchStrategy ragKnowledgeSearchStrategy;
    private final RagKnowledgeDataBuilderManager knowledgeDataBuilderManager;
    private final EzyRagModelToModelConverter modelToModelConverter;
    private final EzyRagSettingService ezyRagSettingService;
    private final RagDataChunkMetaService dataChunkMetaService;
    private final RagDataChunkService dataChunkService;

    @Override
    public List<KnowledgeData> searchKnowledgeDataList(
        String query,
        Map<String, Object> parameters,
        int limit
    ) {
        return ragKnowledgeSearchStrategy.searchKnowledgeDataList(
            query,
            parameters,
            limit
        );
    }

    @Override
    public List<KnowledgeData> getKnowledgeDataListByTypeAndIds(
        String type,
        Collection<Long> ids,
        Map<String, Object> parameters
    ) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<RagDataChunkModel> chunks = dataChunkService
            .getDataChunksBySourceTypeAndSourceIds(type, ids);
        return buildMergedKnowledgeDataList(chunks);
    }

    @Override
    public KnowledgeData getKnowledgeDataByTypeAndIdOrCode(
        String type,
        Long id,
        String code,
        Map<String, Object> parameters
    ) {
        List<RagDataChunkModel> chunks = id != null && id > ZERO_LONG
            ? dataChunkService.getDataChunksBySourceTypeAndSourceId(
                type,
                id
            )
            : Collections.emptyList();
        if (chunks.isEmpty() && !isBlank(code)) {
            chunks = getDataChunksByTypeAndCode(type, code);
        }
        return chunks.isEmpty()
            ? null
            : mergeKnowledgeDataList(buildKnowledgeDataList(chunks));
    }

    private List<RagDataChunkModel> getDataChunksByTypeAndCode(
        String type,
        String code
    ) {
        RagDataChunkModel chunk = getDataChunkByMetaKeyAndMetaValue(
            META_KEY_SLUG,
            code
        );
        if (chunk == null) {
            chunk = getDataChunkByMetaKeyAndMetaValue(
                META_KEY_PRODUCT_CODE,
                code
            );
        }
        return chunk == null || !Objects.equals(type, chunk.getSourceType())
            ? Collections.emptyList()
            : dataChunkService.getDataChunksBySourceTypeAndSourceId(
                type,
                chunk.getSourceId()
            );
    }

    private RagDataChunkModel getDataChunkByMetaKeyAndMetaValue(
        String metaKey,
        String metaValue
    ) {
        Long chunkId = dataChunkMetaService
            .getDataChunkIdByMetaKeyAndMetaValue(
                metaKey,
                metaValue
            );
        if (chunkId == null) {
            return null;
        }
        List<RagDataChunkModel> chunks = dataChunkService
            .getDataChunksByIds(Collections.singletonList(chunkId));
        return chunks.isEmpty() ? null : chunks.get(0);
    }

    private List<KnowledgeData> buildKnowledgeDataList(
        List<RagDataChunkModel> chunks
    ) {
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<Long> chunkIds = newArrayList(
            chunks,
            RagDataChunkModel::getId
        );
        Map<Long, Map<String, String>> metadataMapByChunkId =
            dataChunkMetaService.getDataChunkMetaMapByIds(chunkIds);
        List<RagDocumentModel> documents = newArrayList(chunks, it ->
            modelToModelConverter.toDocument(
                it,
                metadataMapByChunkId.getOrDefault(
                    it.getId(),
                    Collections.emptyMap()
                )
            )
        );
        return getKnowledgeDataBuilder().build(documents);
    }

    private List<KnowledgeData> buildMergedKnowledgeDataList(
        List<RagDataChunkModel> chunks
    ) {
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<RagDataChunkModel>> chunksBySourceId =
            new LinkedHashMap<>();
        for (RagDataChunkModel chunk : chunks) {
            long sourceId = chunk.getSourceId();
            chunksBySourceId
                .computeIfAbsent(sourceId, k -> new ArrayList<>())
                .add(chunk);
        }
        List<KnowledgeData> answer = new ArrayList<>();
        for (List<RagDataChunkModel> sourceChunks
            : chunksBySourceId.values()) {
            KnowledgeData data = mergeKnowledgeDataList(
                buildKnowledgeDataList(sourceChunks)
            );
            if (data != null) {
                answer.add(data);
            }
        }
        return answer;
    }

    private KnowledgeData mergeKnowledgeDataList(
        List<KnowledgeData> list
    ) {
        if (list.isEmpty()) {
            return null;
        }
        KnowledgeData first = list.get(0);
        if (list.size() == 1) {
            return first;
        }
        List<String> contents = new ArrayList<>();
        for (KnowledgeData data : list) {
            contents.add(data.getContent());
        }
        return KnowledgeData.builder()
            .id(first.getId())
            .type(first.getType())
            .title(first.getTitle())
            .slug(first.getSlug())
            .content(
                contents
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n\n"))
            )
            .excerpt(first.getExcerpt())
            .chunked(true)
            .dataUrl(first.getDataUrl())
            .productCode(first.getProductCode())
            .price(first.getPrice())
            .currencyIsoCode(first.getCurrencyIsoCode())
            .build();
    }

    private RagKnowledgeDataBuilder getKnowledgeDataBuilder() {
        String name = ezyRagSettingService.getKnowledgeDataBuilderName();
        RagKnowledgeDataBuilder builder = knowledgeDataBuilderManager
            .getKnowledgeDataBuilderByName(name);
        if (builder == null) {
            throw new IllegalStateException(
                "There is no knowledge data builder: " + name
            );
        }
        return builder;
    }

    @Override
    public String getName() {
        return "RAG";
    }
}
