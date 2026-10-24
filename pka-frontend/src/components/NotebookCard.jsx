import "../styles/notebookCard.css";

function NotebookCard({
  note,
  onEdit,
  onDelete,
  onOpen,
}) {
  const colors = [
    "#4F46E5", // Indigo
    "#0EA5E9", // Blue
    "#10B981", // Green
    "#F59E0B", // Amber
    "#EF4444", // Red
    "#8B5CF6", // Purple
    "#92400E", // Brown
  ];

  const coverColor =
    colors[note.title.charCodeAt(0) % colors.length];

  const thickness = Math.min(
    30,
    8 + Math.floor(note.content.length / 150)
  );

  return (
    <div
      className="note-card"
      onClick={() => onOpen(note)}
      role="button"
      tabIndex={0}
    >
      <div
        className="thumbnail"
        style={{
          background: coverColor,
          width: `${150 + thickness}px`,
        }}
      >
        <div className="thumbnail-overlay">
          <h3>{note.title}</h3>
        </div>
      </div>

      <div className="bookmark"></div>

      <div className="note-content">
        <p>
          {note.content.length > 100
            ? note.content.substring(0, 100) + "..."
            : note.content}
        </p>

        <div className="card-buttons">
          <button
            onClick={(e) => {
              e.stopPropagation();
              onOpen(note);
            }}
            title="Open Notebook"
          >
            🤖
          </button>

          <button
            onClick={(e) => {
              e.stopPropagation();
              onEdit(note);
            }}
            title="Edit Notebook"
          >
            ✏️
          </button>

          <button
            onClick={(e) => {
              e.stopPropagation();
              onDelete(note.id);
            }}
            title="Delete Notebook"
          >
            🗑️
          </button>
        </div>
      </div>
    </div>
  );
}

export default NotebookCard;