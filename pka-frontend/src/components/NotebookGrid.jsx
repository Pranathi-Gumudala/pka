import NotebookCard from "./NotebookCard";
import "../styles/notebookGrid.css";
import NewNotebookCard from "./NewNotebookCard";

function NotebookGrid({
  notes,
  onEdit,
  onDelete,
  onOpen,
  onCreate,
}) {
  return (
    <div className="notes-grid">
      <NewNotebookCard onClick={onCreate} />

      {notes.map((note) => (
        <NotebookCard
          key={note.id}
          note={note}
          onEdit={onEdit}
          onDelete={onDelete}
          onOpen={onOpen}
        />
      ))}
    </div>
  );
}

export default NotebookGrid;