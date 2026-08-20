package org.youngmonkeys.ezyrag.service;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.model.DataMetaModel;
import org.youngmonkeys.ezyplatform.service.DataMetaService;

import java.util.Collection;
import java.util.Map;

import static org.youngmonkeys.ezyrag.constant.EzyRagTableNames.TABLE_NAME_DATA_CHUNK;

@AllArgsConstructor
public class RagDataChunkMetaService {

    private final DataMetaService dataMetaService;

    public void saveDataChunkMeta(
        long chunkId,
        Map<String, Object> metadata
    ) {
        dataMetaService.saveDataMetaValueAndTextValueUniqueKeys(
            TABLE_NAME_DATA_CHUNK,
            chunkId,
            metadata
        );
    }

    public Map<Long, Map<String, String>> getDataChunkMetaMapByIds(
        Collection<Long> chunkIds
    ) {
        return dataMetaService.getDataMetaValueMapsByDataTypeAndDataIds(
            TABLE_NAME_DATA_CHUNK,
            chunkIds,
            DataMetaModel::getMetaValue
        );
    }
}
