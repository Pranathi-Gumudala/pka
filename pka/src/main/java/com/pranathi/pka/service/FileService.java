package com.pranathi.pka.service;

import java.io.InputStream;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            return tika.parseToString(inputStream);

        } catch (Exception e) {
            throw new RuntimeException("Unable to extract text from file", e);
        }
    }
}
