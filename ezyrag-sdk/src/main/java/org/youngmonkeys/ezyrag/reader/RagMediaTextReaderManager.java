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

package org.youngmonkeys.ezyrag.reader;

import com.tvd12.ezyfox.bean.EzySingletonFactory;
import com.tvd12.ezyfox.concurrent.EzyLazyInitializer;
import com.tvd12.ezyfox.io.EzyStrings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RagMediaTextReaderManager {

    private final EzyLazyInitializer<Map<String, RagMediaTextReader>>
        mediaTextReaderByExtension;
    private final EzyLazyInitializer<Map<String, RagMediaTextReader>>
        mediaTextReaderByMimeType;

    @SuppressWarnings("unchecked")
    public RagMediaTextReaderManager(
        EzySingletonFactory singletonFactory
    ) {
        this.mediaTextReaderByExtension = new EzyLazyInitializer<>(() ->
            ((List<RagMediaTextReader>) singletonFactory
                .getSingletonsOf(RagMediaTextReader.class)
            )
                .stream()
                .sorted(Comparator.comparingInt(RagMediaTextReader::getPriority))
                .flatMap(it ->
                    Arrays.stream(it.getExtensions())
                        .filter(EzyStrings::isNotBlank)
                        .map(extension -> new Object[] { extension, it })
                )
                .collect(
                    Collectors.toMap(
                        entry -> (String) entry[0],
                        entry -> (RagMediaTextReader) entry[1],
                        (o, n) -> o
                    )
                )
        );
        this.mediaTextReaderByMimeType = new EzyLazyInitializer<>(() ->
            ((List<RagMediaTextReader>) singletonFactory
                .getSingletonsOf(RagMediaTextReader.class)
            )
                .stream()
                .sorted(Comparator.comparingInt(RagMediaTextReader::getPriority))
                .flatMap(it ->
                    Arrays.stream(it.getMimeTypes())
                        .filter(EzyStrings::isNotBlank)
                        .map(mimeType -> new Object[] { mimeType, it })
                )
                .collect(
                    Collectors.toMap(
                        entry -> (String) entry[0],
                        entry -> (RagMediaTextReader) entry[1],
                        (o, n) -> o
                    )
                )
        );
    }


    public RagMediaTextReader getMediaTextReaderByMimeTypeOrExtension(
        String mimeType,
        String extension
    ) {
        return mediaTextReaderByMimeType
            .get()
            .getOrDefault(
                mimeType,
                mediaTextReaderByExtension
                    .get()
                    .get(extension)
            );
    }
}
