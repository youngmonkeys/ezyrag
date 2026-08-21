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
import java.util.NoSuchElementException;

import static com.tvd12.ezyfox.io.EzyLists.first;
import static com.tvd12.ezyfox.io.EzyStrings.isNotBlank;
import static org.youngmonkeys.ecommerce.constant.EcommerceTableNames.TABLE_NAME_PRODUCT;

@AllArgsConstructor
public class RagProductDataLoader implements RagDataLoader {

    private final ProductRepository productRepository;
    private final ProductDescriptionRepository productDescriptionRepository;
    private final ProductDescriptionI18nRepository productDescriptionI18nRepository;

    @SuppressWarnings("MethodLength")
    @Override
    public Iterator<RagInputData> load(RagDataSourceModel model) {
        long productId = model.getSourceId();
        Product product = productRepository.findById(productId);
        if (product == null) {
            return Collections.emptyIterator();
        }
        StringBuilder builder = new StringBuilder();
        appendContent(builder, product.getProductName());
        appendContent(builder, product.getProductCode());
        appendContent(builder, product.getSecondaryCode());
        ProductDescription productDescription = productDescriptionRepository
            .findById(productId);
        if (productDescription != null) {
            appendContent(builder, productDescription.getDescription());
        }
        String firstData = builder.length() > 0
            ? builder.toString()
            : null;
        return new Iterator<RagInputData>() {
            private String nextData = firstData;
            private int skip;
            private boolean completed;

            @Override
            public boolean hasNext() {
                if (nextData == null && !completed) {
                    nextData = loadNextI18nData();
                }
                return nextData != null;
            }

            @Override
            public RagInputData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                RagInputData inputData = RagInputData
                    .builder()
                    .data(nextData)
                    .dataType(CommonContentType.TEXT.toString())
                    .build();
                nextData = null;
                return inputData;
            }

            private String loadNextI18nData() {
                while (true) {
                    ProductDescriptionI18n i18n = first(
                        productDescriptionI18nRepository.findListByField(
                            "productId",
                            productId,
                            skip++,
                            1
                        )
                    );
                    if (i18n == null) {
                        completed = true;
                        return null;
                    }
                    builder.setLength(0);
                    appendContent(builder, i18n.getProductName());
                    appendContent(builder, i18n.getDescription());
                    if (builder.length() > 0) {
                        return builder.toString();
                    }
                }
            }
        };
    }

    @Override
    public String getDataSourceType() {
        return TABLE_NAME_PRODUCT;
    }

    private static void appendContent(
        StringBuilder builder,
        String content
    ) {
        if (isNotBlank(content)) {
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(content);
        }
    }
}
