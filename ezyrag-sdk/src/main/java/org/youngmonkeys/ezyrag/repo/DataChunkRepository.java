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

package org.youngmonkeys.ezyrag.repo;

import com.tvd12.ezydata.database.EzyDatabaseRepository;
import com.tvd12.ezyfox.database.annotation.EzyQuery;
import org.youngmonkeys.ezyrag.entity.RagDataChunkEntity;
import org.youngmonkeys.ezyrag.result.RagDataChunkEmbeddingResult;

public interface DataChunkRepository
    extends EzyDatabaseRepository<Long, RagDataChunkEntity> {

    @EzyQuery(
        "UPDATE RagDataChunkEntity e " +
            "SET e.embedding = ?1 " +
            "WHERE e.id = ?0"
    )
    void updateEmbeddingById(
        long chunkId,
        float[] embedding
    );

    void deleteBySourceTypeAndSourceIdAndChunkIndexGt(
        String sourceType,
        long sourceId,
        long chunkIndex
    );

    RagDataChunkEntity findBySourceTypeAndSourceIdAndChunkIndex(
        String sourceType,
        long sourceId,
        long chunkIndex
    );

    @EzyQuery(
        "SELECT e.id, e.contentHash, e.embedding FROM RagDataChunkEntity e " +
            "WHERE e.sourceType = ?0 " +
            "AND e.sourceId = ?1 " +
            "AND e.chunkIndex = ?2"
    )
    @SuppressWarnings("LineLength")
    RagDataChunkEmbeddingResult findIdAndContentHashAndEmbeddingBySourceTypeAndSourceIdAndChunkIndex(
        String sourceType,
        long sourceId,
        long chunkIndex
    );
}
