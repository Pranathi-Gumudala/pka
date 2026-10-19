# Personal Knowledge Assistant — RAG setup

This adds retrieval-augmented generation (RAG) to the existing PKA application. Existing notes and user accounts stay in MySQL. PostgreSQL with pgvector is a separate vector index. The authenticated user's email is taken from the JWT-backed Spring Security principal; the browser cannot choose another user's identity.

## Requirements

- Java 25 (the existing Maven project targets Java 25)
- Docker Desktop
- Node.js/npm
- MySQL with the existing PKA schema/database
- An OpenAI API key with API billing enabled

## 1. Configure environment variables (Windows PowerShell)

From the repository root, create your local environment file and replace its placeholder values with your own:

```powershell
Copy-Item .env.example .env
```

Spring Boot loads the repository-root `.env` when run from `pka/` and also supports a `.env` in `pka/`; Docker Compose loads the repository-root `.env`. The `.env.example` template itself is not loaded.

Do not paste real API keys into source files or commit them. The local `.env` file is ignored by Git.

## 2. Start PostgreSQL + pgvector

From the repository root:

```powershell
docker compose up -d
docker compose ps
```

The compose file maps PostgreSQL port 5432 inside the container to port 5433 on your computer. It persists vector data in a Docker volume. The password used by Docker Compose must match `VECTOR_DB_PASSWORD` in the shell. If this is the first start, the container initializes the database automatically.

If you previously created the container with a different password, changing the environment variable alone does not change the existing database user's password. For a disposable local development database, you can stop/remove the container and its volume, then recreate it; removing the volume permanently deletes vector data.

## 3. Start/check MySQL

Make sure your existing MySQL server is running and the database named by `DB_URL` exists. The application uses MySQL for users and notes. Do not point this setting at the vector database.

## 4. Run the Spring Boot backend

In PowerShell terminal 1:

```powershell
cd pka
.\mvnw.cmd clean spring-boot:run
```

If your terminal is not at the repository root, use `cd .\pka` first. On startup the app creates the `note_chunks` table and enables the pgvector extension. The PostgreSQL database user must have permission to create that extension; the provided local Docker image/database owner normally does.

## 5. Run the React frontend

In PowerShell terminal 2, from the repository root:

```powershell
cd pka-frontend
npm install
npm run dev
```

Open the local URL printed by Vite (normally http://localhost:5173). Register or sign in using the app. The existing Axios interceptor adds the JWT to requests.

## 6. Index your existing notes

Open the dashboard and click **Reindex my notes** once after first setup. This reads only the authenticated user's notes from MySQL, chunks each note, creates an embedding for each chunk, and writes those vectors to PostgreSQL. New and edited notes are indexed automatically when the OpenAI API and vector database are available. Reindexing is safe to repeat for one user.

Then ask a question about content actually stored in your notes. The response includes source-note titles and IDs. RAG is not a web search: it can only answer from the retrieved excerpts.

## How the RAG pipeline works

1. **Chunking:** each note's title and content are joined and split into chunks up to 1,200 characters with 200 characters of overlap. Overlap helps retain context at chunk boundaries.
2. **Embedding:** OpenAI's `text-embedding-3-small` converts each chunk into a 1,536-number vector. Similar meanings are represented by vectors that are close in embedding space.
3. **Storage:** PostgreSQL stores each vector beside its note ID, title, chunk text, chunk index, and user email.
4. **Retrieval:** the question is embedded with the same model. pgvector orders chunks by cosine distance and the SQL query filters by the authenticated user's email **before returning matches**.
5. **Generation:** the top five matching chunks are passed to the chat model. The prompt instructs the model to stick to those excerpts and admit when the notes do not contain an answer.
6. **Sources:** the API returns the unique notes that contributed retrieved chunks.

## API

All endpoints require the existing JWT authentication:
- `POST /api/rag/ask` body: `{"question":"What did I write about my deadlines?"}`
- `POST /api/rag/reindex` body: empty

## Troubleshooting

- **MySQL connection refused:** start MySQL and verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- **PostgreSQL connection refused:** run `docker compose ps`; verify port 5433 and `VECTOR_DB_URL`.
- **OpenAI 401:** check `OPENAI_API_KEY`; do not commit it.
- **OpenAI quota/billing errors:** check your OpenAI API project usage and billing.
- **JVM native-memory allocation failure:** close memory-intensive applications and retry. The Maven wrapper limits the JVM heap in `pka/.mvn/jvm.config`; increase available system memory if the failure continues.
- **No notes found:** click **Reindex my notes** while signed in, then retry.
- **Java version error:** install Java 25 and confirm `java -version`.
- **CORS/network error:** use the Vite URL and keep the backend on port 8080, matching the existing frontend API client.

## Important limitations

- MySQL note writes and PostgreSQL vector writes are separate operations, not one atomic transaction. If OpenAI or PostgreSQL is unavailable while a note is saved, the note may exist without a fresh vector. Run **Reindex my notes** to repair the index.
- This is a local-development baseline. For production, use a background job/outbox for indexing, database migrations, request/token limits, retries, monitoring, and tests specifically checking cross-user isolation.
- A previously committed database password should be rotated. Moving it to an environment variable does not erase it from old Git history.
