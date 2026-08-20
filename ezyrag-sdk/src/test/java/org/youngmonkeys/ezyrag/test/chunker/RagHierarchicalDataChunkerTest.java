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

package org.youngmonkeys.ezyrag.test.chunker;

import com.tvd12.test.assertion.Asserts;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.youngmonkeys.ezyrag.chunker.RagHierarchicalDataChunker;
import org.youngmonkeys.ezyrag.constant.RagDataChunkerName;
import org.youngmonkeys.ezyrag.model.RagChunkedResultModel;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import java.util.List;

import static org.mockito.Mockito.when;

public class RagHierarchicalDataChunkerTest {

    private static final String PARAGRAPH_SEPARATOR = "\n\n";
    private static final String SENTENCE_BOUNDARY_PATTERN = "(?<=[.!?])\\s+";

    @Mock
    private EzyRagSettingService ezyRagSettingService;

    @InjectMocks
    private RagHierarchicalDataChunker sut;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void chunkWithNullDataReturnsEmptyList() {
        List<RagChunkedResultModel> actual = sut.chunk(null);

        Asserts.assertTrue(actual.isEmpty());
    }

    @Test
    public void chunkWithBlankDataReturnsEmptyList() {
        List<RagChunkedResultModel> actual = sut.chunk("   \n\t ");

        Asserts.assertTrue(actual.isEmpty());
    }

    @Test
    public void chunkWithContentShorterThanMaxLengthReturnsSingleChunk() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(1000);

        List<RagChunkedResultModel> actual = sut.chunk("Hello   world");

        Asserts.assertEquals(actual.size(), 1);
        Asserts.assertEquals(actual.get(0).getContent(), "Hello world");
        Asserts.assertTrue(actual.get(0).getMetadata().isEmpty());
    }

    @Test
    public void chunkWithContentEqualToMaxLengthReturnsSingleChunk() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(5);

        List<RagChunkedResultModel> actual = sut.chunk("ABCDE");

        Asserts.assertEquals(actual.size(), 1);
        Asserts.assertEquals(actual.get(0).getContent(), "ABCDE");
    }

    @Test
    public void chunkStripsHtmlTagsAndPreservesParagraphBoundaries() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(1000);

        List<RagChunkedResultModel> actual = sut.chunk(
            "<p>Hello world</p><p>Bye</p>"
        );

        Asserts.assertEquals(actual.size(), 1);
        Asserts.assertEquals(
            actual.get(0).getContent(),
            "Hello world\n\nBye"
        );
    }

    @Test
    public void chunkRemovesScriptAndStyleElements() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(1000);

        List<RagChunkedResultModel> actual = sut.chunk(
            "<p>Hello</p><script>alert(1)</script><style>p{color:red}</style>"
        );

        Asserts.assertEquals(actual.size(), 1);
        Asserts.assertEquals(actual.get(0).getContent(), "Hello");
    }

    @Test
    public void chunkConvertsLineBreaksWithoutMergingIntoParagraphBreaks() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(1000);

        List<RagChunkedResultModel> actual = sut.chunk("Line1<br>Line2");

        Asserts.assertEquals(actual.size(), 1);
        Asserts.assertEquals(actual.get(0).getContent(), "Line1\nLine2");
    }

    @Test
    public void chunkSplitsIntoParagraphsWhenContentExceedsMaxLength() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(10);
        when(ezyRagSettingService.getKnowledgeChunkParagraphSeparator())
            .thenReturn(PARAGRAPH_SEPARATOR);
        when(ezyRagSettingService.getKnowledgeChunkSentenceBoundaryPattern())
            .thenReturn(SENTENCE_BOUNDARY_PATTERN);

        String content =
            "AAAAAAAAAA" + PARAGRAPH_SEPARATOR
                + "BBBBBBBBBB" + PARAGRAPH_SEPARATOR
                + "CCCCCCCCCC";

        List<RagChunkedResultModel> actual = sut.chunk(content);

        Asserts.assertEquals(actual.size(), 3);
        Asserts.assertEquals(actual.get(0).getContent(), "AAAAAAAAAA");
        Asserts.assertEquals(actual.get(1).getContent(), "BBBBBBBBBB");
        Asserts.assertEquals(actual.get(2).getContent(), "CCCCCCCCCC");
    }

    @Test
    public void chunkFallsBackToSentencesWhenParagraphExceedsMaxLength() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(10);
        when(ezyRagSettingService.getKnowledgeChunkParagraphSeparator())
            .thenReturn(PARAGRAPH_SEPARATOR);
        when(ezyRagSettingService.getKnowledgeChunkSentenceBoundaryPattern())
            .thenReturn(SENTENCE_BOUNDARY_PATTERN);

        List<RagChunkedResultModel> actual = sut.chunk("AAAAA. BBBBB.");

        Asserts.assertEquals(actual.size(), 2);
        Asserts.assertEquals(actual.get(0).getContent(), "AAAAA.");
        Asserts.assertEquals(actual.get(1).getContent(), "BBBBB.");
    }

    @Test
    public void chunkHardSplitsWhenSentenceExceedsMaxLength() {
        when(ezyRagSettingService.getKnowledgeChunkMaxLength())
            .thenReturn(5);
        when(ezyRagSettingService.getKnowledgeChunkParagraphSeparator())
            .thenReturn(PARAGRAPH_SEPARATOR);
        when(ezyRagSettingService.getKnowledgeChunkSentenceBoundaryPattern())
            .thenReturn(SENTENCE_BOUNDARY_PATTERN);

        List<RagChunkedResultModel> actual = sut.chunk("ABCDEFGHIJ");

        Asserts.assertEquals(actual.size(), 2);
        Asserts.assertEquals(actual.get(0).getContent(), "ABCDE");
        Asserts.assertEquals(actual.get(1).getContent(), "FGHIJ");
    }

    @Test
    public void getNameReturnsHierarchical() {
        Asserts.assertEquals(
            sut.getName(),
            RagDataChunkerName.HIERARCHICAL.toString()
        );
    }
}
