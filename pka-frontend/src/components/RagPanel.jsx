import { useCallback, useEffect, useMemo, useState } from "react";
import {
  askInConversation,
  createConversation,
  getConversationMessages,
  getConversations,
  reindexRag,
} from "../services/ragService";

export default function RagPanel() {
  const [conversations, setConversations] = useState([]);
  const [conversationId, setConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [leafMessageId, setLeafMessageId] = useState(null);
  const [question, setQuestion] = useState("");
  const [task, setTask] = useState("ask");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [status, setStatus] = useState("");

  const activeConversation = useMemo(
    () => conversations.find((item) => item.id === conversationId),
    [conversations, conversationId]
  );

  const refreshConversations = useCallback(async () => {
    const list = await getConversations();
    setConversations(list);
    return list;
  }, []);

  const openConversation = useCallback(async (id, selectedLeaf = null) => {
    setConversationId(id);
    setLeafMessageId(selectedLeaf);
    const branchMessages = await getConversationMessages(id, selectedLeaf);
    setMessages(branchMessages);
    const lastAssistant = [...branchMessages].reverse().find((message) => message.role === "assistant");
    setLeafMessageId(selectedLeaf ?? lastAssistant?.id ?? null);
  }, []);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const list = await getConversations();
        if (!alive) return;
        if (list.length) {
          setConversations(list);
          setConversationId(list[0].id);
          const branchMessages = await getConversationMessages(list[0].id);
          if (!alive) return;
          setMessages(branchMessages);
          setLeafMessageId([...branchMessages].reverse().find((message) => message.role === "assistant")?.id ?? null);
        } else {
          const created = await createConversation();
          if (!alive) return;
          setConversations([created]);
          setConversationId(created.id);
          setMessages([]);
          setLeafMessageId(null);
        }
      } catch (err) {
        if (alive) setError(err.response?.data?.message || "Could not load chat history.");
      }
    })();
    return () => { alive = false; };
  }, []);

  const handleNewConversation = async () => {
    setBusy(true);
    setError("");
    try {
      const created = await createConversation();
      setConversations((current) => [created, ...current]);
      setConversationId(created.id);
      setMessages([]);
      setLeafMessageId(null);
      setQuestion("");
      setStatus("Started a new conversation.");
    } catch (err) {
      setError(err.response?.data?.message || "Could not create a conversation.");
    } finally {
      setBusy(false);
    }
  };

  const handleAsk = async (event) => {
    event.preventDefault();
    if (!question.trim() || !conversationId) return;
    setBusy(true);
    setError("");
    setStatus("");
    const asked = question.trim();
    setQuestion("");
    try {
      const added = await askInConversation(conversationId, asked, leafMessageId, task);
      setMessages((current) => [...current, ...added]);
      const nextLeaf = [...added].reverse().find((message) => message.role === "assistant")?.id ?? leafMessageId;
      setLeafMessageId(nextLeaf);
      await refreshConversations();
    } catch (err) {
      setQuestion(asked);
      setError(err.response?.data?.message || "Could not answer the question. Check that the backend, agent service, embedding model, and vector database are running.");
    } finally {
      setBusy(false);
    }
  };

  const handleExplore = async (message) => {
    if (message.role !== "assistant" || !conversationId) return;
    setError("");
    try {
      await openConversation(conversationId, message.id);
      setStatus("Exploring from this answer. Your original conversation is preserved.");
    } catch (err) {
      setError(err.response?.data?.message || "Could not open this branch.");
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
      setError(err.response?.data?.message || "Reindex failed. Check that the backend, Ollama, and vector database are running.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="rag-shell">
      <div className="rag-heading">
        <div>
          <span className="eyebrow">YOUR PRIVATE RESEARCH DESK</span>
          <h2>Ask your library</h2>
          <p>Explore your notes, follow a thought, or branch off without losing the original thread.</p>
        </div>
        <div className="rag-heading-actions">
          <button className="button button-quiet" type="button" onClick={handleReindex} disabled={busy}>Reindex notes</button>
          <button className="button button-primary" type="button" onClick={handleNewConversation} disabled={busy}>＋ New thread</button>
        </div>
      </div>

      <div className="chat-layout">
        <aside className="thread-rail" aria-label="Conversation threads">
          <div className="rail-label">RECENT THREADS</div>
          {conversations.length === 0 ? <p className="muted-copy">Your threads will appear here.</p> : conversations.map((thread) => (
            <button
              type="button"
              key={thread.id}
              className={`thread-item ${thread.id === conversationId ? "is-active" : ""}`}
              onClick={() => openConversation(thread.id)}
              disabled={busy}
            >
              <span className="thread-dot" />
              <span className="thread-title">{thread.title || "New conversation"}</span>
            </button>
          ))}
          <div className="rail-note"><span className="status-dot" /> Private to your account</div>
        </aside>

        <div className="chat-workspace">
          <div className="chat-topline">
            <div>
              <span className="eyebrow">CURRENT THREAD</span>
              <h3>{activeConversation?.title || "A fresh line of thought"}</h3>
            </div>
            <span className="model-pill">⌁ Gemini + grounded retrieval</span>
          </div>

          <div className="message-list" aria-live="polite">
            {messages.length === 0 ? (
              <div className="chat-empty">
                <div className="empty-glyph">✳</div>
                <h3>What would you like to uncover?</h3>
                <p>Ask about a project, a decision, or a detail you saved in your notes.</p>
                <div className="suggestion-row">
                  {["Summarize my recent project notes", "What deadlines have I mentioned?", "Find connections across my notes"].map((suggestion) => (
                    <button type="button" key={suggestion} onClick={() => setQuestion(suggestion)} disabled={busy}>{suggestion}</button>
                  ))}
                </div>
              </div>
            ) : messages.map((message) => (
              <article className={`chat-message ${message.role === "user" ? "message-user" : "message-assistant"}`} key={message.id}>
                <div className="message-avatar">{message.role === "user" ? "Y" : "✳"}</div>
                <div className="message-body">
                  <div className="message-meta">{message.role === "user" ? "YOU" : "LIBRARY ASSISTANT"}</div>
                  <p className="message-content">{message.content}</p>
                  {message.sources?.length > 0 && (
                    <div className="source-list">
                      <span className="source-heading">SOURCES FROM YOUR LIBRARY</span>
                      {message.sources.map((source) => (
                        <span className="source-chip" key={source.sourceType === "document" ? `document-${source.documentId}` : `note-${source.noteId}`}>
                          ↗ {source.sourceType === "document" ? "Document" : "Note"}: {source.title}
                        </span>
                      ))}
                    </div>
                  )}
                  {message.role === "assistant" && (
                    <button type="button" className="explore-link" onClick={() => handleExplore(message)} disabled={busy}>
                      ↳ Explore this answer
                    </button>
                  )}
                </div>
              </article>
            ))}
            {busy && <div className="thinking-line"><span className="thinking-dot" /> Looking through your notes…</div>}
          </div>

          <form className="composer" onSubmit={handleAsk}>
            <label className="sr-only" htmlFor="rag-question">Ask a question about your notes</label>
            <textarea
              id="rag-question"
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Ask a follow-up or start a new line of thought…"
              rows={3}
              maxLength={2000}
              required
              disabled={busy || !conversationId}
            />
            <div className="composer-footer">
              <div className="composer-tools">
                <label htmlFor="study-task">Mode</label>
                <select id="study-task" className="task-select" value={task} onChange={(event) => setTask(event.target.value)} disabled={busy}>
                  <option value="ask">Ask my library</option>
                  <option value="research">Source-grounded research</option>
                  <option value="summarize">Study summary</option>
                  <option value="quiz">Generate a quiz</option>
                </select>
                <span>Grounded in your indexed notes and documents.</span>
              </div>
              <button className="button button-primary" type="submit" disabled={busy || !question.trim() || !conversationId}>
                {busy ? "Thinking…" : "Send question ↗"}
              </button>
            </div>
          </form>
        </div>
      </div>
      {status && <p className="notice notice-success" role="status">{status}</p>}
      {error && <p className="notice notice-error" role="alert">{error}</p>}
    </section>
  );
}
