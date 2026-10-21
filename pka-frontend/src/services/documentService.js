import api from "./api";

export const listDocuments = async () => (await api.get("/documents")).data;

export const uploadDocument = async (file) => {
  const form = new FormData();
  form.append("file", file);
  return (await api.post("/documents", form)).data;
};

export const deleteDocument = async (id) => api.delete(`/documents/${id}`);
export const reindexDocument = async (id) => (await api.post(`/documents/${id}/reindex`)).data;
