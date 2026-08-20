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

package org.youngmonkeys.ezyrag.admin.it.service;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.util.RandomUtil;
import lombok.AllArgsConstructor;
import org.youngmonkeys.devtools.InstanceRandom;
import org.youngmonkeys.ezyrag.admin.repo.AdminRagDataChunkRepository;
import org.youngmonkeys.ezyrag.admin.pagination.AdminRagDataChunkPaginationParameterConverter;
import org.youngmonkeys.ezyrag.admin.service.AdminPaginationRagDataChunkService;
import org.youngmonkeys.ezyrag.entity.RagDataChunk;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.pagination.DefaultRagDataChunkFilter;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkFilter;
import org.youngmonkeys.ezyrag.pagination.RagDataChunkPaginationSortOrder;
import org.youngmonkeys.ezyplatform.model.PaginationModel;
import org.youngmonkeys.ezyplatform.test.IntegrationTest;

import static org.youngmonkeys.ezyplatform.pagination.PaginationModelFetchers.getPaginationModel;

@EzySingleton
@AllArgsConstructor
public class AdminPaginationRagDataChunkServiceIT
    extends EzyLoggable
    implements IntegrationTest {

    private final AdminRagDataChunkRepository ragDataChunkRepository;
    private final AdminPaginationRagDataChunkService paginationRagDataChunkService;
    private final AdminRagDataChunkPaginationParameterConverter
        paginationParameterConverter;

    @Override
    public void test() {
        getRagDataChunkOrderByIdAscTest();
        getRagDataChunkOrderByIdDescTest();
    }

    public void getRagDataChunkOrderByIdAscTest() {
        // given
        RagDataChunkFilter filter = DefaultRagDataChunkFilter
            .builder()
            .build();
        String actualNextPageToken = paginationParameterConverter
            .getDefaultPageToken(
                RagDataChunkPaginationSortOrder
                    .ID_ASC
                    .toString()
            );
        int limit = RandomUtil.randomSmallInt() + 1;

        // when
        RagDataChunk entity = new InstanceRandom().randomObject(RagDataChunk.class);
        ragDataChunkRepository.save(entity);
        PaginationModel<RagDataChunkModel> pagination = getPaginationModel(
            paginationRagDataChunkService,
            filter,
            actualNextPageToken,
            null,
            false,
            limit
        );

        // then
        Asserts.assertTrue(pagination.getCount() > 0);
        ragDataChunkRepository.delete(entity.getId());
    }

    public void getRagDataChunkOrderByIdDescTest() {
        // given
        RagDataChunkFilter filter = DefaultRagDataChunkFilter
            .builder()
            .build();
        String actualPrevPageToken = paginationParameterConverter
            .getDefaultPageToken(
                RagDataChunkPaginationSortOrder
                    .ID_DESC
                    .toString()
            );
        int limit = RandomUtil.randomSmallInt() + 1;

        // when
        RagDataChunk entity = new InstanceRandom().randomObject(RagDataChunk.class);
        ragDataChunkRepository.save(entity);
        PaginationModel<RagDataChunkModel> pagination = getPaginationModel(
            paginationRagDataChunkService,
            filter,
            actualPrevPageToken,
            null,
            false,
            limit
        );

        // then
        Asserts.assertTrue(pagination.getCount() > 0);
        ragDataChunkRepository.delete(entity.getId());
    }
}
