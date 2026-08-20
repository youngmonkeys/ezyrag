package org.youngmonkeys.ezyrag.admin.converter;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveQdrantConnectionPropertiesRequest;
import org.youngmonkeys.ezyrag.model.RagQdrantConnectionPropertiesModel;

@EzySingleton
public class AdminEzyRagRequestToModelConverter {

    public RagQdrantConnectionPropertiesModel toModel(
        AdminSaveQdrantConnectionPropertiesRequest request
    ) {
        return RagQdrantConnectionPropertiesModel
            .builder()
            .baseUrl(request.getBaseUrl())
            .apiKey(request.getApiKey())
            .collectionName(request.getCollectionName())
            .vectorSize(request.getVectorSize())
            .build();
    }
}
