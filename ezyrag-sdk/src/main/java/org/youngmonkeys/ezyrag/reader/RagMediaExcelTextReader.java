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
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@AllArgsConstructor
public class RagMediaExcelTextReader
    implements RagMediaTextReader {

    private final EzyRagSettingService ezyRagSettingService;

    @Override
    public Iterable<String> read(File path) {
        int maxChunkLength = ezyRagSettingService
            .getKnowledgeChunkMaxLength();
        String content = extractText(path);
        return RagMediaTextBlocks.split(content, maxChunkLength);
    }

    @Override
    public String[] getMimeTypes() {
        return new String[] {
            "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet"
        };
    }

    @Override
    public String[] getExtensions() {
        return new String[] {
            "xlsx"
        };
    }

    private String extractText(File path) {
        try (
            XSSFWorkbook workbook = new XSSFWorkbook(
                Files.newInputStream(path.toPath())
            );
            XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook)
        ) {
            extractor.setIncludeSheetNames(false);
            return extractor.getText();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
