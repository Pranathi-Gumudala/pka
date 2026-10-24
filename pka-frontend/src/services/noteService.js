import api from "./api";

export const getAllNotes = async () => {
  const response = await api.get("/notes");
  return response.data;
};

export const createNote = async (note) => {
  const response = await api.post("/notes", note);
  return response.data;
};

export const updateNote = async (id, note) => {
  const response = await api.put(`/notes/${id}`, note);
  return response.data;
};

export const deleteNote = async (id) => {
  await api.delete(`/notes/${id}`);
};

export const searchNotes = async (keyword) => {
  const response = await api.get(`/notes/search?keyword=${keyword}`);
  return response.data;
};
export const getNotesPaginated = async (page, size) => {
  const response = await api.get(`/notes/paged?page=${page}&size=${size}`);
  return response.data;
};
export const getSortedNotes = async (field) => {
  const response = await api.get(`/notes/sorted?field=${field}`);
  return response.data;
};
export const extractDocument = async (file) => {
    const formData = new FormData();

    formData.append("file", file);

    const response = await api.post("/notes/extract", formData);

    return response.data;
};
export const summarizeNote = async (content) => {
  const response = await api.post("/ai/summarize", {
    content,
  });

  return response.data;
};