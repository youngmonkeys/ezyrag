EzyRAG is the knowledge-retrieval layer for the EzyPlatform ecosystem. It ingests website content, turns that content into vectors for semantic search, and supplies relevant passages to EzyAI or other AI components.

EzyRAG focuses on the **Retrieval** part of Retrieval-Augmented Generation. Calling a language model and generating the final answer are EzyAI responsibilities.

# Overall architecture

EzyRAG consists of four main modules:

- The **core SDK** defines shared data models, abstractions, and RAG pipelines.
- The **admin plugin** provides configuration and data-management features.
- The **web plugin** makes RAG components available in the web runtime.
- The **socket plugin** connects the RAG knowledge source to EzyAI and real-time chat flows.

```mermaid
flowchart TB
    subgraph Platform["EzyPlatform"]
        ADMIN["Admin plugin<br/>Configuration and management"]
        WEB["Web plugin<br/>Web runtime"]
        SOCKET["Socket plugin<br/>Real-time chat"]
        AI["EzyAI<br/>Answer generation"]
    end

    subgraph CORE["EzyRAG SDK"]
        PIPELINE["RAG pipeline"]
        MODELS["Data models"]
        EXTENSIONS["Extension points"]
    end

    CONTENT["Posts, products,<br/>media, and text"]
    EMBEDDING["Embedding service"]
    VECTOR["Vector database"]
    DATABASE["Application database"]

    ADMIN --> CORE
    WEB --> CORE
    SOCKET --> CORE
    CONTENT --> PIPELINE
    PIPELINE --> EMBEDDING
    PIPELINE --> VECTOR
    PIPELINE --> DATABASE
    CORE --> AI
```

# Core SDK

The SDK coordinates two primary flows: indexing data and retrieving knowledge. Each runtime plugin supplies environment-specific dependencies, while the processing contracts and algorithms remain shared.

```mermaid
flowchart LR
    CLIENT["RAG coordinator"]
    CLIENT --> LOADER["Data loader"]
    CLIENT --> CLEANER["Text cleaner"]
    CLIENT --> CHUNKER["Data chunker"]
    CLIENT --> EMBEDDING["Embedding service"]
    CLIENT --> VECTOR["Vector database service"]
    CLIENT --> RETRIEVER["Data retriever"]
    CLIENT --> BUILDER["Knowledge data builder"]
    CLIENT --> SETTINGS["Setting service"]
```

# Data sources and loaders

A data loader converts each source type into a common textual input for the pipeline.

| Source | Indexed content | Attached metadata |
|---|---|---|
| Direct text | Text entered by an administrator | Request metadata |
| Post | Title and body | Title, slug, excerpt |
| Product | Name, codes, description, and localized content | Name, slug, product code, price, currency |
| Media | Descriptive fields and document content | Title, resource URL |

A source can produce multiple inputs. For example, a localized product can yield one input for its default content and additional inputs for its translations.

```mermaid
flowchart LR
    SOURCE["Data source"] --> SELECT{"Source type"}
    SELECT -->|Text| TEXT["Text loader"]
    SELECT -->|Post| POST["Post loader"]
    SELECT -->|Product| PRODUCT["Product loader"]
    SELECT -->|Media| MEDIA["Media loader"]
    TEXT --> INPUT["RAG input"]
    POST --> INPUT
    PRODUCT --> INPUT
    MEDIA --> INPUT
```

# Document readers

For media sources, EzyRAG selects a reader by MIME type or file extension. Supported formats include PDF, Word `.docx`, Excel `.xlsx`, PowerPoint `.pptx`, plain text, and Markdown.

```mermaid
flowchart TD
    MEDIA["Media"] --> INFO["Read title and description"]
    MEDIA --> FILE{"Document format"}
    FILE -->|PDF| PDF["PDF reader"]
    FILE -->|DOCX| WORD["Word reader"]
    FILE -->|XLSX| EXCEL["Excel reader"]
    FILE -->|PPTX| PPT["PowerPoint reader"]
    FILE -->|Text or Markdown| TEXT["Text reader"]
    PDF --> BLOCKS["Text blocks"]
    WORD --> BLOCKS
    EXCEL --> BLOCKS
    PPT --> BLOCKS
    TEXT --> BLOCKS
    INFO --> INPUTS["RAG inputs"]
    BLOCKS --> INPUTS
```

# Text cleaning and chunking

The text cleaner normalizes Unicode, line endings, and whitespace. It also removes unnecessary control characters and repeated blank lines.

The chunker uses a hierarchical strategy. HTML is converted to text while preserving meaningful block boundaries where possible. Content is then split by paragraphs, sentences, and finally character count when no suitable natural boundary remains.

```mermaid
flowchart TD
    RAW["Input text"] --> CLEAN["Normalize text"]
    CLEAN --> HTML["Strip HTML<br/>Preserve block boundaries"]
    HTML --> CHECK{"Within the limit?"}
    CHECK -->|Yes| SINGLE["Create one chunk"]
    CHECK -->|No| PARA["Split and pack by paragraph"]
    PARA --> LONG_PARA{"Paragraph too long?"}
    LONG_PARA -->|No| CHUNKS["Chunk list"]
    LONG_PARA -->|Yes| SENTENCE["Split and pack by sentence"]
    SENTENCE --> LONG_SENTENCE{"Sentence still too long?"}
    LONG_SENTENCE -->|No| CHUNKS
    LONG_SENTENCE -->|Yes| HARD["Split by character count"]
    HARD --> CHUNKS
```

The chunk limit, paragraph separator, and sentence-boundary expression are configurable. The current algorithm limits chunks by character count rather than model tokens.

# Embedding service

The embedding service turns content or queries into numeric vectors. The current implementation supports OpenAI with a configurable embedding model. The target collection determines the required vector size.

The service sits behind an interface and manager, so another provider can be added without changing the main pipeline.

# Vector databases and collections

EzyRAG currently provides adapters for two vector stores:

- EzyVector.
- Qdrant.

The vector database service supports creating collections, upserting and deleting points, and nearest-vector search. A collection records its name, vector database service, vector size, and operational status. Each vector database service can have a default collection.

```mermaid
flowchart LR
    CHUNK["Data chunk"] --> EMB["Embedding"]
    EMB --> POINT["Vector point"]
    POINT --> VECTOR_DB["Vector store<br/>Vector and minimal payload"]
    CHUNK --> APP_DB["Application database<br/>Content, hash, and metadata"]
    VECTOR_DB -. "chunk ID" .-> APP_DB
```

# Chunk and metadata storage

EzyRAG separates business content from vector storage:

- The vector store contains vectors, chunk IDs, and the minimal payload needed for search.
- The application database contains chunk content, hashes, embeddings, and metadata.

Metadata can include a title, slug, excerpt, resource URL, product code, price, and currency. After vector search, EzyRAG uses chunk IDs to load the complete content and metadata.

# Indexing pipeline

```mermaid
sequenceDiagram
    participant A as Admin or application
    participant R as EzyRAG
    participant L as Data loader
    participant C as Cleaner and chunker
    participant D as Application database
    participant E as Embedding service
    participant V as Vector store

    A->>R: Index a source into a collection
    R->>L: Load data by source type
    L-->>R: One or more inputs
    loop Each input
        R->>C: Clean and chunk the text
        C-->>R: Chunk list
        loop Each chunk
            R->>D: Find the previous chunk by source and position
            D-->>R: Existing hash and embedding
            R->>D: Save content and metadata
            alt New or changed content
                R->>E: Create embedding
                E-->>R: Vector
                R->>D: Save embedding
            else Unchanged content
                R->>R: Reuse embedding
            end
            R->>V: Upsert vector point
        end
    end
    R->>D: Remove obsolete trailing chunks
```

Each chunk receives a content hash. For sources with stable identifiers, EzyRAG reuses the existing embedding when the hash has not changed. Direct text input currently creates new chunks instead of looking up embeddings by source and position.

When a new version has fewer chunks, obsolete chunks are deleted from the application database. This indexing flow does not show matching deletion of obsolete vector points from the vector database, which is an important synchronization consideration.

# Search pipeline

The query is normalized, embedded, and searched against a collection. Vector results identify matching chunks; the data retriever loads their content and metadata, and the knowledge builder converts them into data understood by EzyAI.

```mermaid
sequenceDiagram
    participant U as User
    participant AI as EzyAI
    participant R as EzyRAG
    participant Q as Query processor
    participant E as Embedding service
    participant V as Vector store
    participant D as Application database

    U->>AI: Submit a question
    AI->>R: Search for knowledge
    R->>Q: Normalize the query
    Q-->>R: Processed query
    R->>E: Embed the query
    E-->>R: Query vector
    R->>V: Find nearest vectors
    V-->>R: Chunk IDs
    R->>D: Load chunks and metadata
    D-->>R: Relevant documents
    R-->>AI: Knowledge data
    AI-->>U: Generate a contextual answer
```

If the request does not specify a vector service or collection, EzyRAG uses the configured defaults. If the configuration or collection is unavailable, the knowledge source returns an empty list.

# Data retriever and knowledge builder

The data retriever uses IDs from vector search results to load chunks and metadata from the application database. The knowledge builder then converts those documents into EzyAI's knowledge structure.

In addition to vector search, the knowledge source can retrieve content directly by source type and ID or code. When a source has multiple chunks, they are merged in order to reconstruct its full content.

# Configuration and extensibility

Each primary pipeline stage is managed through an interface and manager:

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
    CONFIG["Configuration"] --> MANAGERS["Managers"]
    MANAGERS --> L["Data loader"]
    MANAGERS --> C["Text cleaner"]
    MANAGERS --> H["Data chunker"]
    MANAGERS --> E["Embedding service"]
    MANAGERS --> Q["Query processor"]
    MANAGERS --> V["Vector database service"]
    MANAGERS --> R["Data retriever"]
    MANAGERS --> K["Knowledge data builder"]
    CUSTOM["Custom implementation"] -. "Registered through the dependency container" .-> MANAGERS
```

This design allows individual components to be replaced independently, such as adding a vector database, embedding provider, or chunking strategy.

# Admin, web, and socket plugins

The admin plugin provides UI and APIs for configuring embedding, vector databases, chunking, collections, and default implementations. It also supports indexing sources, inspecting chunks, and testing search.

The web plugin registers the repositories, services, loaders, readers, and adapters required by the web runtime. The socket plugin publishes the knowledge source and search strategy that EzyAI uses in real-time chat flows.

```mermaid
flowchart LR
    CLIENT["Chat or web application"] --> RUNTIME["Web or socket runtime"]
    RUNTIME --> AI["EzyAI"]
    AI --> SOURCE["EzyRAG knowledge source"]
    SOURCE --> SEARCH["Vector search"]
    SEARCH --> CONTEXT["Relevant passages"]
    CONTEXT --> AI
```

The socket plugin does not expose a separate RAG protocol to clients. It supplies an internal knowledge source used by the EzyAI infrastructure.

# Responsibility boundary

EzyRAG is responsible for loading data, cleaning and chunking content, creating embeddings, storing vectors, searching, and returning relevant context.

EzyRAG does not train language models, manage conversations, or generate final answers. Those responsibilities belong to EzyAI and the integrating application.

