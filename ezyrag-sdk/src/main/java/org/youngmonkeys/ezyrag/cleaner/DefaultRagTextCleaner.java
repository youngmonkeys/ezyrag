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

package org.youngmonkeys.ezyrag.cleaner;

import org.youngmonkeys.ezyrag.model.RagCleanTextModel;

import java.text.Normalizer;

import static com.tvd12.ezyfox.io.EzyStrings.EMPTY_STRING;
import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

public class DefaultRagTextCleaner implements RagTextCleaner {

    @Override
    public RagCleanTextModel cleanText(
        RagCleanTextModel text
    ) {
        return text.toCleanedText(
            clean(text.getText())
        );
    }

    private String clean(String text) {
        if (isBlank(text)) {
            return EMPTY_STRING;
        }
        String result = text;
        result = normalizeUnicode(result);
        result = normalizeLineBreaks(result);
        result = removeControlCharacters(result);
        result = normalizeWhitespace(result);
        result = removeRepeatedBlankLines(result);
        return result.trim();
    }

    private String normalizeUnicode(String text) {
        return Normalizer.normalize(
            text,
            Normalizer.Form.NFC
        );
    }

    private String normalizeLineBreaks(String text) {
        return text
            .replace("\r\n", "\n")
            .replace('\r', '\n');
    }

    private String removeControlCharacters(String text) {
        return text.replaceAll(
            "[\\p{Cc}&&[^\\n\\t]]",
            ""
        );
    }

    private String normalizeWhitespace(String text) {
        return text
            .replaceAll("[ \\t]+", " ")
            .replaceAll(" *\\n *", "\n");
    }

    private String removeRepeatedBlankLines(String text) {
        return text.replaceAll(
            "\\n{3,}",
            "\n\n"
        );
    }
}
