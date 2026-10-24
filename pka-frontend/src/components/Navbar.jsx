import "../styles/navbar.css";

function Navbar() {
  return (
    <nav className="navbar">
      <div className="logo">
        🧠 <span>PKA</span>
      </div>

      <div className="nav-right">
        <button className="new-btn">
          + New Notebook
        </button>

        <div className="profile">
          👤
        </div>
      </div>
    </nav>
  );
}

export default Navbar;