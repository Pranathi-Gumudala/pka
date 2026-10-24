import "../styles/CreateNotebookDialog.css";

function CreateNotebookDialog({
  open,
  title,
  setTitle,
  onCreateBlank,
  onImport,
  onClose,
}) {
  if (!open) return null;

  return (
    <div className="dialog-overlay">

      <div className="dialog-box">

        <h2>📖 Create New Notebook</h2>

        <input
          type="text"
          placeholder="Notebook Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        <div className="dialog-buttons">

          <button onClick={onCreateBlank}>
            ✍️ Create Blank
          </button>

          <button onClick={onImport}>
            📄 Import File
          </button>

        </div>

        <button
          className="cancel-btn"
          onClick={onClose}
        >
          Cancel
        </button>

      </div>

    </div>
  );
}

export default CreateNotebookDialog;