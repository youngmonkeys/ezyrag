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
                }
            }
        }
    }
}
