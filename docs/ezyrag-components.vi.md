EzyRAG là lớp truy hồi tri thức dành cho hệ sinh thái EzyPlatform. Hệ thống tiếp nhận nội dung của website, chuyển nội dung thành vector để tìm kiếm theo ngữ nghĩa, sau đó cung cấp các đoạn liên quan cho EzyAI hoặc thành phần AI khác.

EzyRAG tập trung vào phần **Retrieval** của Retrieval-Augmented Generation. Việc gọi mô hình ngôn ngữ và sinh câu trả lời cuối cùng thuộc trách nhiệm của EzyAI.

# Kiến trúc tổng thể

EzyRAG gồm bốn module chính:

- **SDK lõi** định nghĩa mô hình dữ liệu, các abstraction và pipeline RAG dùng chung.
- **Admin plugin** cung cấp chức năng cấu hình và quản trị dữ liệu.
- **Web plugin** đưa các thành phần RAG vào web runtime.
- **Socket plugin** kết nối nguồn tri thức RAG với EzyAI và luồng chat thời gian thực.

```mermaid
flowchart TB
    subgraph Platform["EzyPlatform"]
        ADMIN["Admin plugin<br/>Cấu hình và quản trị"]
        WEB["Web plugin<br/>Web runtime"]
        SOCKET["Socket plugin<br/>Chat thời gian thực"]
        AI["EzyAI<br/>Sinh câu trả lời"]
    end

    subgraph CORE["EzyRAG SDK"]
        PIPELINE["Pipeline RAG"]
        MODELS["Mô hình dữ liệu"]
        EXTENSIONS["Các điểm mở rộng"]
    end

    CONTENT["Bài viết, sản phẩm,<br/>media và văn bản"]
    EMBEDDING["Dịch vụ embedding"]
    VECTOR["Cơ sở dữ liệu vector"]
    DATABASE["Cơ sở dữ liệu ứng dụng"]

    ADMIN --> CORE
    WEB --> CORE
    SOCKET --> CORE
    CONTENT --> PIPELINE
    PIPELINE --> EMBEDDING
    PIPELINE --> VECTOR
    PIPELINE --> DATABASE
    CORE --> AI
```

# SDK lõi

SDK điều phối hai luồng chính: lập chỉ mục dữ liệu và tìm kiếm tri thức. Những thành phần phụ thuộc môi trường được cung cấp bởi từng plugin, trong khi thuật toán và hợp đồng xử lý được dùng chung.

```mermaid
flowchart LR
    CLIENT["Bộ điều phối RAG"]
    CLIENT --> LOADER["Data loader"]
    CLIENT --> CLEANER["Text cleaner"]
    CLIENT --> CHUNKER["Data chunker"]
    CLIENT --> EMBEDDING["Embedding service"]
    CLIENT --> VECTOR["Vector database service"]
    CLIENT --> RETRIEVER["Data retriever"]
    CLIENT --> BUILDER["Knowledge data builder"]
    CLIENT --> SETTINGS["Setting service"]
```

# Nguồn dữ liệu và bộ nạp

Data loader chuyển từng loại nguồn thành đầu vào văn bản thống nhất cho pipeline.

| Nguồn | Nội dung được lập chỉ mục | Metadata đi kèm |
|---|---|---|
| Văn bản trực tiếp | Nội dung do người quản trị nhập | Metadata của yêu cầu |
| Bài viết | Tiêu đề và nội dung | Tiêu đề, slug, tóm tắt |
| Sản phẩm | Tên, mã, mô tả và nội dung đa ngôn ngữ | Tên, slug, mã sản phẩm, giá, tiền tệ |
| Media | Thuộc tính mô tả và nội dung tài liệu | Tiêu đề, URL tài nguyên |

Một nguồn có thể sinh nhiều đầu vào. Ví dụ, sản phẩm đa ngôn ngữ có thể tạo một đầu vào cho nội dung mặc định và các đầu vào tiếp theo cho từng bản dịch.

```mermaid
flowchart LR
    SOURCE["Nguồn dữ liệu"] --> SELECT{"Loại nguồn"}
    SELECT -->|Văn bản| TEXT["Text loader"]
    SELECT -->|Bài viết| POST["Post loader"]
    SELECT -->|Sản phẩm| PRODUCT["Product loader"]
    SELECT -->|Media| MEDIA["Media loader"]
    TEXT --> INPUT["RAG input"]
    POST --> INPUT
    PRODUCT --> INPUT
    MEDIA --> INPUT
```

# Bộ đọc tài liệu

Với nguồn media, hệ thống chọn bộ đọc theo MIME type hoặc phần mở rộng của tệp. Các định dạng được hỗ trợ gồm PDF, Word `.docx`, Excel `.xlsx`, PowerPoint `.pptx`, văn bản thuần và Markdown.

```mermaid
flowchart TD
    MEDIA["Media"] --> INFO["Đọc tiêu đề và mô tả"]
    MEDIA --> FILE{"Định dạng tài liệu"}
    FILE -->|PDF| PDF["PDF reader"]
    FILE -->|DOCX| WORD["Word reader"]
    FILE -->|XLSX| EXCEL["Excel reader"]
    FILE -->|PPTX| PPT["PowerPoint reader"]
    FILE -->|Text hoặc Markdown| TEXT["Text reader"]
    PDF --> BLOCKS["Các khối văn bản"]
    WORD --> BLOCKS
    EXCEL --> BLOCKS
    PPT --> BLOCKS
    TEXT --> BLOCKS
    INFO --> INPUTS["Danh sách RAG input"]
    BLOCKS --> INPUTS
```

# Làm sạch và chia đoạn

Text cleaner chuẩn hóa Unicode, kiểu xuống dòng và khoảng trắng; đồng thời loại bỏ các ký tự điều khiển không cần thiết và dòng trống lặp lại.

Chunker sử dụng chiến lược phân cấp. HTML được chuyển thành văn bản nhưng vẫn cố gắng giữ các ranh giới có ý nghĩa. Nội dung sau đó được chia theo đoạn văn, theo câu và cuối cùng theo số ký tự nếu không còn ranh giới tự nhiên phù hợp.

```mermaid
flowchart TD
    RAW["Văn bản đầu vào"] --> CLEAN["Chuẩn hóa văn bản"]
    CLEAN --> HTML["Loại bỏ HTML<br/>Giữ ranh giới khối"]
    HTML --> CHECK{"Nằm trong giới hạn?"}
    CHECK -->|Có| SINGLE["Tạo một chunk"]
    CHECK -->|Không| PARA["Chia và ghép theo đoạn văn"]
    PARA --> LONG_PARA{"Đoạn quá dài?"}
    LONG_PARA -->|Không| CHUNKS["Danh sách chunk"]
    LONG_PARA -->|Có| SENTENCE["Chia và ghép theo câu"]
    SENTENCE --> LONG_SENTENCE{"Câu vẫn quá dài?"}
    LONG_SENTENCE -->|Không| CHUNKS
    LONG_SENTENCE -->|Có| HARD["Cắt theo số ký tự"]
    HARD --> CHUNKS
```

Giới hạn chunk, dấu phân cách đoạn và biểu thức nhận biết ranh giới câu đều có thể cấu hình. Thuật toán hiện giới hạn theo số ký tự, không phải số token.

# Dịch vụ embedding

Embedding service chuyển nội dung hoặc câu hỏi thành vector số. Triển khai hiện tại hỗ trợ OpenAI và cho phép cấu hình model. Kích thước vector được lấy từ collection đích.

Embedding service được đặt sau một interface và manager, vì vậy có thể bổ sung nhà cung cấp khác mà không thay đổi pipeline chính.

# Cơ sở dữ liệu vector và collection

EzyRAG hiện có adapter cho hai hệ thống lưu trữ vector:

- EzyVector.
- Qdrant.

Vector database service cung cấp các thao tác tạo collection, upsert, xóa point và tìm kiếm vector gần nhất. Collection lưu tên, dịch vụ vector, kích thước vector và trạng thái hoạt động. Mỗi dịch vụ vector có thể có một collection mặc định.

```mermaid
flowchart LR
    CHUNK["Data chunk"] --> EMB["Embedding"]
    EMB --> POINT["Vector point"]
    POINT --> VECTOR_DB["Kho vector<br/>Vector và payload tối thiểu"]
    CHUNK --> APP_DB["Cơ sở dữ liệu ứng dụng<br/>Nội dung, hash và metadata"]
    VECTOR_DB -. "chunk ID" .-> APP_DB
```

# Kho chunk và metadata

EzyRAG tách nội dung nghiệp vụ khỏi kho vector:

- Kho vector giữ vector, ID chunk và payload tối thiểu phục vụ tìm kiếm.
- Cơ sở dữ liệu ứng dụng giữ nội dung, hash, embedding và metadata của chunk.

Metadata có thể chứa tiêu đề, slug, tóm tắt, URL tài nguyên, mã sản phẩm, giá và đơn vị tiền tệ. Sau khi tìm kiếm vector, hệ thống dùng ID chunk để đọc lại nội dung và metadata đầy đủ.

# Pipeline lập chỉ mục

```mermaid
sequenceDiagram
    participant A as Admin hoặc ứng dụng
    participant R as EzyRAG
    participant L as Data loader
    participant C as Cleaner và chunker
    participant D as Cơ sở dữ liệu
    participant E as Embedding service
    participant V as Kho vector

    A->>R: Lập chỉ mục nguồn vào collection
    R->>L: Nạp dữ liệu theo loại nguồn
    L-->>R: Một hoặc nhiều đầu vào
    loop Mỗi đầu vào
        R->>C: Làm sạch và chia đoạn
        C-->>R: Danh sách chunk
        loop Mỗi chunk
            R->>D: Tìm chunk cũ theo nguồn và vị trí
            D-->>R: Hash và embedding hiện có
            R->>D: Lưu nội dung và metadata
            alt Nội dung mới hoặc đã thay đổi
                R->>E: Tạo embedding
                E-->>R: Vector
                R->>D: Lưu embedding
            else Nội dung không đổi
                R->>R: Tái sử dụng embedding
            end
            R->>V: Upsert vector point
        end
    end
    R->>D: Xóa các chunk cũ nằm ngoài phiên bản mới
```

Mỗi chunk được tính hash từ nội dung. Với nguồn có định danh ổn định, hệ thống tái sử dụng embedding khi hash không đổi. Văn bản nhập trực tiếp hiện tạo chunk mới thay vì tìm lại embedding theo nguồn và vị trí.

Khi phiên bản mới có ít chunk hơn, các chunk dư được xóa khỏi cơ sở dữ liệu ứng dụng. Luồng này không thể hiện việc xóa đồng thời các vector point dư khỏi vector database, vì vậy đây là điểm cần lưu ý khi đồng bộ dữ liệu.

# Pipeline tìm kiếm

Câu hỏi được chuẩn hóa, chuyển thành embedding và tìm kiếm trong collection. Kết quả vector chỉ ra các chunk phù hợp; data retriever đọc nội dung và metadata từ cơ sở dữ liệu, sau đó knowledge builder chuyển chúng thành dữ liệu tri thức cho EzyAI.

```mermaid
sequenceDiagram
    participant U as Người dùng
    participant AI as EzyAI
    participant R as EzyRAG
    participant Q as Query processor
    participant E as Embedding service
    participant V as Kho vector
    participant D as Cơ sở dữ liệu

    U->>AI: Gửi câu hỏi
    AI->>R: Tìm tri thức
    R->>Q: Chuẩn hóa câu hỏi
    Q-->>R: Truy vấn đã xử lý
    R->>E: Tạo embedding truy vấn
    E-->>R: Vector truy vấn
    R->>V: Tìm các vector gần nhất
    V-->>R: Danh sách chunk ID
    R->>D: Lấy chunk và metadata
    D-->>R: Các tài liệu liên quan
    R-->>AI: Knowledge data
    AI-->>U: Sinh câu trả lời có ngữ cảnh
```

Nếu dịch vụ vector hoặc collection không được truyền vào truy vấn, hệ thống dùng cấu hình mặc định. Nếu cấu hình hoặc collection không tồn tại, nguồn tri thức trả về danh sách rỗng.

# Data retriever và knowledge builder

Data retriever dùng ID từ kết quả tìm kiếm vector để lấy chunk và metadata trong cơ sở dữ liệu ứng dụng. Knowledge builder sau đó chuyển tài liệu thành cấu trúc mà EzyAI hiểu được.

Ngoài tìm kiếm vector, nguồn tri thức còn hỗ trợ lấy nội dung trực tiếp theo loại nguồn và ID hoặc mã. Khi một nguồn có nhiều chunk, chúng được ghép theo thứ tự để tái tạo nội dung đầy đủ.

# Cấu hình và khả năng mở rộng

Các giai đoạn chính đều được quản lý qua interface và manager:

- Data loader.
- Text cleaner.
- Data chunker.
- Embedding service.
- Query processor.
- Vector database service.
- Data retriever.
- Knowledge data builder.

```mermaid
flowchart TB
    CONFIG["Cấu hình"] --> MANAGERS["Các manager"]
    MANAGERS --> L["Data loader"]
    MANAGERS --> C["Text cleaner"]
    MANAGERS --> H["Data chunker"]
    MANAGERS --> E["Embedding service"]
    MANAGERS --> Q["Query processor"]
    MANAGERS --> V["Vector database service"]
    MANAGERS --> R["Data retriever"]
    MANAGERS --> K["Knowledge data builder"]
    CUSTOM["Implementation mở rộng"] -. "Đăng ký qua dependency container" .-> MANAGERS
```

Thiết kế này cho phép thay thế từng thành phần độc lập, chẳng hạn bổ sung vector database, nhà cung cấp embedding hoặc chiến lược chunking mới.

# Admin, web và socket plugin

Admin plugin cung cấp giao diện và API để cấu hình embedding, vector database, chunking, collection và các implementation mặc định. Plugin cũng hỗ trợ lập chỉ mục nguồn dữ liệu, xem chunk và thử tìm kiếm.

Web plugin đăng ký các repository, service, loader, reader và adapter cần thiết cho web runtime. Socket plugin công bố nguồn tri thức và search strategy để EzyAI sử dụng trong luồng chat thời gian thực.

```mermaid
flowchart LR
    CLIENT["Ứng dụng chat hoặc web"] --> RUNTIME["Web hoặc socket runtime"]
    RUNTIME --> AI["EzyAI"]
    AI --> SOURCE["Nguồn tri thức EzyRAG"]
    SOURCE --> SEARCH["Tìm kiếm vector"]
    SEARCH --> CONTEXT["Các đoạn liên quan"]
    CONTEXT --> AI
```

Socket plugin không tạo một giao thức RAG độc lập cho client; nó cung cấp nguồn tri thức để hạ tầng EzyAI gọi nội bộ.

# Ranh giới trách nhiệm

EzyRAG chịu trách nhiệm nạp dữ liệu, làm sạch, chia đoạn, tạo embedding, lưu vector, tìm kiếm và trả về ngữ cảnh liên quan.

EzyRAG không huấn luyện mô hình ngôn ngữ, không quản lý hội thoại và không sinh câu trả lời cuối cùng. Các trách nhiệm đó thuộc về EzyAI và ứng dụng tích hợp.

