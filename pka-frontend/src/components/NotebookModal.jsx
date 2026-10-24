import "../styles/NotebookModal.css";
import { sendMessage } from "../services/chatService";
import { useEffect, useState } from "react";
import SubChatModal from "./SubChatModal";

function NotebookModal({
  note,
  mode,
  onClose,
  onSave,
}) {
  const [opened, setOpened] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const [question, setQuestion] = useState("");
  const [sessionId, setSessionId] = useState(null);

  // Branch chat state
  const [branchMessage, setBranchMessage] = useState(null);
  const [showBranch, setShowBranch] = useState(false);

  const [messages, setMessages] = useState([
    {
      id: null,
      sender: "assistant",
      text: "👋 Hi! Ask me anything about this notebook.",
    },
  ]);

  const MAX_CHARS = 900;

  const createPages = (text) => {
    if (!text) return [""];

    const words = text.split(" ");
    const result = [];

    let current = "";

    words.forEach((word) => {
      if ((current + word).length > MAX_CHARS) {
        result.push(current);
        current = word + " ";
      } else {
        current += word + " ";
      }
    });

    if (current.trim()) result.push(current);

    return result;
  };

  const pages = createPages(content);

  useEffect(() => {
    if (!note) return;

    setOpened(false);
    setCurrentPage(0);
    setTitle(note.title || "");
    setContent(note.content || "");
    setSessionId(null);

    setMessages([
      {
        id: null,
        sender: "assistant",
        text: "👋 Hi! Ask me anything about this notebook.",
      },
    ]);

    setQuestion("");

    const timer = setTimeout(() => {
      setOpened(true);
    }, 150);

    return () => clearTimeout(timer);
  }, [note]);

  if (!note) return null;

  const openBranch = (message) => {
    setBranchMessage(message);
    setShowBranch(true);
  };

  const handleSend = async () => {
    if (!question.trim()) return;

    const userQuestion = question;

    setMessages((prev) => [
      ...prev,
      {
        id: null,
        sender: "user",
        text: userQuestion,
      },
    ]);

    setQuestion("");

    try {
      const response = await sendMessage(
        note.id,
        sessionId,
        null,
        userQuestion
      );

      if (!sessionId && response.sessionId) {
        setSessionId(response.sessionId);
      }

      setMessages((prev) => [
        ...prev,
        {
          id: response.messageId,
          sender: "assistant",
          text: response.content,
        },
      ]);
    } catch (error) {
      console.error(error);

      setMessages((prev) => [
        ...prev,
        {
          id: null,
          sender: "assistant",
          text: "Something went wrong.",
        },
      ]);
    }
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}>
        <div
          className="book-container"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Cover */}
          <div className={`book-cover ${opened ? "open" : ""}`}>
            <h1>{title}</h1>
            <p>Personal Knowledge Assistant</p>
          </div>

          {/* Book */}
          <div className={`book-pages ${opened ? "show" : ""}`}>
            {/* LEFT PAGE */}
            <div className="page left-page">
              <div className="page-header">
                {mode === "view" ? (
                  <h2>{title}</h2>
                ) : (
                  <input
                    className="book-title-input"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                )}

                <span>{new Date().toLocaleDateString()}</span>
              </div>

              <div className="page-content">
                {mode === "view" ? (
                  <div className="page-text">
                    {pages[currentPage]}
                  </div>
                ) : (
                  <textarea
                    className="book-editor"
                    value={content}
                    placeholder="Start writing..."
                    onChange={(e) => setContent(e.target.value)}
                  />
                )}

                <div className="page-navigation">
                  <button
                    disabled={currentPage === 0}
                    onClick={() => setCurrentPage(currentPage - 1)}
                  >
                    ◀ Previous
                  </button>

                  <span>
                    Page {currentPage + 1} / {pages.length}
                  </span>

                  <button
                    disabled={currentPage === pages.length - 1}
                    onClick={() => setCurrentPage(currentPage + 1)}
                  >
                    Next ▶
                  </button>
                </div>
              </div>

              <div className="page-footer">
                {currentPage + 1}
              </div>
            </div>

            {/* RIGHT PAGE */}
            <div className="page right-page">
              <div className="chat-header">
                🤖 Notebook Assistant
              </div>

              <div className="chat-messages">
                {messages.map((msg, index) => (
                  <div
                    key={msg.id ?? index}
                    className={
                      msg.sender === "user"
                        ? "user-message"
                        : "assistant-message"
                    }
                  >
                    <div>{msg.text}</div>

                    {msg.sender === "assistant" && msg.id && (
                      <button
                        className="branch-btn"
                        onClick={() => openBranch(msg)}
                      >
                        🌿 Explore
                      </button>
                    )}
                  </div>
                ))}
              </div>

              <div className="chat-input-area">
                <input
                  type="text"
                  placeholder="Ask this notebook..."
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleSend();
                    }
                  }}
                />

                <button onClick={handleSend}>
                  ➤
                </button>
              </div>

              {mode !== "view" && (
                <button
                  className="save-button"
                  onClick={() =>
                    onSave({
                      ...note,
                      title,
                      content,
                    })
                  }
                >
                  💾 Save Notebook
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      <SubChatModal
        open={showBranch}
        parentMessage={branchMessage}
        note={note}
        sessionId={sessionId}
        onClose={() => setShowBranch(false)}
      />
    </>
  );
}

export default NotebookModal;