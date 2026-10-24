import "../styles/searchbar.css";

function SearchBar({
  keyword,
  setKeyword,
  onSearch,
  onReset,
  onSort,
}) {
  return (
    <section className="toolbar">

      <input
        type="text"
        placeholder="Search your knowledge..."
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
      />

      <button onClick={onSearch}>
        🔍 Search
      </button>

      <button onClick={onReset}>
        Reset
      </button>

      <button onClick={onSort}>
        Sort
      </button>

    </section>
  );
}

export default SearchBar;