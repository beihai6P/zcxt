package com.zcxt.file.controller;

import com.zcxt.common.web.ApiResponse;
import com.zcxt.file.entity.AssetAttachment;
import com.zcxt.file.service.FileStorageService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('asset:write')")
    public ApiResponse<AssetAttachment> upload(
            @RequestParam(required = false) String assetId,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.ok(fileStorageService.upload(assetId, file));
    }

    @GetMapping("/assets/{assetId}")
    @PreAuthorize("hasAuthority('asset:read')")
    public ApiResponse<List<AssetAttachment>> list(@PathVariable @NotBlank String assetId) {
        return ApiResponse.ok(fileStorageService.listByAssetId(assetId));
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAuthority('asset:write')")
    public ApiResponse<Void> delete(@PathVariable String attachmentId) {
        fileStorageService.delete(attachmentId);
        return ApiResponse.ok(null);
    }
}

