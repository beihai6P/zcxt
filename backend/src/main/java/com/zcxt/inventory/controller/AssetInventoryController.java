package com.zcxt.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.inventory.entity.AssetInventory;
import com.zcxt.inventory.entity.AssetInventoryDetail;
import com.zcxt.inventory.service.AssetInventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventories")
public class AssetInventoryController {
    private final AssetInventoryService inventoryService;

    public AssetInventoryController(AssetInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<Page<AssetInventory>> page(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(inventoryService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetInventory> get(@PathVariable String id) {
        var inv = inventoryService.getById(id);
        if (inv == null) {
            return ApiResponse.fail("盘点任务不存在");
        }
        return ApiResponse.ok(inv);
    }

    @PostMapping
    public ApiResponse<AssetInventory> create(@Valid @RequestBody InventoryCreateRequest req) {
        AssetInventory inv = new AssetInventory();
        inv.setInventoryName(req.inventoryName());
        inv.setScopeType(req.scopeType());
        inv.setDeptId(req.deptId());
        inv.setAssetType(req.assetType());
        inv.setCreatorId(req.creatorId());
        inv.setStartTime(req.startTime());
        inv.setEndTime(req.endTime());
        return ApiResponse.ok(inventoryService.create(inv));
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetInventory> update(@PathVariable String id, @RequestBody InventoryUpdateRequest req) {
        AssetInventory patch = new AssetInventory();
        patch.setInventoryName(req.inventoryName());
        patch.setStatus(req.status());
        return ApiResponse.ok(inventoryService.update(id, patch));
    }

    @GetMapping("/{id}/details")
    public ApiResponse<Page<AssetInventoryDetail>> getDetails(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return ApiResponse.ok(inventoryService.getDetails(id, page, size));
    }

    public record InventoryCreateRequest(
            @NotBlank String inventoryName,
            @NotBlank String scopeType,
            String deptId,
            String assetType,
            @NotBlank String creatorId,
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime
    ) {
    }

    public record InventoryUpdateRequest(
            String inventoryName,
            String status
    ) {
    }
}
