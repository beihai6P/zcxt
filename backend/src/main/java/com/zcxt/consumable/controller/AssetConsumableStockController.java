package com.zcxt.consumable.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.consumable.entity.AssetConsumableStock;
import com.zcxt.consumable.service.AssetConsumableStockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumables")
public class AssetConsumableStockController {
    private final AssetConsumableStockService stockService;

    public AssetConsumableStockController(AssetConsumableStockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ApiResponse<Page<AssetConsumableStock>> page(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(stockService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetConsumableStock> get(@PathVariable String id) {
        var stock = stockService.getById(id);
        if (stock == null) {
            return ApiResponse.fail("耗材不存在");
        }
        return ApiResponse.ok(stock);
    }

    @PostMapping
    public ApiResponse<AssetConsumableStock> create(@Valid @RequestBody ConsumableCreateRequest req) {
        AssetConsumableStock stock = new AssetConsumableStock();
        stock.setConsumableType(req.consumableType());
        stock.setConsumableName(req.consumableName());
        stock.setStockQuantity(req.stockQuantity());
        stock.setWarningThreshold(req.warningThreshold());
        return ApiResponse.ok(stockService.create(stock));
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetConsumableStock> update(@PathVariable String id, @RequestBody ConsumableUpdateRequest req) {
        AssetConsumableStock patch = new AssetConsumableStock();
        patch.setConsumableType(req.consumableType());
        patch.setConsumableName(req.consumableName());
        patch.setStockQuantity(req.stockQuantity());
        patch.setWarningThreshold(req.warningThreshold());
        return ApiResponse.ok(stockService.update(id, patch));
    }

    public record ConsumableCreateRequest(
            @NotBlank String consumableType,
            @NotBlank String consumableName,
            Integer stockQuantity,
            Integer warningThreshold
    ) {
    }

    public record ConsumableUpdateRequest(
            String consumableType,
            String consumableName,
            Integer stockQuantity,
            Integer warningThreshold
    ) {
    }
}
