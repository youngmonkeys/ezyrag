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
