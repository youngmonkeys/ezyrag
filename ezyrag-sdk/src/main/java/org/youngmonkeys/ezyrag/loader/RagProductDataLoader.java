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

package org.youngmonkeys.ezyrag.loader;

import lombok.AllArgsConstructor;
import org.youngmonkeys.ecommerce.entity.Product;
import org.youngmonkeys.ecommerce.entity.ProductDescription;
import org.youngmonkeys.ecommerce.entity.ProductDescriptionI18n;
import org.youngmonkeys.ecommerce.repo.ProductDescriptionI18nRepository;
import org.youngmonkeys.ecommerce.repo.ProductDescriptionRepository;
import org.youngmonkeys.ecommerce.repo.ProductRepository;
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagInputData;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.tvd12.ezyfox.io.EzyLists.first;
import static com.tvd12.ezyfox.io.EzyStrings.isNotBlank;
import static org.youngmonkeys.ecommerce.constant.EcommerceTableNames.TABLE_NAME_PRODUCT;

@AllArgsConstructor
public class RagProductDataLoader implements RagDataLoader {

    private final ProductRepository productRepository;
    private final ProductDescriptionRepository productDescriptionRepository;
    private final ProductDescriptionI18nRepository productDescriptionI18nRepository;

    @Override
    public Iterator<RagInputData> load(RagDataSourceModel model) {
        long productId = model.getSourceId();
        Product product = productRepository.findById(productId);
        if (product == null) {
            return Collections.emptyIterator();
        }
        StringBuilder builder = new StringBuilder()
            .append(product.getProductName())
            .append("\n\n")
            .append(product.getProductCode());
        String secondaryCode = product.getSecondaryCode();
        if (isNotBlank(secondaryCode)) {
            builder.append("\n\n").append(secondaryCode);
        }
        ProductDescription productDescription = productDescriptionRepository
            .findById(productId);
        if (productDescription != null) {
            String description = productDescription.getDescription();
            if (isNotBlank(description)) {
                builder.append("\n\n").append(description);
            }
        }
        AtomicReference<String> ref = new AtomicReference<>(
            builder.toString()
        );
        AtomicInteger skip = new AtomicInteger();
        return new Iterator<RagInputData>() {
            @Override
            public boolean hasNext() {
                return ref.get() != null;
            }

            @Override
            public RagInputData next() {
                RagInputData inputData = RagInputData
                    .builder()
                    .data(ref.get())
                    .dataType(CommonContentType.TEXT.toString())
                    .build();
                ProductDescriptionI18n i18n = first(
                    productDescriptionI18nRepository.findListByField(
                        "productId",
                        productId,
                        skip.getAndIncrement(),
                        1
                    )
                );
                if (i18n == null) {
                    ref.set(null);
                } else {
                    builder.setLength(0);
                    String name = i18n.getProductName();
                    String des = i18n.getDescription();
                    boolean isNotBlankName = isNotBlank(name);
                    boolean isNotBlankDes = isNotBlank(des);
                    if (isNotBlankName) {
                        builder.append(name);
                    }
                    if (isNotBlankName && isNotBlankDes) {
                        builder.append("\n\n");
                    }
                    if (isNotBlankDes) {
                        builder.append(des);
                    }
                    ref.set(builder.toString());
                }
                return inputData;
            }
        };
    }

    @Override
    public String getDataSourceType() {
        return TABLE_NAME_PRODUCT;
    }
}
