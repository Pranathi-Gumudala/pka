# PKA retrieval and document ingestion

The original RAG guide described an earlier OpenAI-backed prototype and is now superseded by the current implementation.

See the repository [README](README.md) for current setup, environment variables, Gemini/Ollama provider selection, document upload endpoints, LangGraph setup, security notes, and troubleshooting.

Current embedding behavior:
- Chat generation defaults to Gemini 3.8 Flash, configurable with GEMINI_CHAT_MODEL.
- Ollama's nomic-embed-text remains the embedding model (768 dimensions) so the existing pgvector index remains compatible.
- Retrieval combines note chunks and uploaded-document chunks, filtered by the authenticated user's email.
- The document ingestion API supports PDF, TXT, DOCX, and PPTX text extraction. Scanned PDFs requiring OCR are not supported in this first version.

Do not follow the previous guide's OPENAI_API_KEY or text-embedding-3-small instructions; they no longer match the current code.
