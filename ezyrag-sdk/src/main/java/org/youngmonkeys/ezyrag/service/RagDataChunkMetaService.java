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

package org.youngmonkeys.ezyrag.service;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.model.DataMetaModel;
import org.youngmonkeys.ezyplatform.service.DataMetaService;

import java.util.Collection;
import java.util.Collections;
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

    public Long getDataChunkIdByMetaKeyAndMetaValue(
        String metaKey,
        String metaValue
    ) {
        return dataMetaService
            .getDataIdMapByMetaValues(
                TABLE_NAME_DATA_CHUNK,
                metaKey,
                Collections.singletonList(metaValue)
            )
            .get(metaValue);
    }
}
