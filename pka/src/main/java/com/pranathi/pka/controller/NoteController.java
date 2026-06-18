package com.pranathi.pka.controller;

import com.pranathi.pka.dto.NoteDTO;
import com.pranathi.pka.entity.Note;
import com.pranathi.pka.service.NoteService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
     public Note createNote(@Valid @RequestBody Note note) {
        return noteService.saveNote(note);
    }
    @GetMapping
public List<Note> getAllNotes() {
    return noteService.getAllNotes();
}
@GetMapping("/{id}")
public Note getNoteById(@PathVariable Long id) {
    return noteService.getNoteById(id);
}

@DeleteMapping("/{id}")
public void deleteNote(@PathVariable Long id) {
    noteService.deleteNote(id);
}
@PutMapping("/{id}")
public Note updateNote(@PathVariable Long id,
                       @RequestBody Note note) {
    return noteService.updateNote(id, note);
}
@GetMapping("/search")
public List<Note> searchNotes(@RequestParam String keyword) {
    return noteService.searchNotes(keyword);
}
/*@PostMapping
public NoteDTO createNote(@Valid @RequestBody NoteDTO noteDTO) {
    return noteService.createNote(noteDTO);
}*/
@GetMapping("/paged")
public Page<Note> getNotes(
        @RequestParam int page,
        @RequestParam int size) {

    return noteService.getNotes(page, size);
}
@GetMapping("/sorted")
public List<Note> getNotesSorted(
        @RequestParam String field) {

    return noteService.getNotesSorted(field);
}
@GetMapping("/paged-sorted")
public Page<Note> getNotes(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam String sortBy) {

    return noteService.getNotes(page, size, sortBy);
}
}