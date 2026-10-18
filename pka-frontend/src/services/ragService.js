import api from "./api";

export const askRag = async (question) => {
  const response = await api.post("/rag/ask", { question });
  return response.data;
};

export const reindexRag = async () => {
  const response = await api.post("/rag/reindex");
  return response.data;
};
