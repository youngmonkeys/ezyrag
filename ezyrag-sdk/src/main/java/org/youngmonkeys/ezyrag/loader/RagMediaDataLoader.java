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
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyplatform.entity.Media;
import org.youngmonkeys.ezyplatform.repo.MediaRepository;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagInputData;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.tvd12.ezyfox.io.EzyStrings.isNotBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonTableNames.TABLE_NAME_MEDIA;

@AllArgsConstructor
public class RagMediaDataLoader implements RagDataLoader {

    private final MediaRepository mediaRepository;

    @Override
    public Iterator<RagInputData> load(
        RagDataSourceModel model
    ) {
        Media media = mediaRepository.findById(model.getSourceId());
        if (media == null) {
            return Collections.emptyIterator();
        }
        StringBuilder builder = new StringBuilder();
        appendContent(builder, media.getTitle());
        appendContent(builder, media.getCaption());
        appendContent(builder, media.getAlternativeText());
        appendContent(builder, media.getDescription());
        appendContent(builder, media.getName());
        appendContent(builder, media.getOriginalName());
        if (builder.length() == 0) {
            return Collections.emptyIterator();
        }
        return Stream
            .of(builder.toString())
            .map(it ->
                RagInputData.builder()
                    .data(it)
                    .dataType(CommonContentType.TEXT.toString())
                    .build()
            )
            .iterator();
    }

    @Override
    public String getDataSourceType() {
        return TABLE_NAME_MEDIA;
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
