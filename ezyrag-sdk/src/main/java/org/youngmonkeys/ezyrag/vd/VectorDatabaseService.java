package org.youngmonkeys.ezyrag.vd;

import org.youngmonkeys.ezyrag.model.VectorPoint;
import org.youngmonkeys.ezyrag.model.VectorSearchResult;

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
        List<VectorPoint> points
    ) throws Exception;

    List<VectorSearchResult> search(
        String baseUrl,
        String apiKey,
        String collectionName,
        float[] vector,
        int limit
    ) throws Exception;
}
