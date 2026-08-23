# Tổng quan

EzyRag là plugin RAG (Retrieval-Augmented Generation) cho EzyPlatform. Plugin tự động biến nội dung sẵn có trên website — bài viết, mô tả sản phẩm, văn bản, file tài liệu tải lên — thành vector (embedding) và lưu vào cơ sở dữ liệu vector, phục vụ tìm kiếm ngữ nghĩa. EzyRag đảm nhiệm tầng truy hồi tri thức (retrieval); việc sinh câu trả lời bằng LLM được thực hiện bởi plugin EzyAI thông qua cơ chế tìm kiếm tri thức mà EzyRag cung cấp.

# Các tính năng chính

**Nạp dữ liệu từ nhiều nguồn**
Tự động lấy nội dung bài viết CMS, mô tả sản phẩm và văn bản thuần trên website. Ngoài ra hỗ trợ tải lên và trích xuất nội dung từ file PDF, Word (.docx), Excel (.xlsx), PowerPoint (.pptx) và text/Markdown.

**Chia nhỏ nội dung (chunking) thông minh**
Làm sạch HTML rồi chia nội dung theo cấu trúc phân cấp đoạn → câu → cắt ký tự khi cần, với độ dài chunk có thể cấu hình, giúp embedding và tìm kiếm chính xác hơn.

**Embedding bằng OpenAI**
Tạo vector embedding qua API OpenAI (mặc định `text-embedding-3-small`, có thể đổi model), cấu hình API key ngay trong trang quản trị.

**Nhiều lựa chọn cơ sở dữ liệu vector**
Hỗ trợ lưu và tìm kiếm vector qua Qdrant, plugin EzyVector, hoặc trực tiếp trong MySQL — chọn và cấu hình kết nối (URL, API key, kích thước vector) theo nhu cầu hạ tầng.

**Tích hợp chat thời gian thực**
Cung cấp socket plugin để tra cứu tri thức RAG theo thời gian thực cho tính năng chat, phối hợp cùng EzyAI để trả lời câu hỏi người dùng dựa trên dữ liệu của chính website.

**Quản trị trực quan**
Trang quản trị EzyPlatform cho phép chọn knowledge builder/data chunker/data retriever, cấu hình dịch vụ embedding và vector database, xem và tìm kiếm danh sách các chunk đã được tạo.

# Yêu cầu

Website EzyPlatform cần cài thêm plugin EzyAI (đảm nhiệm sinh câu trả lời bằng LLM), có API key OpenAI để tạo embedding, và ít nhất một kho vector đang chạy: máy chủ Qdrant, plugin EzyVector, hoặc MySQL.
