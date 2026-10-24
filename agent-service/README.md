# PKA LangGraph service

This private FastAPI service runs a LangGraph workflow: retrieve the authenticated user's relevant excerpts through Spring Boot, then generate a grounded answer, study summary, or quiz.

## Requirements

- Python 3.11+
- Spring Boot API running on http://127.0.0.1:8080
- A repository-root .env containing the same INTERNAL_SERVICE_TOKEN as Spring Boot
- GEMINI_API_KEY for Gemini, or AI_CHAT_PROVIDER=ollama with Ollama running locally

## Run on Windows PowerShell

From the repository root:

    cd agent-service
    py -3.11 -m venv .venv
    .\.venv\Scripts\Activate.ps1
    python -m pip install -r requirements.txt
    uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload

The service binds to loopback by default; do not expose it directly to the public internet. Spring Boot owns user authentication and passes the authenticated email to this service. Requests are protected with a shared internal token.

## Endpoint

POST /agent/run is intended for Spring Boot only. Tasks: ask, research, summarize, quiz. The service returns an answer and source metadata. It does not browse the public web; research means source-grounded research across the user's indexed notes and documents.
