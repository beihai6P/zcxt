package com.zcxt.asset.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.entity.AssetChangeHistory;
import com.zcxt.asset.mapper.AssetBaseMapper;
import com.zcxt.asset.mapper.AssetChangeHistoryMapper;
import com.zcxt.common.crypto.AesGcmService;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.config.AppProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicQrcodeController {
    private final AppProperties appProperties;
    private final AesGcmService aesGcmService;
    private final ObjectMapper objectMapper;
    private final AssetBaseMapper assetBaseMapper;
    private final AssetChangeHistoryMapper historyMapper;

    public PublicQrcodeController(AppProperties appProperties, AesGcmService aesGcmService, ObjectMapper objectMapper, AssetBaseMapper assetBaseMapper, AssetChangeHistoryMapper historyMapper) {
        this.appProperties = appProperties;
        this.aesGcmService = aesGcmService;
        this.objectMapper = objectMapper;
        this.assetBaseMapper = assetBaseMapper;
        this.historyMapper = historyMapper;
    }

    @GetMapping(value = "/qrcodes/{fileName}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrcodeFile(@PathVariable String fileName) {
        if (!fileName.endsWith(".png")) {
            return ResponseEntity.notFound().build();
        }
        Path file = Path.of(appProperties.getQrcode().getLocalDir()).resolve(fileName).normalize();
        try {
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(file));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/qrcode/resolve")
    public ApiResponse<Map<String, Object>> resolve(@RequestBody ResolveRequest req) {
        String json = aesGcmService.decryptFromBase64(req.content());
        Map<String, Object> payload = readMap(json);

        Object assetIdObj = payload.get("assetId");
        if (assetIdObj == null) {
            return ApiResponse.fail("二维码内容不完整");
        }
        String assetId = assetIdObj.toString();

        AssetBase asset = assetBaseMapper.selectById(assetId);
        Page<AssetChangeHistory> history = historyMapper.selectPage(Page.of(1, 20), new LambdaQueryWrapper<AssetChangeHistory>()
                .eq(AssetChangeHistory::getAssetId, assetId)
                .orderByDesc(AssetChangeHistory::getChangeTime));

        return ApiResponse.ok(Map.of(
                "payload", payload,
                "asset", asset,
                "history", history.getRecords()
        ));
    }

    @GetMapping("/assets/{assetId}")
    public ApiResponse<AssetBase> publicAsset(@PathVariable String assetId) {
        AssetBase asset = assetBaseMapper.selectById(assetId);
        if (asset == null) {
            return ApiResponse.fail("资产不存在");
        }
        asset.setQrcodeContent(null);
        return ApiResponse.ok(asset);
    }

    private Map<String, Object> readMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("二维码解析失败");
        }
    }

    public record ResolveRequest(String content) {
    }
}

