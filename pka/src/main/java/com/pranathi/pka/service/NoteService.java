package com.pranathi.pka.service;

import com.pranathi.pka.entity.Note;
import com.pranathi.pka.repository.NoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.*;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }
   public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long id) {
        super("Note not found with id: " + id);
    }
}
public Note getNoteById(Long id) {
    return noteRepository.findById(id)
            .orElseThrow(() -> new NoteNotFoundException(id));
}

public void deleteNote(Long id) {
    noteRepository.deleteById(id);
}
public Note updateNote(Long id, Note updatedNote) {
    Note note = noteRepository.findById(id).orElseThrow();

    note.setTitle(updatedNote.getTitle());
    note.setContent(updatedNote.getContent());

    return noteRepository.save(note);
}
public List<Note> searchNotes(String keyword) {
    return noteRepository.findByTitleContainingIgnoreCase(keyword);
}
public List<Note> getNotesSorted(String field) {
    return noteRepository.findAll(Sort.by(Sort.Direction.ASC, field));
}
public Page<Note> getNotes(int page, int size, String sortBy) {
    Pageable pageable =
            PageRequest.of(page, size, Sort.by(sortBy));

    return noteRepository.findAll(pageable);
}
public Page<Note> getNotes(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return noteRepository.findAll(pageable);
}
}
