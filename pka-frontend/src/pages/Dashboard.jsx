import { useEffect, useState } from "react";
import {
  getAllNotes,
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
  const [newNote, setNewNote] = useState({
    title: "",
    content: "",
  });
  const [keyword,setKeyword]=useState("");
  const[page,setPage]=useState(0);
  const[totalPages,setTotalPages]=useState(0);
  useEffect(() => {
    fetchPaginatedNotes(page);
  }, []);

  const fetchNotes = async () => {
    try {
      const data = await getAllNotes();
      setNotes(data);
    } catch (error) {
      console.error(error);
      alert("Failed to load notes");
    }
  };

  const handleChange = (e) => {
    setNewNote({
      ...newNote,
      [e.target.name]: e.target.value,
    });
  };
const handleDelete = async (id) => {
  const confirmDelete = window.confirm(
    "Are you sure you want to delete this note?"
  );

  if (!confirmDelete) return;

  try {
    await deleteNote(id);
    fetchPaginatedNotes(page);
  } catch (error) {
    console.error(error);
    alert("Failed to delete note");
  }
};
const handleEdit = (note) => {
  setEditingId(note.id);

  setNewNote({
    title: note.title,
    content: note.content,
  });
};
const handleSearch = async () => {
  try {
    if (keyword.trim() === "") {
      fetchPaginatedNotes(0);
      return;
    }

    const data = await searchNotes(keyword);
    setNotes(data);
  } catch (error) {
    console.error(error);
    alert("Search failed");
  }
};
const fetchPaginatedNotes = async (currentPage) => {
  try {
    const data = await getNotesPaginated(currentPage, 5);

    setNotes(data.content);
    setPage(data.number);
    setTotalPages(data.totalPages);
  } catch (error) {
    console.error(error);
  }
};
const handleSort = async (field) => {
  try {
    const data = await getSortedNotes(field);
    setNotes(data);
  } catch (error) {
    console.error(error);
    alert("Sorting failed");
  }
};
 const handleSubmit = async (e) => {
  e.preventDefault();

  try {
    if (editingId) {
      await updateNote(editingId, newNote);
    } else {
      await createNote(newNote);
    }

    setNewNote({
      title: "",
      content: "",
    });

    setEditingId(null);

    fetchPaginatedNotes(page);
  } catch (error) {
    console.error(error);
    alert("Operation failed");
  }
};

  return (
    <div>
      <h2>Dashboard</h2>

      <h3>Create Note</h3>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          name="title"
          placeholder="Title"
          value={newNote.title}
          onChange={handleChange}
          required
        />

        <br /><br />

        <textarea
          name="content"
          placeholder="Write your note..."
          rows="5"
          cols="40"
          value={newNote.content}
          onChange={handleChange}
          required
        />

        <br /><br />

        <button type="submit">
  {editingId ? "Update Note" : "Create Note"}
</button>
      </form>
      <hr />

<h3>Search Notes</h3>

<input
  type="text"
  placeholder="Search by title or content..."
  value={keyword}
  onChange={(e) => setKeyword(e.target.value)}
/>

<button
  onClick={handleSearch}
  style={{ marginLeft: "10px" }}
>
  Search
</button>

<button
  onClick={() => {
    setKeyword("");
    fetchPaginatedNotes(0);
  }}
  style={{ marginLeft: "10px" }}
>
  Clear
</button>
<button onClick={() => handleSort("title")}>
  Sort by title
</button>

<button
  onClick={() => fetchPaginatedNotes(0)}
  style={{ marginLeft: "10px" }}
>
  Reset
</button>
<hr />
      <hr />

      <h3>My Notes</h3>

      {notes.length === 0 ? (
        <p>No notes found.</p>
      ) : (
        notes.map((note) => (
            
        <div
  key={note.id}
  style={{
    border: "1px solid #ccc",
    padding: "10px",
    marginBottom: "10px",
    borderRadius: "8px",
  }}
>
  <h4>{note.title}</h4>
  <p>{note.content}</p>

  <button onClick={() => handleEdit(note)}>
    Edit
  </button>

  <button
    onClick={() => handleDelete(note.id)}
    style={{ marginLeft: "10px" }}
  >
    Delete
  </button>
</div>

        ))
      )}
      <div style={{ marginTop: "20px" }}>
  <button
    disabled={page === 0}
    onClick={() => fetchPaginatedNotes(page - 1)}
  >
    Previous
  </button>

  <span style={{ margin: "0 15px" }}>
    Page {page + 1} of {totalPages}
  </span>

  <button
    disabled={page === totalPages - 1}
    onClick={() => fetchPaginatedNotes(page + 1)}
  >
    Next
  </button>
</div>
    </div>
  );
}

export default Dashboard;