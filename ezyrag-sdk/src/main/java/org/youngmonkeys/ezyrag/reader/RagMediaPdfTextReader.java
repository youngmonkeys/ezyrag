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

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class RagMediaPdfTextReader
    implements RagMediaTextReader {

    private final EzyRagSettingService ezyRagSettingService;

    @Override
    public Iterable<String> read(File path) {
        int maxChunkLength = ezyRagSettingService
            .getKnowledgeChunkMaxLength();
        String content = extractText(path);
        return () -> new TextBlockIterator(content, maxChunkLength);
    }

    @Override
    public String[] getMimeTypes() {
        return new String[] {
            "application/pdf"
        };
    }

    @Override
    public String[] getExtensions() {
        return new String[] {
            "pdf"
        };
    }

    private String extractText(File path) {
        try (PDDocument document = PDDocument.load(path)) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class TextBlockIterator implements Iterator<String> {

        private final String content;
        private final int maxChunkLength;
        private int position;

        TextBlockIterator(String content, int maxChunkLength) {
            this.content = content;
            this.maxChunkLength = maxChunkLength;
        }

        @Override
        public boolean hasNext() {
            return position < content.length();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int end = Math.min(
                position + maxChunkLength,
                content.length()
            );
            String block = content.substring(position, end);
            position = end;
            return block;
        }
    }
}
