package com.pranathi.pka.controller;



import com.pranathi.pka.service.NoteService;
import com.pranathi.pka.service.FileService;
import java.security.Principal;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import com.pranathi.pka.dto.NoteRequestDTO;
import com.pranathi.pka.dto.NoteResponseDTO;
@RestController
@RequestMapping("/api/notes")
public class NoteController {
     private final FileService fileService;
     private final NoteService noteService;

    public NoteController(NoteService noteService,FileService fileService) {
        this.noteService = noteService;
        this.fileService = fileService;
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
        @RequestParam int size,
        Authentication authentication) {

    return noteService.getNotes(
            page,
            size,
            authentication.getName());
}
@GetMapping("/sorted")
public List<NoteResponseDTO> getNotesSorted(
        @RequestParam String field,
        Authentication authentication) {

    return noteService.getNotesSorted(field, authentication.getName());
}

@GetMapping("/paged-sorted")
public Page<NoteResponseDTO> getNotes(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam String sortBy,
        Authentication authentication) {

    return noteService.getNotes(
            page,
            size,
            sortBy,
            authentication.getName());
}
@PostMapping("/upload")
public NoteResponseDTO uploadNote(
        @RequestParam String title,
        @RequestParam(required = false) String additionalNotes,
        @RequestParam MultipartFile file,
        Authentication authentication) {

    String extractedText = fileService.extractText(file);

    String finalContent = extractedText;

    if (additionalNotes != null && !additionalNotes.isBlank()) {
        finalContent += "\n\n";
        finalContent += additionalNotes;
    }

    NoteRequestDTO dto = new NoteRequestDTO();
    dto.setTitle(title);
    dto.setContent(finalContent);

    return noteService.saveNote(dto, authentication.getName());
}
@PostMapping("/extract")
public NoteRequestDTO extractDocument(
        @RequestParam MultipartFile file,
        Authentication authentication) {

    String extractedText = fileService.extractText(file);

    NoteRequestDTO dto = new NoteRequestDTO();
    dto.setTitle(file.getOriginalFilename());
    dto.setContent(extractedText);

    return dto;
}
}