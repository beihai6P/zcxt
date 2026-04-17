package com.zcxt.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.common.IdUtil;
import com.zcxt.config.AppProperties;
import com.zcxt.file.entity.AssetAttachment;
import com.zcxt.file.mapper.AssetAttachmentMapper;
import com.zcxt.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileStorageService {
    private final AppProperties appProperties;
    private final AssetAttachmentMapper attachmentMapper;

    public FileStorageService(AppProperties appProperties, AssetAttachmentMapper attachmentMapper) {
        this.appProperties = appProperties;
        this.attachmentMapper = attachmentMapper;
    }

    public AssetAttachment upload(String assetId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("文件为空");
        }

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > -1 && idx < original.length() - 1) {
            ext = original.substring(idx);
        }

        String name = IdUtil.uuid32() + ext;
        Path dir = Path.of("./data/uploads");
        Path target = dir.resolve(name);
        try {
            Files.createDirectories(dir);
            file.transferTo(target);
        } catch (Exception e) {
            throw new IllegalStateException("文件保存失败");
        }

        String baseUrl = appProperties.getQrcode().getBaseUrl();
        String url = baseUrl + "/api/public/files/" + name;

        AssetAttachment a = new AssetAttachment();
        a.setAttachmentId(IdUtil.uuid32());
        a.setAssetId(assetId);
        a.setFileName(name);
        a.setOriginalName(original);
        a.setContentType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType());
        a.setSizeBytes(file.getSize());
        a.setUrl(url);
        a.setUploaderId(currentUserId());
        a.setUploadTime(LocalDateTime.now());
        attachmentMapper.insert(a);
        return a;
    }

    public List<AssetAttachment> listByAssetId(String assetId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<AssetAttachment>().eq(AssetAttachment::getAssetId, assetId).orderByDesc(AssetAttachment::getUploadTime));
    }

    public void delete(String attachmentId) {
        AssetAttachment a = attachmentMapper.selectById(attachmentId);
        if (a == null) {
            return;
        }
        attachmentMapper.deleteById(attachmentId);
        Path file = Path.of("./data/uploads").resolve(a.getFileName()).normalize();
        try {
            Files.deleteIfExists(file);
        } catch (Exception ignored) {
        }
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p.getUserId();
        }
        return "system";
    }
}

