package com.pranathi.pka.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pranathi.pka.dto.UploadedDocumentDto;
import com.pranathi.pka.service.UploadedDocumentService;

@RestController
@RequestMapping("/api/documents")
public class UploadedDocumentController {
    private final UploadedDocumentService service;

    public UploadedDocumentController(UploadedDocumentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadedDocumentDto upload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        return service.upload(file, authentication.getName());
    }

    @GetMapping
    public List<UploadedDocumentDto> list(Authentication authentication) {
        return service.list(authentication.getName());
    }

    @GetMapping("/{id}")
    public UploadedDocumentDto get(@PathVariable Long id, Authentication authentication) {
        return service.get(id, authentication.getName());
    }

    @PostMapping("/{id}/reindex")
    public UploadedDocumentDto reindex(@PathVariable Long id, Authentication authentication) {
        return service.reindex(id, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName());
    }
}
