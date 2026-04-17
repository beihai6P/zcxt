package com.zcxt.file.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/public/files")
public class PublicFileController {
    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> file(@PathVariable String fileName) {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ResponseEntity.notFound().build();
        }
        Path file = Path.of("./data/uploads").resolve(fileName).normalize();
        try {
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            String type = Files.probeContentType(file);
            MediaType mediaType = type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type);
            return ResponseEntity.ok().contentType(mediaType).body(Files.readAllBytes(file));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
