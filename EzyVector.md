# Thiết kế EzyRAG Vector Database bằng Java

## 1. Mục tiêu

Tài liệu này đề xuất kiến trúc cho một Vector Database nhẹ viết bằng
Java, phục vụ EzyRAG và có thể chạy trên máy chủ tài nguyên hạn chế
nhưng vẫn có đường nâng cấp tới hàng triệu vector.

Các mục tiêu chính:

-   Không tải toàn bộ vector từ MySQL vào Java Heap khi server khởi động
    hoặc ở request đầu tiên.
-   MySQL đảm nhiệm metadata, payload, transaction và durability.
-   Vector Engine đảm nhiệm lưu trữ vector, ANN index và similarity
    search.
-   Vector/index được lưu bền vững trên disk.
-   Hỗ trợ khởi động nhanh bằng cách mở metadata/index thay vì rebuild
    từ database.
-   Hỗ trợ segment để giới hạn RAM và dễ compaction.
-   Có cơ chế recovery khi server crash.
-   Có thể hỗ trợ nhiều distance metric như Cosine, Dot Product và
    Euclidean.
-   Thiết kế đủ đơn giản để triển khai từng giai đoạn.

------------------------------------------------------------------------

## 2. Vấn đề của kiến trúc load toàn bộ vào RAM

Không nên triển khai theo mô hình:

``` text
MySQL
  |
  v
SELECT toàn bộ points
  |
  v
Map<Long, float[]>
  |
  v
Java Heap
  |
  v
Search
```

Ví dụ với 1.000.000 vector, dimension 1536 và `float32`:

``` text
1,000,000 * 1536 * 4 bytes
= 6,144,000,000 bytes
≈ 5.72 GiB
```

Đây mới chỉ là raw vector, chưa bao gồm:

-   Java object overhead.
-   `float[]` overhead.
-   `Map`/hash table.
-   HNSW graph.
-   Payload.
-   Metadata.
-   Temporary objects khi search.
-   GC overhead.

Vì vậy lazy loading toàn collection ở request đầu tiên chỉ chuyển vấn đề
từ startup sang request đầu tiên, chứ không giải quyết vấn đề.

------------------------------------------------------------------------

# 3. Kiến trúc tổng thể

Kiến trúc đề xuất:

``` text
                         EzyRAG
                            |
               +------------+------------+
               |                         |
               v                         v
        MySQL Metadata             Vector Engine
        & Point Store                  Java
               |                         |
               |                 +-------+-------+
               |                 |               |
               |                 v               v
               |          Immutable Segments  Mutable Segment
               |                 |               |
               |                 v               v
               |             Disk/mmap          RAM
               |                 |
               |                 v
               |           Persistent ANN
               |              Index
               |                 |
               +---------> Recovery / WAL
```

Phân chia trách nhiệm:

### MySQL

Lưu:

-   Collection metadata.
-   Point metadata.
-   Payload.
-   Trạng thái point.
-   Thông tin segment.
-   Trạng thái indexing/recovery.
-   Có thể giữ raw vector làm source of truth ở giai đoạn đầu.

### Vector Engine

Quản lý:

-   Vector storage.
-   Segment.
-   ANN index.
-   Distance calculation.
-   Search.
-   Merge Top-K.
-   Flush.
-   Compaction.
-   Recovery.

------------------------------------------------------------------------

# 4. Database schema đề xuất

Các bảng cũ đang ở giai đoạn nháp, vì vậy nên sửa ngay thay vì giữ
compatibility không cần thiết.

## 4.1. `ezyrag_collections`

``` sql
CREATE TABLE IF NOT EXISTS `ezyrag_collections` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(120) NOT NULL,

    `vector_size` INT UNSIGNED NOT NULL,
    `distance` VARCHAR(32) NOT NULL,

    `index_type` VARCHAR(32) NOT NULL DEFAULT 'HNSW',

    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',

    `points_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,

    `config` TEXT NULL,

    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ezyrag_collection_name` (`name`),
    KEY `idx_ezyrag_collection_status` (`status`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_520_ci;
```

`distance` có thể nhận:

``` text
COSINE
DOT
EUCLIDEAN
```

`config` có thể lưu JSON cấu hình riêng của index, ví dụ:

``` json
{
  "m": 16,
  "efConstruction": 100,
  "efSearch": 64,
  "segmentMaxPoints": 100000
}
```

Nếu MySQL version hỗ trợ JSON ổn định trong môi trường triển khai, có
thể đổi `TEXT` thành `JSON`.

------------------------------------------------------------------------

# 5. Point table

Trong giai đoạn đầu, nên giữ raw vector trong MySQL để MySQL đóng vai
trò source of truth.

``` sql
CREATE TABLE IF NOT EXISTS `ezyrag_collection_points` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    `collection_id` BIGINT UNSIGNED NOT NULL,
    `point_id` BIGINT UNSIGNED NOT NULL,

    `vector` MEDIUMBLOB NOT NULL,
    `payload` MEDIUMTEXT NULL,

    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',

    `version` BIGINT UNSIGNED NOT NULL DEFAULT 1,

    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_ezyrag_collection_point`
        (`collection_id`, `point_id`),

    KEY `idx_ezyrag_points_collection_status`
        (`collection_id`, `status`),

    KEY `idx_ezyrag_points_updated_at`
        (`updated_at`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_520_ci;
```

Không cần lưu `segment_id` và `vector_offset` trực tiếp trong point
table ở giai đoạn đầu. Hai thông tin này thuộc physical index storage và
có thể thay đổi khi compaction. Nếu ghi chúng vào point table, mỗi lần
merge segment sẽ kéo theo hàng loạt UPDATE MySQL không cần thiết.

Mapping vật lý nên nằm trong index/segment metadata.

------------------------------------------------------------------------

# 6. Segment table

Nên có bảng riêng để Vector Engine biết trạng thái các segment.

``` sql
CREATE TABLE IF NOT EXISTS `ezyrag_collection_segments` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    `collection_id` BIGINT UNSIGNED NOT NULL,

    `segment_no` BIGINT UNSIGNED NOT NULL,

    `segment_type` VARCHAR(32) NOT NULL,
    `status` VARCHAR(32) NOT NULL,

    `points_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,

    `min_point_id` BIGINT UNSIGNED NULL,
    `max_point_id` BIGINT UNSIGNED NULL,

    `index_version` BIGINT UNSIGNED NOT NULL DEFAULT 1,

    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_ezyrag_collection_segment`
        (`collection_id`, `segment_no`),

    KEY `idx_ezyrag_segment_collection_status`
        (`collection_id`, `status`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_520_ci;
```

Ví dụ `segment_type`:

``` text
MUTABLE
IMMUTABLE
```

Ví dụ `status`:

``` text
BUILDING
ACTIVE
COMPACTING
OBSOLETE
CORRUPTED
```

Không nên lưu absolute filesystem path vào database. Storage path nên
được suy ra từ `collection_id`, `segment_no` và cấu hình data directory
để hệ thống có thể di chuyển sang máy khác.

------------------------------------------------------------------------

# 7. Operation log / recovery table

Đây là phần quan trọng để tránh MySQL và Vector Index lệch nhau khi
server crash.

``` sql
CREATE TABLE IF NOT EXISTS `ezyrag_index_operations` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    `collection_id` BIGINT UNSIGNED NOT NULL,
    `point_id` BIGINT UNSIGNED NOT NULL,

    `operation` VARCHAR(16) NOT NULL,
    `point_version` BIGINT UNSIGNED NOT NULL,

    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',

    `created_at` DATETIME NOT NULL,
    `processed_at` DATETIME NULL,

    PRIMARY KEY (`id`),

    KEY `idx_ezyrag_operation_pending`
        (`status`, `id`),

    KEY `idx_ezyrag_operation_collection`
        (`collection_id`, `id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_520_ci;
```

Operation:

``` text
UPSERT
DELETE
```

Status:

``` text
PENDING
PROCESSING
DONE
FAILED
```

`point_version` giúp operation có tính idempotent và tránh một operation
cũ ghi đè dữ liệu mới hơn.

------------------------------------------------------------------------

# 8. Disk layout

Ví dụ:

``` text
data/
└── ezyrag/
    └── collections/
        └── 12/
            ├── collection.meta
            ├── segments/
            │   ├── 000001/
            │   │   ├── segment.meta
            │   │   ├── vectors.dat
            │   │   ├── point_ids.dat
            │   │   ├── hnsw.dat
            │   │   └── deleted.dat
            │   │
            │   ├── 000002/
            │   │   ├── segment.meta
            │   │   ├── vectors.dat
            │   │   ├── point_ids.dat
            │   │   ├── hnsw.dat
            │   │   └── deleted.dat
            │   │
            │   └── ...
            │
            └── temp/
```

`vectors.dat` chứa raw float liên tục:

``` text
vector 0
vector 1
vector 2
...
```

Nếu dimension = `D`:

``` text
vectorByteSize = D * 4
```

Offset của local vector thứ `n`:

``` text
offset = n * D * 4
```

Nhờ đó không cần tạo hàng triệu `float[]`.

------------------------------------------------------------------------

# 9. Memory-mapped vector storage

Java có thể dùng:

``` java
FileChannel channel = FileChannel.open(
    path,
    StandardOpenOption.READ
);

MappedByteBuffer buffer = channel.map(
    FileChannel.MapMode.READ_ONLY,
    0,
    channel.size()
);

buffer.order(ByteOrder.LITTLE_ENDIAN);
```

Không nên giữ:

``` java
Map<Long, float[]> vectors;
```

cho toàn collection.

Mmap cho phép OS quyết định page nào cần nằm trong RAM:

``` text
SSD / Disk
    |
    v
Mapped File
    |
    v
OS Page Cache
    |
    v
Vector Search
```

Java Heap chủ yếu giữ:

-   collection metadata;
-   segment metadata;
-   index structures cần thiết;
-   query;
-   candidate list;
-   mutable segment.

------------------------------------------------------------------------

# 10. Segmented storage

Không tạo một file/index khổng lồ cho toàn collection.

Ví dụ:

``` text
Collection 12

Segment 1
100,000 vectors

Segment 2
100,000 vectors

Segment 3
100,000 vectors

...

Mutable Segment
4,281 vectors
```

Một cấu hình ban đầu hợp lý:

``` text
segmentMaxPoints = 50,000 - 200,000
```

Không nên hard-code con số này; nó phải là collection/index
configuration.

------------------------------------------------------------------------

# 11. Mutable và immutable segment

## Mutable segment

Nhận dữ liệu mới.

Có thể giữ một lượng vector/index nhỏ trong RAM hoặc dùng storage
appendable.

``` text
INSERT
  |
  v
MySQL transaction
  |
  v
Index operation
  |
  v
Mutable Segment
```

## Immutable segment

Khi mutable segment đạt threshold:

``` text
Mutable
   |
   | freeze
   v
BUILDING segment
   |
   | build persistent ANN index
   v
ACTIVE immutable segment
```

Sau khi immutable:

-   Không thêm point trực tiếp.
-   Search được.
-   Có thể mmap.
-   Có thể tham gia compaction.

------------------------------------------------------------------------

# 12. Search flow

Ví dụ query:

``` java
search(collectionId, queryVector, 10);
```

Luồng:

``` text
Query Vector
     |
     +-------------------------+
     |            |            |
     v            v            v
 Segment 1     Segment 2    Mutable
     |            |            |
   Top N        Top N        Top N
     |            |            |
     +------------+------------+
                  |
                  v
              Merge Heap
                  |
                  v
                Top K
                  |
                  v
          load payload by IDs
                  |
                  v
               Result
```

Mỗi segment nên trả nhiều hơn `K` một chút nếu cần để merge.

Không nên load payload của mọi candidate ngay trong ANN traversal. Chỉ
lấy payload cho kết quả cuối cùng.

------------------------------------------------------------------------

# 13. Top-K merge

Có thể dùng `PriorityQueue`.

``` java
PriorityQueue<VectorSearchResult> queue =
    new PriorityQueue<>(
        Comparator.comparingDouble(
            VectorSearchResult::getScore
        )
    );
```

Mỗi segment trả candidate và manager merge thành Top-K toàn collection.

Cần chuẩn hóa semantic của score giữa các distance metric, ví dụ API
luôn trả `score` theo hướng càng lớn càng tốt, còn distance nội bộ được
convert trước khi merge.

------------------------------------------------------------------------

# 14. Insert flow

Không nên:

``` text
INSERT MySQL
    |
update index ngay trong cùng logic
```

vì crash giữa hai bước có thể gây inconsistency.

Nên dùng transaction:

``` text
BEGIN

UPSERT ezyrag_collection_points

INSERT ezyrag_index_operations
    operation = UPSERT
    status = PENDING

COMMIT
```

Sau đó Index Worker:

``` text
PENDING operation
       |
       v
read point/version
       |
       v
update mutable index
       |
       v
durably flush/checkpoint
       |
       v
operation = DONE
```

Điểm quan trọng: chỉ đánh dấu `DONE` sau khi thay đổi index đã durable.
Nếu process chết trước đó, operation được replay.

------------------------------------------------------------------------

# 15. Delete flow

Không nên xóa vật lý ngay khỏi immutable segment.

MySQL:

``` text
point.status = DELETED
```

và:

``` text
operation = DELETE
```

Vector Engine ghi tombstone:

``` text
deleted.dat
```

Search bỏ qua các point đã delete.

Sau này compaction sẽ loại chúng vật lý.

------------------------------------------------------------------------

# 16. Update vector

Update có thể coi là:

``` text
UPSERT new version
```

Point:

``` text
point_id = 100
version = 5
```

Operation:

``` text
UPSERT
point_id = 100
point_version = 5
```

Nếu worker gặp operation:

``` text
version = 4
```

trong khi index đã có:

``` text
version = 5
```

thì bỏ qua operation cũ.

Nhờ vậy recovery có thể idempotent.

------------------------------------------------------------------------

# 17. Startup flow

Đây là phần giải quyết trực tiếp vấn đề ban đầu.

Không làm:

``` text
SELECT *
FROM ezyrag_collection_points
```

Không làm:

``` text
build toàn bộ HNSW
```

Startup chỉ:

``` text
1. Load active collections.
2. Load segment metadata.
3. Validate segment files.
4. Open/mmap active immutable segments.
5. Open mutable segment.
6. Recover unfinished operations.
7. Server ready.
```

Pseudo-code:

``` java
public void start() {
    List<RagCollection> collections =
        collectionRepository.findActive();

    for (RagCollection collection : collections) {
        indexManager.open(collection);
    }

    recoveryManager.recover();
}
```

`open()` không đọc hàng triệu vector thành Java object.

------------------------------------------------------------------------

# 18. Recovery khi crash

Giả sử:

``` text
MySQL COMMIT
    |
    v
operation PENDING
    |
SERVER CRASH
```

Sau restart:

``` sql
SELECT *
FROM ezyrag_index_operations
WHERE status <> 'DONE'
ORDER BY id
LIMIT ?;
```

Worker replay.

Đối với operation bị kẹt ở `PROCESSING`, startup có thể reset về
`PENDING`, hoặc dùng lease/timeout để reclaim.

------------------------------------------------------------------------

# 19. ANN index

Giai đoạn đầu có thể triển khai theo hai bước.

## Bước 1 - Exact Search

Segment nhỏ có thể brute-force:

``` text
query
  |
  v
scan vectors.dat
  |
calculate cosine/dot/L2
  |
Top K
```

Ưu điểm:

-   Dễ kiểm tra correctness.
-   Dùng làm baseline benchmark.
-   Dùng để test persistent storage.
-   Dùng cho collection nhỏ.

## Bước 2 - HNSW

Sau khi storage/segment/recovery ổn định, thêm:

``` text
HNSW
```

HNSW graph cũng phải persist:

``` text
hnsw.dat
```

Nếu restart phải rebuild HNSW từ MySQL thì persistent storage vẫn chưa
hoàn chỉnh.

------------------------------------------------------------------------

# 20. Vector binary format

Nên chuẩn hóa ngay từ đầu.

Ví dụ:

``` text
float32
little-endian
```

Java:

``` java
ByteBuffer buffer = ByteBuffer
    .allocate(vector.length * Float.BYTES)
    .order(ByteOrder.LITTLE_ENDIAN);

for (float value : vector) {
    buffer.putFloat(value);
}
```

Không dùng Java Serialization cho vector/index file.

Format file phải độc lập với Java object serialization để sau này có thể
thay implementation mà vẫn đọc được dữ liệu cũ.

------------------------------------------------------------------------

# 21. File header

Mỗi file persistent nên có header.

Ví dụ:

``` text
MAGIC
FORMAT_VERSION
COLLECTION_ID
SEGMENT_ID
VECTOR_SIZE
POINT_COUNT
CHECKSUM
```

Ví dụ conceptual:

``` text
EZVR
1
12
4
1536
100000
...
```

Mục đích:

-   Phát hiện file sai.
-   Phát hiện dimension mismatch.
-   Upgrade format.
-   Recovery.
-   Integrity validation.

------------------------------------------------------------------------

# 22. Checksum

Nên có checksum cho immutable segment/index.

Ví dụ:

``` text
segment.meta
    |
    +-- vectors checksum
    +-- point_ids checksum
    +-- hnsw checksum
```

Nếu checksum sai:

``` text
status = CORRUPTED
```

Sau đó rebuild segment từ source of truth trong MySQL.

------------------------------------------------------------------------

# 23. Compaction

Theo thời gian:

``` text
Segment 1: 100k
Segment 2: 100k
Segment 3: 100k
```

có thể có:

-   Deleted points.
-   Updated points.
-   Nhiều segment nhỏ.

Compaction:

``` text
Segment A
Segment B
Segment C
    |
    v
read active latest points
    |
    v
build new segment D
    |
    v
fsync
    |
    v
atomic activate D
    |
    v
mark A/B/C OBSOLETE
    |
    v
delete old files later
```

Không overwrite segment đang ACTIVE trực tiếp.

------------------------------------------------------------------------

# 24. Atomic segment publish

Khi build:

``` text
temp/segment-10-building/
```

Sau khi:

-   vectors complete;
-   IDs complete;
-   HNSW complete;
-   checksum complete;
-   fsync complete;

mới publish:

``` text
segments/000010/
```

và cập nhật metadata.

Điều này tránh server crash để lại một segment nửa hoàn chỉnh nhưng bị
coi là usable.

------------------------------------------------------------------------

# 25. Java interfaces

## VectorIndex

``` java
public interface VectorIndex {

    void upsert(
        long pointId,
        long version,
        float[] vector
    );

    void remove(
        long pointId,
        long version
    );

    List<VectorSearchResult> search(
        float[] query,
        int limit
    );

    void flush();

    void close();
}
```

------------------------------------------------------------------------

## VectorSegment

``` java
public interface VectorSegment {

    long getId();

    long size();

    boolean isMutable();

    List<VectorSearchResult> search(
        float[] query,
        int limit
    );

    void close();
}
```

------------------------------------------------------------------------

## MutableVectorSegment

``` java
public interface MutableVectorSegment
        extends VectorSegment {

    void upsert(
        long pointId,
        long version,
        float[] vector
    );

    void remove(
        long pointId,
        long version
    );

    void flush();
}
```

------------------------------------------------------------------------

## VectorIndexManager

``` java
public interface VectorIndexManager {

    void open(RagCollection collection);

    void close(long collectionId);

    void upsert(
        long collectionId,
        long pointId,
        long version,
        float[] vector
    );

    void remove(
        long collectionId,
        long pointId,
        long version
    );

    List<VectorSearchResult> search(
        long collectionId,
        float[] query,
        int limit
    );
}
```

------------------------------------------------------------------------

# 26. Distance abstraction

``` java
public interface VectorDistance {

    float calculate(
        float[] a,
        float[] b
    );
}
```

Implement:

``` text
CosineDistance
DotProductDistance
EuclideanDistance
```

Có thể dùng enum/factory:

``` java
public enum VectorDistanceType {
    COSINE,
    DOT,
    EUCLIDEAN
}
```

------------------------------------------------------------------------

# 27. Collection runtime

``` java
public class VectorCollectionRuntime {

    private final long collectionId;

    private final int vectorSize;

    private final VectorDistanceType distance;

    private final List<VectorSegment> immutableSegments;

    private MutableVectorSegment mutableSegment;
}
```

Không chứa:

``` java
Map<Long, float[]> allVectors;
```

------------------------------------------------------------------------

# 28. Index worker

``` text
             MySQL
               |
               v
       PENDING operations
               |
               v
          IndexWorker
               |
       +-------+-------+
       |               |
       v               v
    UPSERT           DELETE
       |               |
       +-------+-------+
               |
               v
        Mutable Segment
               |
               v
             flush
               |
               v
         mark operation
              DONE
```

Worker nên xử lý batch:

``` sql
SELECT ...
FROM ezyrag_index_operations
WHERE status = 'PENDING'
ORDER BY id
LIMIT 1000;
```

Không xử lý từng operation bằng một DB round-trip nếu throughput cao.

------------------------------------------------------------------------

# 29. Payload

Payload không cần nằm trong ANN index nếu chưa có nhu cầu filter phức
tạp.

Search:

``` text
ANN
 |
 v
point IDs
 |
 v
MySQL batch query
 |
 v
payload
```

Ví dụ:

``` sql
SELECT point_id, payload
FROM ezyrag_collection_points
WHERE collection_id = ?
  AND point_id IN (...);
```

Chỉ load payload cho Top-K hoặc một lượng candidate nhỏ.

Sau này nếu cần filter trước ANN search, mới xây payload/filter index
riêng.

------------------------------------------------------------------------

# 30. Cache

Không nên tự cache toàn bộ vector.

Có thể cache:

-   Collection metadata.
-   Segment metadata.
-   Frequently accessed payload.
-   Small query/result cache nếu thực sự cần.

Raw immutable vector nên ưu tiên:

``` text
mmap + OS page cache
```

thay vì:

``` text
Java HashMap cache
```

------------------------------------------------------------------------

# 31. Direct memory

Nếu chưa dùng mmap cho mutable data, có thể cân nhắc:

``` java
ByteBuffer.allocateDirect(...)
```

thay cho hàng loạt:

``` java
float[]
```

Nhưng direct memory không giải quyết persistence.

Vì vậy:

``` text
DirectByteBuffer
```

không phải phương án thay thế cho disk-backed segment.

------------------------------------------------------------------------

# 32. Dimension validation

Ngay khi upsert:

``` java
if (vector.length != collection.getVectorSize()) {
    throw new IllegalArgumentException(
        "Vector dimension mismatch: expected "
            + collection.getVectorSize()
            + ", got "
            + vector.length
    );
}
```

Không để dimension sai đi vào operation queue hoặc segment.

------------------------------------------------------------------------

# 33. Collection lifecycle

Nên định nghĩa rõ:

``` text
CREATING
ACTIVE
DELETING
DELETED
ERROR
```

Create:

``` text
create DB metadata
    |
create data directory
    |
create mutable segment
    |
ACTIVE
```

Delete:

``` text
DELETING
   |
stop accepting writes
   |
close runtime
   |
delete segment files
   |
delete/retain point records theo policy
   |
DELETED
```

------------------------------------------------------------------------

# 34. Không dùng database foreign key bắt buộc

Có thể thêm foreign key, nhưng với một storage engine cần bulk operation
và lifecycle riêng, không bắt buộc phải dựa vào FK.

Nếu codebase hiện tại ưu tiên application-level integrity thì index đúng
và repository/service validation thường linh hoạt hơn.

Quan trọng nhất vẫn là:

``` text
collection_id + point_id UNIQUE
```

và consistency được quản lý ở service layer.

------------------------------------------------------------------------

# 35. Pagination khi rebuild

Trong trường hợp cần rebuild một collection từ MySQL, tuyệt đối không:

``` sql
SELECT *
FROM ezyrag_collection_points
WHERE collection_id = ?;
```

Nên keyset pagination:

``` sql
SELECT id,
       point_id,
       vector,
       version
FROM ezyrag_collection_points
WHERE collection_id = ?
  AND status = 'ACTIVE'
  AND id > ?
ORDER BY id
LIMIT 1000;
```

Sau batch:

``` text
lastId = max(id)
```

rồi đọc batch tiếp theo.

Không dùng:

``` sql
LIMIT 1000 OFFSET 1000000
```

với dữ liệu lớn.

------------------------------------------------------------------------

# 36. Rebuild index

Cần hỗ trợ command/service:

``` text
rebuild(collectionId)
```

Luồng:

``` text
MySQL
  |
keyset pagination
  |
  v
new temporary segments
  |
build ANN
  |
validate
  |
atomic swap
  |
new ACTIVE segments
```

Trong quá trình rebuild, index cũ vẫn có thể phục vụ search nếu thiết kế
runtime cho phép.

------------------------------------------------------------------------

# 37. Metrics cần theo dõi

Nên có tối thiểu:

``` text
collection_count

point_count

segment_count

mutable_segment_points

pending_index_operations

failed_index_operations

search_latency_ms

search_candidate_count

index_operation_latency_ms

segment_build_time_ms

compaction_time_ms

recovery_time_ms
```

Với HNSW:

``` text
hnsw_ef_search
hnsw_m
```

cũng nên xuất ra diagnostics.

------------------------------------------------------------------------

# 38. Cấu hình đề xuất

Ví dụ:

``` properties
ezyrag.vector.data_dir=data/ezyrag

ezyrag.vector.segment_max_points=100000

ezyrag.vector.index_type=HNSW

ezyrag.vector.hnsw.m=16
ezyrag.vector.hnsw.ef_construction=100
ezyrag.vector.hnsw.ef_search=64

ezyrag.vector.operation_batch_size=500

ezyrag.vector.compaction.enabled=true
```

Các giá trị trên chỉ là default khởi đầu; cần benchmark theo dimension,
RAM, CPU và dataset thực tế.

------------------------------------------------------------------------

# 39. Lộ trình triển khai

Không nên viết HNSW ngay từ đầu.

## Phase 1 - Persistent Vector Storage

Làm:

``` text
collection
segment
vectors.dat
point_ids.dat
mmap
exact search
```

Mục tiêu:

> Restart server mà không load toàn bộ vector từ MySQL.

------------------------------------------------------------------------

## Phase 2 - Recovery

Thêm:

``` text
index_operations
version
UPSERT
DELETE
replay
```

Mục tiêu:

> Crash không làm mất đồng bộ giữa MySQL và vector storage.

------------------------------------------------------------------------

## Phase 3 - Immutable/Mutable Segment

Thêm:

``` text
mutable segment
freeze
immutable segment
atomic publish
```

Mục tiêu:

> Có thể xử lý hàng triệu vector theo từng segment.

------------------------------------------------------------------------

## Phase 4 - HNSW

Thêm persistent:

``` text
hnsw.dat
```

Mục tiêu:

> Không phải brute-force toàn bộ vector.

------------------------------------------------------------------------

## Phase 5 - Compaction

Thêm:

``` text
tombstone
merge segments
remove obsolete versions
```

------------------------------------------------------------------------

## Phase 6 - Filtering / Optimization

Sau khi core ổn định mới thêm:

``` text
payload filtering
quantization
parallel segment search
hot segment cache
SIMD/vector API
replication
snapshot
backup
```

------------------------------------------------------------------------

# 40. Kiến trúc cuối cùng

``` text
                         Client
                            |
                            v
                    RagVectorService
                            |
                  +---------+---------+
                  |                   |
                  v                   v
              Point Store      VectorIndexManager
                MySQL                  |
                  |            +-------+-------+
                  |            |               |
                  |            v               v
                  |      Collection A     Collection B
                  |            |
                  |       +----+----+----------------+
                  |       |         |                |
                  |       v         v                v
                  |     Seg 1     Seg 2          Mutable
                  |       |         |                |
                  |       v         v                v
                  |     mmap      mmap             RAM/
                  |       |         |             append
                  |       v         v
                  |     HNSW      HNSW
                  |       |         |
                  |       +----+----+
                  |            |
                  |            v
                  |        Top-K Merge
                  |            |
                  +----------->|
                               v
                         Payload Fetch
                               |
                               v
                            Result
```

Write path:

``` text
                  UPSERT / DELETE
                         |
                         v
                   MySQL Transaction
                         |
              +----------+----------+
              |                     |
              v                     v
           Point Row          Operation Row
                                    |
                                  COMMIT
                                    |
                                    v
                              Index Worker
                                    |
                                    v
                            Mutable Segment
                                    |
                              durable flush
                                    |
                                    v
                              Operation DONE
```

Startup:

``` text
Server Start
    |
    v
Load Collections
    |
    v
Load Segment Metadata
    |
    v
Open / mmap Segment Files
    |
    v
Open Mutable Segments
    |
    v
Replay unfinished operations
    |
    v
Ready
```

------------------------------------------------------------------------

# 41. Kết luận

Thay đổi quan trọng nhất là không coi Vector Database là:

``` text
MySQL + load toàn bộ vector vào Java Heap
```

mà là:

``` text
MySQL
+
Persistent Vector Storage
+
Persistent ANN Index
+
Segment Manager
+
Recovery Log
```

MySQL chịu trách nhiệm:

``` text
durability
metadata
payload
transaction
source of truth
```

Vector Engine chịu trách nhiệm:

``` text
vector storage
ANN index
distance search
segment
Top-K
compaction
```

Với kiến trúc này, server restart không cần load hàng triệu bản ghi
thành Java objects. Nó chỉ mở collection metadata, segment metadata và
các persistent index/file cần thiết. Raw vector có thể được truy cập qua
memory mapping và OS page cache.

Đối với EzyRAG, thứ tự triển khai hợp lý nhất là:

``` text
Persistent Segment Storage
        ↓
mmap
        ↓
Exact Search
        ↓
Operation Log + Recovery
        ↓
Mutable / Immutable Segment
        ↓
Persistent HNSW
        ↓
Compaction
        ↓
Filtering / Quantization / Optimization
```

Cách triển khai này giữ phần đầu đủ đơn giản để kiểm chứng correctness,
đồng thời không khóa kiến trúc vào mô hình phải giữ toàn bộ vector trong
Java Heap khi dữ liệu tăng lên hàng triệu point.

File file = fileSystemManager.concatWithEzyHomeToFile(
    data/ezyvector
);


Chưa phải 100%.
Hiện tại đã có bản chạy được về mặt thiết kế chính:
- vector raw nằm trên disk;
- không load toàn bộ vector vào heap để exact search;
- có backfill batch;
- có hnsw.dat;
- search ưu tiên HNSW khi ready;
- fallback exact scan khi HNSW chưa ready.
  Nhưng để gọi là hoàn thiện production 100% thì còn thiếu:
- recovery/operation log cho crash giữa lúc DB đã commit nhưng file/HNSW chưa ghi xong;
- delete vector/tombstone;
- segment thật sự nhiều segment + freeze/compaction, hiện mới dùng segment 000001;
- checksum/header cho vectors.dat, point_ids.dat, hnsw.dat;
- migration SQL cho bảng cũ bằng ALTER TABLE;
- benchmark/tuning M, efConstruction, efSearch;
- test build/integration/corruption/restart.