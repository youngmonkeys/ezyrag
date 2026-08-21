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

import com.tvd12.ezyfox.util.EzyFileUtil;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyplatform.entity.Media;
import org.youngmonkeys.ezyplatform.manager.FileSystemManager;
import org.youngmonkeys.ezyplatform.repo.MediaRepository;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagInputData;
import org.youngmonkeys.ezyrag.reader.RagMediaTextReader;
import org.youngmonkeys.ezyrag.reader.RagMediaTextReaderManager;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;
import static com.tvd12.ezyfox.io.EzyStrings.isNotBlank;
import static org.youngmonkeys.ezyplatform.constant.CommonTableNames.TABLE_NAME_MEDIA;

@AllArgsConstructor
public class RagMediaDataLoader implements RagDataLoader {

    private final RagMediaTextReaderManager mediaTextReaderManager;
    private final FileSystemManager fileSystemManager;
    private final MediaRepository mediaRepository;

    @SuppressWarnings("MethodLength")
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
        RagInputData firstInputData = toTextInputData(builder.toString());
        RagMediaTextReader mediaTextReader = getMediaTextReader(media);
        if (firstInputData == null && mediaTextReader == null) {
            return Collections.emptyIterator();
        }
        return new Iterator<RagInputData>() {
            private RagInputData nextInputData = firstInputData;
            private Iterator<String> mediaTextIterator;
            private boolean mediaTextIteratorLoaded;

            @Override
            public boolean hasNext() {
                if (nextInputData == null) {
                    nextInputData = loadNextMediaTextInputData();
                }
                return nextInputData != null;
            }

            @Override
            public RagInputData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                RagInputData answer = nextInputData;
                nextInputData = null;
                return answer;
            }

            private RagInputData loadNextMediaTextInputData() {
                Iterator<String> iterator = getMediaTextIterator();
                while (iterator != null && iterator.hasNext()) {
                    RagInputData inputData = toTextInputData(iterator.next());
                    if (inputData != null) {
                        return inputData;
                    }
                }
                return null;
            }

            private Iterator<String> getMediaTextIterator() {
                if (!mediaTextIteratorLoaded) {
                    mediaTextIteratorLoaded = true;
                    mediaTextIterator = loadMediaTextIterator();
                }
                return mediaTextIterator;
            }

            private Iterator<String> loadMediaTextIterator() {
                if (mediaTextReader == null) {
                    return null;
                }
                File mediaFile = fileSystemManager.getMediaFilePath(
                    media.getType(),
                    media.getName()
                );
                if (!mediaFile.isFile()) {
                    return null;
                }
                Iterable<String> mediaTextIterable =
                    mediaTextReader.read(mediaFile);
                return mediaTextIterable != null
                    ? mediaTextIterable.iterator()
                    : null;
            }
        };
    }

    @Override
    public String getDataSourceType() {
        return TABLE_NAME_MEDIA;
    }

    private RagMediaTextReader getMediaTextReader(Media media) {
        String mediaName = media.getName();
        if (isBlank(mediaName)) {
            return null;
        }
        return mediaTextReaderManager
            .getMediaTextReaderByMimeTypeOrExtension(
                media.getMimeType(),
                EzyFileUtil.getFileExtension(mediaName)
            );
    }

    private static RagInputData toTextInputData(
        String text
    ) {
        if (isNotBlank(text)) {
            return RagInputData.builder()
                .data(text)
                .dataType(CommonContentType.TEXT.toString())
                .build();
        }
        return null;
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
