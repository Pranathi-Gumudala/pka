import axios from "axios";

const API = "http://localhost:8080/api/chat";

export const sendMessage = async (
    noteId,
    sessionId,
    parentMessageId,
    question
) => {

    const token = localStorage.getItem("token");

    const response = await axios.post(
        `${API}/send`,
        {
            noteId,
            sessionId,
            parentMessageId,
            question,
        },
        {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        }
    );

    return response.data;
};