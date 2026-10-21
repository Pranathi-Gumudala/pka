import { useCallback, useEffect, useRef, useState } from "react";
import { deleteDocument, listDocuments, reindexDocument, uploadDocument } from "../services/documentService";

export default function DocumentPanel() {
  const [documents, setDocuments] = useState([]);
  const [file, setFile] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [status, setStatus] = useState("");
  const fileInputRef = useRef(null);

  const refresh = useCallback(async () => {
    const data = await listDocuments();
    setDocuments(data);
  }, []);

  useEffect(() => {
    refresh().catch(() => setError("Could not load your uploaded documents."));
  }, [refresh]);

  const handleUpload = async (event) => {
    event.preventDefault();
    if (!file) return;
    setBusy(true);
    setError("");
    setStatus("");
    try {
      const result = await uploadDocument(file);
      setStatus(result.status === "READY"
        ? `Indexed ${result.filename} successfully.`
        : `${result.filename} upload status: ${result.status}. ${result.errorMessage || ""}`);
      setFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      await refresh();
    } catch (err) {
      setError(err.response?.data?.detail || err.response?.data?.message || "Upload failed. Check the backend, Ollama embeddings, and pgvector.");
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async (document) => {
    if (!window.confirm(`Delete ${document.filename} and its indexed chunks?`)) return;
    setBusy(true);
    setError("");
    setStatus("");
    try {
      await deleteDocument(document.id);
      setStatus("Document and its indexed chunks were deleted.");
      await refresh();
    } catch (err) {
      setError(err.response?.data?.detail || "Could not delete this document.");
    } finally {
      setBusy(false);
    }
  };

  const handleReindex = async (document) => {
    setBusy(true);
    setError("");
    setStatus("");
    try {
      const result = await reindexDocument(document.id);
      setStatus(result.status === "READY" ? `Reindexed ${result.filename}.` : `Reindex status: ${result.status}. ${result.errorMessage || ""}`);
      await refresh();
    } catch (err) {
      setError(err.response?.data?.detail || "Could not reindex this document.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="panel-card document-panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">SOURCE MATERIAL</span>
          <h2>Documents for your study desk</h2>
          <p>Upload PDFs, text files, Word documents or PowerPoint decks to make their text searchable.</p>
        </div>
        <span className="model-pill">{documents.length} uploaded</span>
      </div>

      <form className="document-upload-form" onSubmit={handleUpload}>
        <div className="field">
          <label htmlFor="document-file">Choose a document (max 15 MB)</label>
          <input
            ref={fileInputRef}
            id="document-file"
            type="file"
            accept=".pdf,.txt,.docx,.pptx"
            onChange={(event) => setFile(event.target.files?.[0] || null)}
            disabled={busy}
            required
          />
        </div>
        <button className="button button-primary" type="submit" disabled={busy || !file}>
          {busy ? "Working…" : "Upload and index"}
        </button>
      </form>

      {documents.length > 0 ? (
        <div className="document-list">
          {documents.map((document) => (
            <article className="document-row" key={document.id}>
              <div className="document-icon" aria-hidden="true">▤</div>
              <div className="document-details">
                <strong>{document.filename}</strong>
                <span>{document.mediaType} · {new Date(document.createdAt).toLocaleDateString()}</span>
                {document.errorMessage && <small>{document.errorMessage}</small>}
              </div>
              <span className={`document-status status-${document.status.toLowerCase()}`}>{document.status}</span>
              <div className="document-actions">
                <button type="button" className="button button-quiet" onClick={() => handleReindex(document)} disabled={busy}>Reindex</button>
                <button type="button" className="button button-quiet" onClick={() => handleDelete(document)} disabled={busy}>Delete</button>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <p className="muted-copy document-empty">No documents yet. Your uploaded sources will appear here.</p>
      )}
      {status && <p className="notice notice-success" role="status">{status}</p>}
      {error && <p className="notice notice-error" role="alert">{error}</p>}
    </section>
  );
}
