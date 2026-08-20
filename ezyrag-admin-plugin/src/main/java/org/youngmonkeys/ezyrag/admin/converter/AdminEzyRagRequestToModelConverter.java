package org.youngmonkeys.ezyrag.admin.converter;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import org.youngmonkeys.ezyrag.admin.model.AdminSaveQdrantConnectionPropertiesModel;
import org.youngmonkeys.ezyrag.admin.request.AdminSaveQdrantConnectionPropertiesRequest;

@EzySingleton
public class AdminEzyRagRequestToModelConverter {

    public AdminSaveQdrantConnectionPropertiesModel toModel(
        AdminSaveQdrantConnectionPropertiesRequest request
    ) {
        return AdminSaveQdrantConnectionPropertiesModel
            .builder()
            .baseUrl(request.getBaseUrl())
            .apiKey(request.getApiKey())
            .collectionName(request.getCollectionName())
            .build();
    }
}
