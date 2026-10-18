import { useState } from "react";
import { askRag, reindexRag } from "../services/ragService";

export default function RagPanel() {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [sources, setSources] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [status, setStatus] = useState("");

  const handleAsk = async (event) => {
    event.preventDefault();
    if (!question.trim()) return;
    setBusy(true);
    setError("");
    setStatus("");
    setAnswer("");
    setSources([]);
    try {
      const result = await askRag(question.trim());
      setAnswer(result.answer);
      setSources(result.sources ?? []);
    } catch (err) {
      setError(err.response?.data?.message || "Could not answer the question. Check the backend, API key, and vector database.");
    } finally {
      setBusy(false);
    }
  };

  const handleReindex = async () => {
    setBusy(true);
    setError("");
    setStatus("");
    try {
      const result = await reindexRag();
      setStatus(`Reindexed ${result.indexedNotes} notes. You can ask a question now.`);
    } catch (err) {
      setError(err.response?.data?.message || "Reindex failed. Check the backend, API key, and vector database.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <section style={{ border: "1px solid #cbd5e1", borderRadius: 10, padding: 16, margin: "20px 0" }}>
      <h3>Ask your notes (RAG)</h3>
      <p>Ask a question about information you have saved in your notes.</p>
      <form onSubmit={handleAsk}>
        <textarea
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="Example: What did I write about my project deadlines?"
          rows={3}
          maxLength={2000}
          required
          style={{ display: "block", width: "100%", maxWidth: 700, marginBottom: 10 }}
        />
        <button type="submit" disabled={busy || !question.trim()}>{busy ? "Working…" : "Ask"}</button>
        <button type="button" onClick={handleReindex} disabled={busy} style={{ marginLeft: 8 }}>
          Reindex my notes
        </button>
      </form>
      {status && <p role="status">{status}</p>}
      {error && <p role="alert" style={{ color: "crimson" }}>{error}</p>}
      {answer && (
        <div style={{ marginTop: 16 }}>
          <h4>Answer</h4>
          <p style={{ whiteSpace: "pre-wrap" }}>{answer}</p>
          {sources.length > 0 && <>
            <h4>Source notes</h4>
            <ul>{sources.map((source) => (
              <li key={source.noteId}>
                <strong>{source.title}</strong> (note #{source.noteId})
              </li>
            ))}</ul>
          </>}
        </div>
      )}
    </section>
  );
}
