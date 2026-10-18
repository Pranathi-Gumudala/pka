# Personal Knowledge Assistant (PKA)

PKA is a full-stack note-taking app with JWT authentication, a Spring Boot backend, a React/Vite frontend, and MySQL persistence.

## Existing app

- Backend: `pka/` (Spring Boot, Java 25, Maven)
- Frontend: `pka-frontend/` (React + Vite)
- Primary data: MySQL (users and notes)
- Authentication: JWT bearer tokens

## RAG feature

This branch adds retrieval-augmented generation (RAG): users can ask questions about their own saved notes. It uses OpenAI for embeddings and answer generation, plus PostgreSQL with pgvector for semantic retrieval. MySQL remains the source of truth for notes.

**Start here:** [RAG setup and detailed explanation](README-RAG.md) for environment variables, Docker, backend/frontend commands, architecture, API endpoints, and troubleshooting.

## Security / configuration

Set database credentials and your OpenAI API key as environment variables. Never commit secrets. If any real credential was previously committed, rotate it; changing the current file does not remove it from Git history.

## Local development quick start

1. Set the environment variables documented in `README-RAG.md`.
2. Run `docker compose up -d` from the repository root to start pgvector.
3. Start MySQL and run the backend from `pka/` using `mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (macOS/Linux).
4. Run `npm install` and `npm run dev` from `pka-frontend/`.
5. Sign in and click **Reindex my notes** once before asking questions about existing notes.
