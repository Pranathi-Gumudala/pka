import api from "./api";

export const askRag = async (question) => {
  const response = await api.post("/rag/ask", { question });
  return response.data;
};

export const reindexRag = async () => {
  const response = await api.post("/rag/reindex");
  return response.data;
};

export const getConversations = async () => (await api.get("/rag/conversations")).data;
export const createConversation = async (title = "New conversation") =>
  (await api.post("/rag/conversations", { title })).data;
export const getConversationMessages = async (conversationId, leafMessageId) => {
  const params = leafMessageId ? { leafMessageId } : {};
  return (await api.get(`/rag/conversations/${conversationId}/messages`, { params })).data;
};
export const askInConversation = async (conversationId, question, parentMessageId) =>
  (await api.post(`/rag/conversations/${conversationId}/messages`, {
    question,
    parentMessageId: parentMessageId ?? null,
  })).data;
