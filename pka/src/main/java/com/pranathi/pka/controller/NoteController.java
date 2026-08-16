package com.pranathi.pka.controller;



import com.pranathi.pka.service.NoteService;
import java.security.Principal;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import com.pranathi.pka.dto.NoteRequestDTO;
import com.pranathi.pka.dto.NoteResponseDTO;
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

@PostMapping
public NoteResponseDTO createNote(
        @Valid @RequestBody NoteRequestDTO dto,
        Authentication authentication) {

    return noteService.saveNote(dto, authentication.getName());
}
@GetMapping
public List<NoteResponseDTO> getAllNotes(Authentication authentication) {

    return noteService.getAllNotes(authentication.getName());
}
@GetMapping("/{id}")
public NoteResponseDTO getNoteById(@PathVariable Long id,
                                   Principal principal) {

    return noteService.getNoteById(id, principal.getName());
}

@DeleteMapping("/{id}")
public void deleteNote(@PathVariable Long id, Principal principal) {
    noteService.deleteNote(id, principal.getName());
}
@PutMapping("/{id}")
public NoteResponseDTO updateNote(
        @PathVariable Long id,
        @Valid @RequestBody NoteRequestDTO dto,
        Principal principal) {

    return noteService.updateNote(id, dto, principal.getName());
}
@GetMapping("/search")
public List<NoteResponseDTO> searchNotes(
        @RequestParam String keyword,
        Authentication authentication) {

    return noteService.searchNotes(
            keyword,
            authentication.getName());
}
/*@PostMapping
public NoteDTO createNote(@Valid @RequestBody NoteDTO noteDTO) {
    return noteService.createNote(noteDTO);
}*/
@GetMapping("/paged")
public Page<NoteResponseDTO> getNotes(
        @RequestParam int page,
        @RequestParam int size) {

    return noteService.getNotes(page, size);
}
@GetMapping("/sorted")
public List<NoteResponseDTO> getNotesSorted(
        @RequestParam String field) {

    return noteService.getNotesSorted(field);
}
@GetMapping("/paged-sorted")
public Page<NoteResponseDTO> getNotes(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam String sortBy) {

    return noteService.getNotes(page, size, sortBy);
}
}