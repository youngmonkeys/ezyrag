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
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class RagMediaTextTextReader
    implements RagMediaTextReader {

    private final EzyRagSettingService ezyRagSettingService;

    @Override
    public Iterable<String> read(File path) {
        int maxChunkLength = ezyRagSettingService
            .getKnowledgeChunkMaxLength();
        return () -> new TextBlockIterator(path, maxChunkLength);
    }

    @Override
    public String[] getMimeTypes() {
        return new String[] {
            "text/plain",
            "text/markdown"
        };
    }

    @Override
    public String[] getExtensions() {
        return new String[] {
            "txt",
            "md",
            "markdown"
        };
    }

    private static class TextBlockIterator implements Iterator<String> {

        private final BufferedReader reader;
        private final int maxChunkLength;
        private String nextBlock;
        private boolean closed;

        TextBlockIterator(File path, int maxChunkLength) {
            this.maxChunkLength = maxChunkLength;
            try {
                this.reader = Files.newBufferedReader(
                    path.toPath(),
                    StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            this.nextBlock = readNextBlock();
        }

        @Override
        public boolean hasNext() {
            return nextBlock != null;
        }

        @Override
        public String next() {
            if (nextBlock == null) {
                throw new NoSuchElementException();
            }
            String block = nextBlock;
            nextBlock = readNextBlock();
            return block;
        }

        private String readNextBlock() {
            if (closed) {
                return null;
            }
            char[] buffer = new char[maxChunkLength];
            try {
                int length = reader.read(buffer);
                if (length == -1) {
                    close();
                    return null;
                }
                return new String(buffer, 0, length);
            } catch (IOException e) {
                close();
                throw new UncheckedIOException(e);
            }
        }

        private void close() {
            if (!closed) {
                closed = true;
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // do nothing
                }
            }
        }
    }
}
