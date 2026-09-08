import { useEffect, useState } from "react";
import {
  getAllNotes,
  createNote,
  deleteNote,
} from "../services/noteService";
function Dashboard() {
  const [notes, setNotes] = useState([]);

  const [newNote, setNewNote] = useState({
    title: "",
    content: "",
  });

  useEffect(() => {
    fetchNotes();
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
  try {
    await deleteNote(id);
    fetchNotes();
  } catch (error) {
    console.error(error);
    alert("Failed to delete note");
  }
};
  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await createNote(newNote);

      setNewNote({
        title: "",
        content: "",
      });

      fetchNotes();
    } catch (error) {
      console.error(error);
      alert("Failed to create note");
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

        <button type="submit">Create Note</button>
      </form>

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
            }}
          >
            <h4>{note.title}</h4>
            <p>{note.content}</p>
            <button onClick={() => handleDelete(note.id)}>
    Delete
  </button>
          </div>
        ))
      )}
    </div>
  );
}

export default Dashboard;