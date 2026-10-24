import "../styles/NewNotebookCard.css";

function NewNotebookCard({ onClick }) {
  return (
    <div className="new-notebook" onClick={onClick}>
      <div className="plus">+</div>
      <h3>New Notebook</h3>
      <p>Create your next idea</p>
    </div>
  );
}

export default NewNotebookCard;