package com.pranathi.pka.service;

import com.pranathi.pka.entity.Note;
import com.pranathi.pka.entity.User;
import com.pranathi.pka.repository.NoteRepository;
import com.pranathi.pka.repository.UserRepository;

import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.*;
import com.pranathi.pka.dto.NoteRequestDTO;
import com.pranathi.pka.dto.NoteResponseDTO;
@Service
public class NoteService {

private final NoteRepository noteRepository;
private final UserRepository userRepository;
private final NoteIndexingService noteIndexingService;
private NoteResponseDTO mapToResponseDTO(Note note) {
    return new NoteResponseDTO(
            note.getId(),
            note.getTitle(),
            note.getContent()
    );
}

private Note mapToEntity(NoteRequestDTO dto) {
    Note note = new Note();
    note.setTitle(dto.getTitle());
    note.setContent(dto.getContent());
    return note;
}
public NoteService(NoteRepository noteRepository,
                   UserRepository userRepository,
                   NoteIndexingService noteIndexingService) {
    this.noteRepository = noteRepository;
    this.userRepository = userRepository;
    this.noteIndexingService = noteIndexingService;
}

    public NoteResponseDTO saveNote(NoteRequestDTO dto, String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    Note note = mapToEntity(dto);

    note.setUser(user);

    Note savedNote = noteRepository.save(note);
    noteIndexingService.indexNote(savedNote, email);

    return mapToResponseDTO(savedNote);
}
    public List<NoteResponseDTO> getAllNotes(String email) {

    return noteRepository.findByUser_Email(email)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
}
   public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long id) {
        super("Note not found with id: " + id);
    }
}
public NoteResponseDTO getNoteById(Long id, String email) {

    Note note = noteRepository.findByIdAndUser_Email(id, email)
            .orElseThrow(() -> new NoteNotFoundException(id));

    return mapToResponseDTO(note);
}

public void deleteNote(Long id, String email) {

    Note note = noteRepository.findByIdAndUser_Email(id, email)
            .orElseThrow(() -> new NoteNotFoundException(id));

    noteIndexingService.deleteNote(id, email);
    noteRepository.delete(note);
}
public NoteResponseDTO updateNote(Long id,
                                  NoteRequestDTO dto,
                                  String email) {

    Note note = noteRepository.findByIdAndUser_Email(id, email)
            .orElseThrow(() -> new NoteNotFoundException(id));

    note.setTitle(dto.getTitle());
    note.setContent(dto.getContent());

    Note updated = noteRepository.save(note);
    noteIndexingService.indexNote(updated, email);

    return mapToResponseDTO(updated);
}
public List<NoteResponseDTO> searchNotes(String keyword, String email) {

    return noteRepository
            .searchNotes(keyword, email)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
}
public List<NoteResponseDTO> getNotesSorted(String field, String email) {

    return noteRepository.findByUser_Email(
                    email,
                    Sort.by(Sort.Direction.ASC, field))
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
}
public Page<NoteResponseDTO> getNotes(int page,
                                      int size,
                                      String sortBy,
                                      String email) {

    Pageable pageable =
            PageRequest.of(page, size, Sort.by(sortBy));

    return noteRepository
            .findByUser_Email(email, pageable)
            .map(this::mapToResponseDTO);
}
public Page<NoteResponseDTO> getNotes(int page,
                                      int size,
                                      String email) {

    Pageable pageable = PageRequest.of(page, size);

    return noteRepository
            .findByUser_Email(email, pageable)
            .map(this::mapToResponseDTO);
}
}
