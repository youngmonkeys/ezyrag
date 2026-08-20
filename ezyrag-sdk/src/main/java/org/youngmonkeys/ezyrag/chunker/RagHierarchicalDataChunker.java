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

package org.youngmonkeys.ezyrag.chunker;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.youngmonkeys.ezyrag.constant.RagDataChunkerName;
import org.youngmonkeys.ezyrag.model.RagChunkedResultModel;
import org.youngmonkeys.ezyrag.model.RagDataChunkModel;
import org.youngmonkeys.ezyrag.service.EzyRagSettingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.tvd12.ezyfox.io.EzyStrings.isBlank;

/**
 * Strips HTML and compacts whitespace in a raw content string and, when the
 * compacted content is still longer than the configured max chunk length,
 * splits it into several smaller {@link RagDataChunkModel} chunks.
 *
 * <p>The chunking algorithm prefers natural text boundaries in hierarchical
 * order:
 *
 * <ol>
 *     <li>
 *         <b>Strip HTML</b> ({@link #stripHtml(String)}):
 *         remove HTML markup while preserving paragraph-like block
 *         boundaries and line breaks.
 *     </li>
 *     <li>
 *         <b>Compact</b> ({@link #compact(String)}):
 *         normalize line endings, collapse runs of spaces/tabs into a single
 *         space, and collapse 3+ consecutive newlines into exactly one blank
 *         line ({@code "\n\n"}).
 *     </li>
 *     <li>
 *         <b>Short-circuit</b>:
 *         if the compacted content already fits within
 *         {@code maxChunkLength} characters, return it as a single chunk.
 *     </li>
 *     <li>
 *         <b>Pack by paragraph</b> ({@link #splitIntoChunks}):
 *         split the content on paragraph boundaries and greedily pack
 *         consecutive paragraphs into the current chunk until adding the
 *         next paragraph would exceed {@code maxChunkLength}.
 *     </li>
 *     <li>
 *         <b>Fall back to sentences</b>
 *         ({@link #splitLongParagraph(String, int, String)}):
 *         when a single paragraph exceeds {@code maxChunkLength}, split it
 *         on sentence boundaries and greedily pack consecutive sentences.
 *     </li>
 *     <li>
 *         <b>Fall back to hard splitting</b>
 *         ({@link #hardSplit(String, int)}):
 *         when a single sentence still exceeds {@code maxChunkLength},
 *         split it directly by character index as a last resort.
 *     </li>
 * </ol>
 *
 * <p>This strategy can be described as hierarchical boundary-aware chunking
 * with greedy packing:
 *
 * <pre>
 * Content
 *   |
 *   v
 * Paragraph
 *   |
 *   | too long
 *   v
 * Sentence
 *   |
 *   | too long
 *   v
 * Hard character split
 * </pre>
 *
 * <p>Example, with {@code maxChunkLength = 40}:
 *
 * <pre>{@code
 * String content =
 *     "Step 1: issue a certificate with Let's Encrypt.\n\n" +
 *     "Step 2: point Nginx at the certificate you just made.";
 *
 * List<DataChunkModel> chunks =
 *     new HierarchicalRagDataChunker(ezyRagSettingService)
 *         .chunk(content);
 *
 * // chunks.get(0).getContent()
 * // -> "Step 1: issue a certificate with Let's Encrypt."
 *
 * // chunks.get(1).getContent()
 * // -> "Step 2: point Nginx at the certificate you just made."
 * }</pre>
 */
@AllArgsConstructor
public class RagHierarchicalDataChunker implements RagDataChunker {

    private static final Pattern HTML_TAG_PATTERN =
        Pattern.compile("<[a-zA-Z/][^>]*>");

    private static final String BLOCK_TAGS_SELECTOR =
        "p, div, li, tr, h1, h2, h3, h4, h5, h6, blockquote";

    /**
     * Supplies chunking parameters from settings so they can be tuned
     * without redeploying the application.
     */
    private final EzyRagSettingService ezyRagSettingService;

    /**
     * Compacts and, if needed, splits the given content into one or more
     * smaller chunks.
     *
     * @param data the raw content to chunk; {@code null} or blank content
     *             yields an empty list
     * @return an empty list when {@code data} is blank, a list containing
     *         one chunk when the compacted content already fits within the
     *         configured maximum chunk length, or multiple chunks when it
     *         does not
     */
    @Override
    public List<RagChunkedResultModel> chunk(String data) {
        if (isBlank(data)) {
            return Collections.emptyList();
        }
        String content = compact(stripHtml(data));
        int maxChunkLength = ezyRagSettingService
            .getKnowledgeChunkMaxLength();

        if (content.length() <= maxChunkLength) {
            return Collections.singletonList(
                toChunkedResult(content)
            );
        }

        List<String> parts = splitIntoChunks(
            content,
            maxChunkLength,
            ezyRagSettingService
                .getKnowledgeChunkParagraphSeparator(),
            ezyRagSettingService
                .getKnowledgeChunkSentenceBoundaryPattern()
        );

        List<RagChunkedResultModel> result =
            new ArrayList<>(parts.size());
        for (String part : parts) {
            result.add(toChunkedResult(part));
        }
        return result;
    }

    /**
     * Removes HTML markup while preserving meaningful block boundaries.
     *
     * <p>Script and style elements are removed. {@code <br>} elements are
     * converted to line breaks, while common block elements such as
     * paragraphs, headings and list items are separated using blank lines.
     *
     * @param content the original content
     * @return plain text content with basic document structure preserved
     */
    private String stripHtml(String content) {
        if (!HTML_TAG_PATTERN.matcher(content).find()) {
            return content;
        }
        Document document = Jsoup.parse(content);
        document
            .select("script, style")
            .remove();

        document
            .select("br")
            .append("\\n");

        document
            .select(BLOCK_TAGS_SELECTOR)
            .prepend("\\n\\n");

        return document
            .body()
            .text()
            .replace("\\n", "\n")
            .replace('\u00A0', ' ');
    }

    /**
     * Normalizes whitespace in {@code content} to reduce unnecessary
     * characters without changing the logical text structure.
     *
     * <p>The following transformations are applied:
     *
     * <ul>
     *     <li>Convert Windows line endings to {@code "\n"}.</li>
     *     <li>Collapse consecutive spaces and tabs into one space.</li>
     *     <li>Remove spaces directly around line breaks.</li>
     *     <li>Collapse 3+ consecutive line breaks into {@code "\n\n"}.</li>
     *     <li>Trim leading and trailing whitespace.</li>
     * </ul>
     *
     * @param content the raw content to normalize
     * @return the compacted content
     */
    private String compact(String content) {
        return content
            .replace("\r\n", "\n")
            .replaceAll("[ \\t]+", " ")
            .replaceAll(" ?\n ?", "\n")
            .replaceAll("\n{3,}", "\n\n")
            .trim();
    }

    /**
     * Splits content into chunks of at most {@code maxChunkLength}
     * characters, preferring paragraph boundaries.
     *
     * <p>Consecutive paragraphs are greedily packed into a chunk. When
     * adding the next paragraph would exceed the configured maximum length,
     * the current chunk is flushed and a new chunk is started.
     *
     * <p>If a single paragraph itself exceeds {@code maxChunkLength}, it is
     * delegated to
     * {@link #splitLongParagraph(String, int, String)}.
     *
     * @param content                 the already compacted content
     * @param maxChunkLength          maximum number of characters per chunk
     * @param paragraphSeparator      regex used to split paragraphs
     * @param sentenceBoundaryPattern regex used for sentence fallback
     * @return chunks in their original document order
     */
    private List<String> splitIntoChunks(
        String content,
        int maxChunkLength,
        String paragraphSeparator,
        String sentenceBoundaryPattern
    ) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : content.split(paragraphSeparator)) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }
            if (paragraph.length() > maxChunkLength) {
                current = flush(chunks, current);
                chunks.addAll(
                    splitLongParagraph(
                        paragraph,
                        maxChunkLength,
                        sentenceBoundaryPattern
                    )
                );

                continue;
            }
            if (current.length() > 0
                    && current.length()
                    + paragraphSeparator.length()
                    + paragraph.length()
                    > maxChunkLength
            ) {
                current = flush(chunks, current);
            }
            if (current.length() > 0) {
                current.append(paragraphSeparator);
            }
            current.append(paragraph);
        }
        flush(chunks, current);
        return chunks;
    }

    /**
     * Splits an over-long paragraph into sentence-based chunks.
     *
     * <p>Consecutive sentences are greedily packed until adding another
     * sentence would exceed {@code maxChunkLength}.
     *
     * <p>If a single sentence is itself longer than the configured maximum,
     * it is delegated to {@link #hardSplit(String, int)}.
     *
     * @param paragraph               the paragraph to split
     * @param maxChunkLength          maximum number of characters per chunk
     * @param sentenceBoundaryPattern regex defining sentence boundaries
     * @return sentence-based chunks
     */
    private List<String> splitLongParagraph(
        String paragraph,
        int maxChunkLength,
        String sentenceBoundaryPattern
    ) {
        List<String> chunks = new ArrayList<>();

        StringBuilder current =
            new StringBuilder();

        for (
            String sentence
            : paragraph.split(sentenceBoundaryPattern)
        ) {
            sentence = sentence.trim();

            if (sentence.isEmpty()) {
                continue;
            }

            if (sentence.length() > maxChunkLength) {
                current = flush(
                    chunks,
                    current
                );

                chunks.addAll(
                    hardSplit(
                        sentence,
                        maxChunkLength
                    )
                );

                continue;
            }

            if (
                current.length() > 0
                    && current.length()
                    + 1
                    + sentence.length()
                    > maxChunkLength
            ) {
                current = flush(
                    chunks,
                    current
                );
            }

            if (current.length() > 0) {
                current.append(' ');
            }

            current.append(sentence);
        }

        flush(
            chunks,
            current
        );

        return chunks;
    }

    /**
     * Last-resort splitting strategy.
     *
     * <p>The text is cut directly every {@code maxChunkLength} characters,
     * without considering words, sentences or other semantic boundaries.
     *
     * <p>This method is used only when a single sentence is itself longer
     * than the configured maximum chunk length.
     *
     * @param text           text to split
     * @param maxChunkLength maximum number of characters per chunk
     * @return fixed-length chunks
     */
    private List<String> hardSplit(
        String text,
        int maxChunkLength
    ) {
        List<String> chunks =
            new ArrayList<>();

        for (
            int i = 0;
            i < text.length();
            i += maxChunkLength
        ) {
            chunks.add(
                text.substring(
                    i,
                    Math.min(
                        i + maxChunkLength,
                        text.length()
                    )
                )
            );
        }

        return chunks;
    }

    /**
     * Flushes the current chunk into the target list.
     *
     * @param chunks  destination chunk list
     * @param current currently accumulated chunk
     * @return a new empty {@link StringBuilder}
     */
    private StringBuilder flush(
        List<String> chunks,
        StringBuilder current
    ) {
        if (current.length() > 0) {
            chunks.add(
                current.toString()
            );
        }

        return new StringBuilder();
    }

    private RagChunkedResultModel toChunkedResult(
        String chunkContent
    ) {
        return RagChunkedResultModel.builder()
            .content(chunkContent)
            .build();
    }

    @Override
    public String getName() {
        return RagDataChunkerName.HIERARCHICAL.toString();
    }
}