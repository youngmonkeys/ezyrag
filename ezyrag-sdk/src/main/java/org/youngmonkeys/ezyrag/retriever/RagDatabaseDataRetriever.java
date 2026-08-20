package org.youngmonkeys.ezyrag.retriever;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyrag.constant.RagDataRetrieverName;
import org.youngmonkeys.ezyrag.converter.EzyRagModelToModelConverter;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.model.RagDocumentModel;
import org.youngmonkeys.ezyrag.model.RagVectorSearchResultModel;
import org.youngmonkeys.ezyrag.service.RagDataChunkMetaService;
import org.youngmonkeys.ezyrag.service.RagDataChunkService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyLists.newArrayList;

@AllArgsConstructor
public class RagDatabaseDataRetriever implements RagDataRetriever {

    private final RagDataChunkService dataChunkService;
    private final RagDataChunkMetaService dataChunkMetaService;
    private final EzyRagModelToModelConverter modelToModelConverter;

    @Override
    public List<RagDocumentModel> retrieve(
        List<RagVectorSearchResultModel> result
    ) {
        List<Long> chunkIds = newArrayList(
            result,
            RagVectorSearchResultModel::getChunkId
        );
        List<RagDataChunkModel> chunks = dataChunkService
            .getDataChunksByIds(chunkIds);
        Map<Long, Map<String, String>> metadataMapByChunkId =
            dataChunkMetaService.getDataChunkMetaMapByIds(
                chunkIds
            );
        return newArrayList(chunks, it ->
            modelToModelConverter.toDocument(
                it,
                metadataMapByChunkId.getOrDefault(
                    it.getId(),
                    Collections.emptyMap()
                )
            )
        );
    }

    @Override
    public String getName() {
        return RagDataRetrieverName.DATABASE.toString();
    }
}
