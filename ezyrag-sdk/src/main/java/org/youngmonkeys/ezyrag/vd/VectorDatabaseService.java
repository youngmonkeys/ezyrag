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

package org.youngmonkeys.ezyrag.vd;

import org.youngmonkeys.ezyrag.model.VectorPointModel;
import org.youngmonkeys.ezyrag.model.VectorSearchResultModel;

import java.util.List;

public interface VectorDatabaseService {

    String getProviderName();

    void createCollectionIfAbsent(
        String baseUrl,
        String apiKey,
        String collectionName,
        int vectorSize
    ) throws Exception;

    void upsert(
        String baseUrl,
        String apiKey,
        String collectionName,
        List<VectorPointModel> points
    ) throws Exception;

    List<VectorSearchResultModel> search(
        String baseUrl,
        String apiKey,
        String collectionName,
        float[] vector,
        int limit
    ) throws Exception;
}
