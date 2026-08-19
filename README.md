# EzyRag

                         ┌──────────────────────┐
                         │       USER           │
                         │     Câu hỏi          │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Query Processing   │
                         │ Chuẩn hóa câu hỏi    │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Embedding Model    │
                         │ Text → Vector        │
                         └──────────┬───────────┘
                                    │
                                    ▼
                    ┌──────────────────────────────┐
                    │      Vector Database         │
                    │                              │
                    │  Qdrant / Pinecone /         │
                    │  Weaviate / pgvector         │
                    └──────────────┬───────────────┘
                                   │
                         Tìm tài liệu liên quan
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │        Retriever             │
                    │                              │
                    │  Top-K đoạn nội dung         │
                    │  liên quan nhất              │
                    └──────────────┬───────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │      Context Builder         │
                    │                              │
                    │ Question + Retrieved Context │
                    └──────────────┬───────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │            LLM               │
                    │                              │
                    │ GPT / Claude / Gemini /      │
                    │ DeepSeek / Local LLM         │
                    └──────────────┬───────────────┘
                                   │
                                   ▼
                         ┌──────────────────────┐
                         │      ANSWER          │
                         │  Câu trả lời cuối    │
                         └──────────────────────┘


┌─────────────────┐
│  Nguồn dữ liệu  │
│                 │
│ PDF             │
│ Word            │
│ Website         │
│ Database        │
│ API             │
│ Knowledge Base  │
└────────┬────────┘
│
▼
┌─────────────────┐
│ Document Loader │
│ Đọc dữ liệu     │
└────────┬────────┘
│
▼
┌─────────────────┐
│ Text Cleaning   │
│ Làm sạch dữ liệu│
└────────┬────────┘
│
▼
┌─────────────────┐
│    Chunking     │
│                 │
│ Chia tài liệu   │
│ thành đoạn nhỏ  │
└────────┬────────┘
│
▼
┌─────────────────┐
│ Embedding Model │
│                 │
│ Text → Vector   │
└────────┬────────┘
│
▼
┌─────────────────┐
│ Vector Database │
│                 │
│ Lưu vector +    │
│ metadata        │
└─────────────────┘
