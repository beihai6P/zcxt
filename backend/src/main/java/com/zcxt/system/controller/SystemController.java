package com.zcxt.system.controller;

import com.zcxt.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import com.zcxt.system.service.SystemDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    private final SystemDataService systemDataService;

    public SystemController(SystemDataService systemDataService) {
        this.systemDataService = systemDataService;
    }

    @PostMapping("/backup")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<SystemDataService.BackupResult> backup() {
        return ApiResponse.ok(systemDataService.backup());
    }

    @GetMapping("/backups")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<List<String>> backups() {
        return ApiResponse.ok(systemDataService.listBackups());
    }

    @GetMapping("/backups/{fileName}")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        try {
            var f = systemDataService.getBackupFile(fileName);
            String download = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=utf-8''" + download)
                    .body(Files.readAllBytes(f));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/restore")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<Void> restore(@Valid @RequestBody RestoreRequest req) {
        systemDataService.restore(req.fileName());
        return ApiResponse.ok(null);
    }

    @PostMapping("/clean")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<Integer> clean(@RequestParam(defaultValue = "10") @Min(1) int keep) {
        return ApiResponse.ok(systemDataService.cleanOldBackups(keep));
    }

    public record RestoreRequest(@NotBlank String fileName) {
    }
}
