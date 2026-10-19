import { useEffect, useState } from "react";
import RagPanel from "../components/RagPanel";
import {
  createNote,
  updateNote,
  deleteNote,
  searchNotes,
  getNotesPaginated,
  getSortedNotes,
} from "../services/noteService";

function Dashboard() {
  const [notes, setNotes] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [newNote, setNewNote] = useState({ title: "", content: "" });
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [notice, setNotice] = useState("");

  useEffect(() => { fetchPaginatedNotes(0); }, []);

  const fetchPaginatedNotes = async (currentPage) => {
    try {
      const data = await getNotesPaginated(currentPage, 6);
      setNotes(data.content);
      setPage(data.number);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error(error);
      setNotice("Couldn't load your notes. Please try again.");
    }
  };

  const handleChange = (event) => setNewNote({ ...newNote, [event.target.name]: event.target.value });

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this note? This action cannot be undone.")) return;
    try {
      await deleteNote(id);
      setNotice("Note deleted.");
      fetchPaginatedNotes(page);
    } catch (error) {
      console.error(error);
      setNotice("Failed to delete the note.");
    }
  };

  const handleEdit = (note) => {
    setEditingId(note.id);
    setNewNote({ title: note.title, content: note.content });
    document.getElementById("note-editor")?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  const handleSearch = async (event) => {
    event?.preventDefault();
    try {
      if (!keyword.trim()) {
        await fetchPaginatedNotes(0);
        return;
      }
      setNotes(await searchNotes(keyword.trim()));
      setPage(0);
      setTotalPages(1);
    } catch (error) {
      console.error(error);
      setNotice("Search failed. Please try again.");
    }
  };

  const handleSort = async (field) => {
    try {
      setNotes(await getSortedNotes(field));
      setPage(0);
      setTotalPages(1);
    } catch (error) {
      console.error(error);
      setNotice("Sorting failed. Please try again.");
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      if (editingId !== null) await updateNote(editingId, newNote);
      else await createNote(newNote);
      setNewNote({ title: "", content: "" });
      setEditingId(null);
      setNotice(editingId !== null ? "Note updated." : "Note added to your library.");
      setKeyword("");
      await fetchPaginatedNotes(page);
    } catch (error) {
      console.error(error);
      setNotice("Couldn't save your note. Please try again.");
    }
  };

  const clearSearch = () => {
    setKeyword("");
    fetchPaginatedNotes(0);
  };

  return (
    <main className="app-frame">
      <header className="app-header">
        <div className="brand-lockup">
          <div className="brand-mark" aria-hidden="true">✳</div>
          <div><div className="brand-name">folio<span style={{ color: "var(--copper)" }}>.</span></div><div className="brand-caption">your personal knowledge library</div></div>
        </div>
        <div className="header-actions"><span className="header-status"><span className="status-dot" /> Your library, in one place</span></div>
      </header>

      <section className="page-intro">
        <div><span className="eyebrow">A SPACE TO THINK CLEARLY</span><h1>Make room for<br />your next idea.</h1><p>Keep your notes close, connect the dots, and ask your own knowledge base questions when a thought needs a little more room.</p></div>
      </section>

      <RagPanel />

      <section className="panel-card" id="note-editor">
        <div className="section-heading"><div><span className="eyebrow">{editingId !== null ? "REFINE AN IDEA" : "CAPTURE SOMETHING"} </span><h2>{editingId !== null ? "Edit note" : "Add to your library"}</h2><p>Small thoughts become useful when you can find them again.</p></div></div>
        <form onSubmit={handleSubmit}>
          <div className="field"><label htmlFor="note-title">Note title</label><input id="note-title" type="text" name="title" placeholder="e.g. Ideas for my next project" value={newNote.title} onChange={handleChange} required /></div>
          <div className="field"><label htmlFor="note-content">Your notes</label><textarea id="note-content" name="content" placeholder="Write down the useful details…" rows={5} value={newNote.content} onChange={handleChange} required /></div>
          <div className="toolbar"><button className="button button-primary" type="submit">{editingId !== null ? "Save changes" : "＋ Save note"}</button>{editingId !== null && <button className="button button-quiet" type="button" onClick={() => { setEditingId(null); setNewNote({ title: "", content: "" }); }}>Cancel edit</button>}</div>
        </form>
      </section>

      <section style={{ marginTop: 34 }}>
        <div className="section-heading"><div><span className="eyebrow">YOUR COLLECTION</span><h2>Notes & fragments</h2><p>Everything you've chosen to keep.</p></div><span className="model-pill">{notes.length} shown</span></div>
        <form className="toolbar" onSubmit={handleSearch}>
          <label className="sr-only" htmlFor="note-search">Search notes</label>
          <input id="note-search" className="search-input" type="search" placeholder="Search titles or note content…" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
          <button className="button button-primary" type="submit">Search</button>
          <button className="button button-quiet" type="button" onClick={clearSearch}>Clear</button>
          <button className="button button-quiet" type="button" onClick={() => handleSort("title")}>Sort A–Z</button>
          <button className="button button-quiet" type="button" onClick={() => { setKeyword(""); fetchPaginatedNotes(0); }}>Reset</button>
        </form>

        {notice && <p className="notice" role="status" style={{ background: "#eef1e9", color: "var(--ink)" }}>{notice}</p>}
        {notes.length === 0 ? <div className="empty-state"><p>No notes here yet. Save a thought above, and it will find a home.</p></div> : (
          <div className="note-grid">{notes.map((note) => (
            <article className="note-card" key={note.id}>
              <span className="note-kicker">NOTE / {String(note.id).padStart(3, "0")}</span>
              <h3>{note.title}</h3>
              <p>{note.content}</p>
              <div className="note-card-actions">
                <button className="button button-quiet" type="button" onClick={() => handleEdit(note)}>Edit</button>
                <button className="button button-danger" type="button" onClick={() => handleDelete(note.id)}>Delete</button>
              </div>
            </article>
          ))}</div>
        )}
        {totalPages > 1 && !keyword.trim() && <div className="pagination"><button className="button button-quiet" disabled={page === 0} onClick={() => fetchPaginatedNotes(page - 1)}>← Previous</button><span>Page {page + 1} of {totalPages}</span><button className="button button-quiet" disabled={page >= totalPages - 1} onClick={() => fetchPaginatedNotes(page + 1)}>Next →</button></div>}
      </section>
      <footer style={{ borderTop: "1px solid var(--line)", marginTop: 48, paddingTop: 20, color: "var(--muted)", fontSize: ".78rem" }}>Built for your own ideas. Kept in your own library.</footer>
    </main>
  );
}

export default Dashboard;
