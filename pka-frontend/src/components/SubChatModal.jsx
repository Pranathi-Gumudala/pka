import { useState } from "react";
import { sendMessage } from "../services/chatService";
import "../styles/SubChatModal.css";

function SubChatModal({
    open,
    onClose,
    parentMessage,
    note,
    sessionId,
}) {

    const [question, setQuestion] = useState("");

    const [messages, setMessages] = useState(() => [
        {
            sender: "assistant",
            text: parentMessage?.text || "",
        },
    ]);

    if (!open || !parentMessage) return null;

    const handleSend = async () => {

        if (!question.trim()) return;

        const userQuestion = question;

        setMessages(prev => [
            ...prev,
            {
                sender: "user",
                text: userQuestion,
            },
        ]);

        setQuestion("");

        try {

            const response = await sendMessage(
                note.id,
                sessionId,
                parentMessage.id,
                userQuestion
            );

            setMessages(prev => [
                ...prev,
                {
                    id: response.messageId,
                    sender: "assistant",
                    text: response.content,
                },
            ]);

        } catch (err) {

            console.error(err);

            setMessages(prev => [
                ...prev,
                {
                    sender: "assistant",
                    text: "Something went wrong.",
                },
            ]);

        }

    };

    return (

        <div className="subchat-overlay">

            <div className="subchat-container">

                <div className="subchat-header">

                    <h2>🌿 Explore Response</h2>

                    <button
                        className="close-btn"
                        onClick={onClose}
                    >
                        ✕
                    </button>

                </div>

                <div className="subchat-messages">

                    {messages.map((msg, index) => (

                        <div
                            key={index}
                            className={
                                msg.sender === "user"
                                    ? "user-message"
                                    : "assistant-message"
                            }
                        >
                            {msg.text}
                        </div>

                    ))}

                </div>

                <div className="subchat-input">

                    <input
                        type="text"
                        placeholder="Ask more about this response..."
                        value={question}
                        onChange={(e) => setQuestion(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                handleSend();
                            }
                        }}
                    />

                    <button onClick={handleSend}>
                        Send
                    </button>

                </div>

            </div>

        </div>

    );

}

export default SubChatModal;