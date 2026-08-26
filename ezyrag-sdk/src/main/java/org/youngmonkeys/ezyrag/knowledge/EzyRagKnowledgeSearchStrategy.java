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

import com.tvd12.ezyfox.util.EzyLoggable;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyai.knowledge.KnowledgeData;
import org.youngmonkeys.ezyai.knowledge.KnowledgeSearchStrategy;
import org.youngmonkeys.ezyrag.client.EzyRagClient;
import org.youngmonkeys.ezyrag.model.VectorCollectionModel;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;
import org.youngmonkeys.ezyrag.service.RagVectorCollectionService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

@AllArgsConstructor
public class EzyRagKnowledgeSearchStrategy
    extends EzyLoggable
    implements KnowledgeSearchStrategy {

    private final EzyRagClient ezyRagClient;
    private final EzyRagSettingService ezyRagSettingService;
    private final RagVectorCollectionService vectorCollectionService;

    @SuppressWarnings("MethodLength")
    @Override
    public List<KnowledgeData> searchKnowledgeDataList(
        String query,
        Map<String, Object> parameters,
        int limit
    ) {
        String vectorDatabaseServiceName = (String) parameters
            .get("vector_database_service_name");
        if (isBlank(vectorDatabaseServiceName)) {
            vectorDatabaseServiceName = ezyRagSettingService
                .getVectorDatabaseServiceName();
        }
        if (isBlank(vectorDatabaseServiceName)) {
            logger.warn(
                "there is no vector database service, " +
                    "you need set as default one"
            );
            return Collections.emptyList();
        }
        String collectionName = (String) parameters
            .get("rag_collection_name");
        if (isBlank(collectionName)) {
            collectionName = ezyRagSettingService
                .getDefaultCollectionNameByVectorDbServiceName(
                    vectorDatabaseServiceName
                );
        }
        if (isBlank(collectionName)) {
            logger.warn(
                "there is no vector collection, " +
                    "you need set as default one"
            );
            return Collections.emptyList();
        }
        VectorCollectionModel collection = vectorCollectionService
            .getVectorCollectionByDbServiceNameAndCollectionName(
                vectorDatabaseServiceName,
                collectionName
            );
        if (collection == null) {
            logger.warn(
                "vector collection: {} of db service: {}, no found",
                vectorDatabaseServiceName,
                collectionName
            );
            return Collections.emptyList();
        }
        try {
            return ezyRagClient.getKnowledgeDataList(
                collection,
                query,
                limit
            );
        } catch (Exception e) {
            logger.warn(
                "search: {} limit; {} in collection: {} " +
                    "of vector db service: {} error",
                query,
                limit,
                vectorDatabaseServiceName,
                collectionName,
                e
            );
            return Collections.emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Short.MIN_VALUE;
    }
}
