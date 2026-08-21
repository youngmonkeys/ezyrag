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

package org.youngmonkeys.ezyrag.vd;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class EzyVectorFileStorage {

    private static final long SEGMENT_NO = 1L;
    private static final long MAX_MAPPED_BYTES = 128L * 1024L * 1024L;
    private static final String VECTORS_FILE = "vectors.dat";
    private static final String POINT_IDS_FILE = "point_ids.dat";
    private static final String BACKFILL_PROGRESS_FILE = "backfill.progress";

    private final Path dataDir;

    public EzyVectorFileStorage(String dataDir) {
        this.dataDir = Paths.get(dataDir);
    }

    public void upsert(
        long collectionId,
        long slotId,
        long pointId,
        int vectorSize,
        float[] vector
    ) throws IOException {
        upsertAll(
            collectionId,
            vectorSize,
            Collections.singletonList(
                new VectorRecord(slotId, pointId, vector)
            )
        );
    }

    public void upsertAll(
        long collectionId,
        int vectorSize,
        List<VectorRecord> records
    ) throws IOException {
        if (records.isEmpty()) {
            return;
        }
        Path segmentDir = getSegmentDir(collectionId);
        Files.createDirectories(segmentDir);
        try (
            FileChannel vectorChannel = FileChannel.open(
                segmentDir.resolve(VECTORS_FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
            FileChannel pointIdChannel = FileChannel.open(
                segmentDir.resolve(POINT_IDS_FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            )
        ) {
            for (VectorRecord record : records) {
                validateSlotId(record.getSlotId());
                validateVector(vectorSize, record.getVector());
                long vectorOffset =
                    (record.getSlotId() - 1L) * vectorSize * Float.BYTES;
                long pointIdOffset =
                    (record.getSlotId() - 1L) * Long.BYTES;
                writeFully(
                    vectorChannel,
                    toVectorBuffer(normalize(record.getVector())),
                    vectorOffset
                );
                writeFully(
                    pointIdChannel,
                    toLongBuffer(record.getPointId()),
                    pointIdOffset
                );
            }
            vectorChannel.force(true);
            pointIdChannel.force(true);
        }
    }

    public List<SearchResult> search(
        long collectionId,
        int vectorSize,
        float[] query,
        int limit
    ) throws IOException {
        validateVector(vectorSize, query);
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Path segmentDir = getSegmentDir(collectionId);
        Path vectorsFile = segmentDir.resolve(VECTORS_FILE);
        Path pointIdsFile = segmentDir.resolve(POINT_IDS_FILE);
        if (!Files.isRegularFile(vectorsFile)
            || !Files.isRegularFile(pointIdsFile)) {
            return Collections.emptyList();
        }
        long vectorByteSize = (long) vectorSize * Float.BYTES;
        try (
            FileChannel vectorChannel = FileChannel.open(
                vectorsFile,
                StandardOpenOption.READ
            );
            FileChannel pointIdChannel = FileChannel.open(
                pointIdsFile,
                StandardOpenOption.READ
            )
        ) {
            long vectorSlots = vectorChannel.size() / vectorByteSize;
            long pointIdSlots = pointIdChannel.size() / Long.BYTES;
            long slotCount = Math.min(vectorSlots, pointIdSlots);
            if (slotCount <= 0) {
                return Collections.emptyList();
            }
            float[] normalizedQuery = normalize(query);
            PriorityQueue<SearchResult> queue = new PriorityQueue<>(
                limit,
                Comparator.comparingDouble(SearchResult::getScore)
            );
            long maxVectorsPerChunk = Math.max(
                1L,
                MAX_MAPPED_BYTES / vectorByteSize
            );
            for (long slot = 0; slot < slotCount; slot += maxVectorsPerChunk) {
                long chunkSlots = Math.min(maxVectorsPerChunk, slotCount - slot);
                searchChunk(
                    vectorChannel,
                    pointIdChannel,
                    normalizedQuery,
                    vectorSize,
                    vectorByteSize,
                    slot,
                    chunkSlots,
                    limit,
                    queue
                );
            }
            List<SearchResult> results = new ArrayList<>(queue);
            results.sort(
                (a, b) -> Float.compare(b.getScore(), a.getScore())
            );
            return results;
        }
    }

    public long getBackfillProgress(long collectionId) throws IOException {
        Path progressFile = getSegmentDir(collectionId)
            .resolve(BACKFILL_PROGRESS_FILE);
        if (!Files.isRegularFile(progressFile)
            || Files.size(progressFile) < Long.BYTES) {
            return 0L;
        }
        try (
            FileChannel channel = FileChannel.open(
                progressFile,
                StandardOpenOption.READ
            )
        ) {
            ByteBuffer buffer = ByteBuffer
                .allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
            readFully(channel, buffer);
            buffer.flip();
            return buffer.getLong();
        }
    }

    public void saveBackfillProgress(
        long collectionId,
        long lastPointDbId
    ) throws IOException {
        Path segmentDir = getSegmentDir(collectionId);
        Files.createDirectories(segmentDir);
        try (
            FileChannel channel = FileChannel.open(
                segmentDir.resolve(BACKFILL_PROGRESS_FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        ) {
            writeFully(channel, toLongBuffer(lastPointDbId), 0L);
            channel.force(true);
        }
    }

    public Path getSegmentDir(long collectionId) {
        return dataDir
            .resolve("collections")
            .resolve(String.valueOf(collectionId))
            .resolve("segments")
            .resolve(String.format("%06d", SEGMENT_NO));
    }

    private void searchChunk(
        FileChannel vectorChannel,
        FileChannel pointIdChannel,
        float[] normalizedQuery,
        int vectorSize,
        long vectorByteSize,
        long slot,
        long chunkSlots,
        int limit,
        PriorityQueue<SearchResult> queue
    ) throws IOException {
        MappedByteBuffer vectorBuffer = vectorChannel.map(
            FileChannel.MapMode.READ_ONLY,
            slot * vectorByteSize,
            chunkSlots * vectorByteSize
        );
        MappedByteBuffer pointIdBuffer = pointIdChannel.map(
            FileChannel.MapMode.READ_ONLY,
            slot * Long.BYTES,
            chunkSlots * Long.BYTES
        );
        vectorBuffer.order(ByteOrder.LITTLE_ENDIAN);
        pointIdBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < chunkSlots; ++i) {
            long pointId = pointIdBuffer.getLong(i * Long.BYTES);
            if (pointId <= 0) {
                continue;
            }
            float score = cosineScore(
                vectorBuffer,
                i * (int) vectorByteSize,
                normalizedQuery,
                vectorSize
            );
            SearchResult result = new SearchResult(pointId, score);
            if (queue.size() < limit) {
                queue.add(result);
            } else if (score > queue.peek().getScore()) {
                queue.poll();
                queue.add(result);
            }
        }
    }

    private static float cosineScore(
        ByteBuffer buffer,
        int offset,
        float[] normalizedQuery,
        int vectorSize
    ) {
        float score = 0f;
        for (int i = 0; i < vectorSize; ++i) {
            score += normalizedQuery[i] *
                buffer.getFloat(offset + i * Float.BYTES);
        }
        return score;
    }

    private static ByteBuffer toVectorBuffer(float[] vector) {
        ByteBuffer buffer = ByteBuffer
            .allocate(vector.length * Float.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector) {
            buffer.putFloat(value);
        }
        buffer.flip();
        return buffer;
    }

    private static ByteBuffer toLongBuffer(long value) {
        ByteBuffer buffer = ByteBuffer
            .allocate(Long.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(value);
        buffer.flip();
        return buffer;
    }

    private static void writeFully(
        FileChannel channel,
        ByteBuffer buffer,
        long offset
    ) throws IOException {
        while (buffer.hasRemaining()) {
            offset += channel.write(buffer, offset);
        }
    }

    private static void readFully(
        FileChannel channel,
        ByteBuffer buffer
    ) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                throw new IOException("Unexpected end of file");
            }
        }
    }

    private static void validateVector(
        int vectorSize,
        float[] vector
    ) {
        if (vector == null || vector.length != vectorSize) {
            throw new IllegalArgumentException(
                "Vector dimension mismatch: expected " +
                    vectorSize +
                    ", actual " +
                    (vector == null ? 0 : vector.length)
            );
        }
    }

    private static void validateSlotId(long slotId) {
        if (slotId <= 0) {
            throw new IllegalArgumentException(
                "slotId must be positive: " + slotId
            );
        }
    }

    private static float[] normalize(float[] vector) {
        float norm = 0f;
        for (float value : vector) {
            norm += value * value;
        }
        norm = (float) Math.sqrt(norm);
        float[] answer = new float[vector.length];
        if (norm == 0f) {
            return answer;
        }
        for (int i = 0; i < vector.length; ++i) {
            answer[i] = vector[i] / norm;
        }
        return answer;
    }

    public static final class SearchResult {
        private final long id;
        private final float score;

        public SearchResult(long id, float score) {
            this.id = id;
            this.score = score;
        }

        public long getId() {
            return id;
        }

        public float getScore() {
            return score;
        }
    }

    public static final class VectorRecord {
        private final long slotId;
        private final long pointId;
        private final float[] vector;

        public VectorRecord(
            long slotId,
            long pointId,
            float[] vector
        ) {
            this.slotId = slotId;
            this.pointId = pointId;
            this.vector = vector;
        }

        public long getSlotId() {
            return slotId;
        }

        public long getPointId() {
            return pointId;
        }

        public float[] getVector() {
            return vector;
        }
    }
}
