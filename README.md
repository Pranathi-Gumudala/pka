# Personal Knowledge Assistant (PKA)

PKA is a personal study assistant with private notes, persistent branched conversations, source-grounded retrieval, document ingestion and a small LangGraph orchestration service.

## Architecture

- React + Vite frontend: pka-frontend/
- Spring Boot API: pka/
- MySQL: accounts, notes, uploaded-document metadata and extracted text
- PostgreSQL + pgvector: embeddings/chunks for notes and uploaded documents
- Python + FastAPI + LangGraph: bounded workflow for questions, source-grounded research, summaries and quizzes
- Chat provider: Gemini by default, configurable to local Ollama
- Embeddings: Ollama nomic-embed-text (768 dimensions) to keep the existing vector index compatible

Spring Boot remains the authentication and authorization boundary. The browser calls authenticated Spring Boot endpoints. The Python agent is bound to localhost and calls a shared-token-protected retrieval endpoint; it should not be exposed publicly.

## Supported uploads

PDF, TXT, DOCX and PPTX. PKA extracts selectable text and indexes it for retrieval. Scanned PDFs without an embedded text layer are not OCR'd in this first version and may report that no extractable text was found.

## Requirements

- Java 25 (the Maven project currently declares Java 25)
- Maven
- Node.js and npm
- Python 3.11+
- Docker Desktop with WSL2 enabled
- MySQL running locally (database pka_db)
- Ollama running locally with nomic-embed-text pulled
- A Gemini API key for hosted chat, unless selecting AI_CHAT_PROVIDER=ollama

## 1. Configure environment

From the repository root, copy the template:

PowerShell:

    Copy-Item .env.example .env

Edit .env. Set DB_PASSWORD, VECTOR_DB_PASSWORD, GEMINI_API_KEY and a long random INTERNAL_SERVICE_TOKEN. Keep .env private; it is gitignored.

The same INTERNAL_SERVICE_TOKEN is loaded by Spring Boot and the Python service. Do not use the example placeholder in a real environment.

## 2. Start PostgreSQL/pgvector

From the repository root:

    docker compose up -d

This starts the pgvector database on localhost:5433. Ensure VECTOR_DB_PASSWORD in .env is set before running Compose.

Create the MySQL database pka_db and ensure the MySQL credentials in .env can access it.

## 3. Prepare embeddings

Install Ollama and pull the embedding model:

    ollama pull nomic-embed-text

Ollama is used for embeddings even when Gemini is selected for chat. The vector index expects 768 dimensions. Do not switch the embedding model without planning a vector reindex/migration.

Optional local chat model:

    ollama pull qwen2.5-coder:7b

To use local chat instead of Gemini, set AI_CHAT_PROVIDER=ollama in .env. If using Gemini, set AI_CHAT_PROVIDER=gemini and configure GEMINI_API_KEY.

## 4. Start Spring Boot

In a new PowerShell terminal:

    cd pka
    .\mvnw.cmd spring-boot:run

If this checkout does not contain the Maven wrapper, use:

    mvn spring-boot:run

Spring Boot reads the repository-root .env through the existing optional config import. It exposes the API on localhost:8080.

## 5. Start the LangGraph agent

In a new PowerShell terminal from the repository root:

    cd agent-service
    py -3.11 -m venv .venv
    .\.venv\Scripts\Activate.ps1
    python -m pip install -r requirements.txt
    uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload

The agent loads environment variables from the repository-root .env. Keep it bound to 127.0.0.1. It is not a public API.

## 6. Start the frontend

In a new PowerShell terminal:

    cd pka-frontend
    npm ci
    npm run dev

Open the Vite URL shown in the terminal (normally http://localhost:5173).

## API overview

All /api endpoints below require the existing JWT except the existing auth endpoints.

- POST /api/documents — multipart upload with form field file
- GET /api/documents — list the signed-in user's uploads
- GET /api/documents/{id} — fetch owned document metadata
- POST /api/documents/{id}/reindex — rebuild vector chunks for an owned document
- DELETE /api/documents/{id} — delete owned document metadata and vector chunks
- POST /api/rag/ask — existing note/document RAG endpoint
- POST /api/agent/run — JSON { "task": "ask|research|summarize|quiz", "question": "..." }

Internal-only:
- POST /internal/rag/retrieve — protected by X-Internal-Token and used by the Python agent
- POST http://127.0.0.1:8000/agent/run — protected by X-Internal-Token; call through Spring Boot, not from the browser

## Security and limitations

- API keys and database credentials belong in .env, never in frontend variables or committed source.
- Documents and chunks are scoped by authenticated email in Spring Boot.
- Internal service endpoints use a shared secret and the Python service binds to loopback for local development.
- Treat extracted document text as untrusted content; the assistant is instructed to ignore instructions embedded in sources.
- This first version indexes synchronously, so large files may take time while embeddings are generated.
- Source metadata currently identifies the document and chunk; precise PDF page/PowerPoint slide citations are not yet available.
- Initial retrieval uses cosine similarity and top-k. Hybrid search, reranking, OCR and background ingestion are deferred.
- The repository changes have not been built or integration-tested in this editing session. Run build/test commands locally before merging the feature branch.
