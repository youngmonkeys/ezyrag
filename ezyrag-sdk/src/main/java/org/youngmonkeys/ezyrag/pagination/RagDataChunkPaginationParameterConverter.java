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

package org.youngmonkeys.ezyrag.pagination;

import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyplatform.pagination.ComplexPaginationParameterConverter;
import org.youngmonkeys.ezyplatform.pagination.PaginationParameterConverter;

import java.util.Map;
import java.util.function.Function;

public class RagDataChunkPaginationParameterConverter
    extends ComplexPaginationParameterConverter<
        String,
        RagDataChunkModel
    > {

    public RagDataChunkPaginationParameterConverter(
        PaginationParameterConverter converter
    ) {
        super(converter);
    }

    @Override
    protected void mapPaginationParametersToTypes(
        Map<String, Class<?>> map
    ) {
        map.put(
            RagDataChunkPaginationSortOrder.ID_ASC.toString(),
            IdAscRagDataChunkPaginationParameter.class
        );
        map.put(
            RagDataChunkPaginationSortOrder.ID_DESC.toString(),
            IdDescRagDataChunkPaginationParameter.class
        );
    }

    @Override
    protected void addPaginationParameterExtractors(
        Map<String, Function<RagDataChunkModel, Object>> map
    ) {
        map.put(
            RagDataChunkPaginationSortOrder.ID_ASC.toString(),
            model -> new IdAscRagDataChunkPaginationParameter(
                model.getId()
            )
        );
        map.put(
            RagDataChunkPaginationSortOrder.ID_DESC.toString(),
            model -> new IdDescRagDataChunkPaginationParameter(
                model.getId()
            )
        );
    }
}
