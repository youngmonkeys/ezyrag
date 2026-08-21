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

import java.util.Iterator;
import java.util.NoSuchElementException;

final class RagMediaTextBlocks {

    private RagMediaTextBlocks() {}

    static Iterable<String> split(String content, int maxChunkLength) {
        return () -> new Iterator<String>() {
            private int position;

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
        };
    }
}
