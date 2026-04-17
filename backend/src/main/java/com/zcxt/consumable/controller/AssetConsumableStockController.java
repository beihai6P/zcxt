package com.zcxt.consumable.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.consumable.entity.AssetConsumableStock;
import com.zcxt.consumable.entity.ConsumableTxn;
import com.zcxt.consumable.service.AssetConsumableStockService;
import com.zcxt.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumables")
public class AssetConsumableStockController {
    private final AssetConsumableStockService stockService;

    public AssetConsumableStockController(AssetConsumableStockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<Page<AssetConsumableStock>> page(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(stockService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<AssetConsumableStock> get(@PathVariable String id) {
        var stock = stockService.getById(id);
        if (stock == null) {
            return ApiResponse.fail("耗材不存在");
        }
        return ApiResponse.ok(stock);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<AssetConsumableStock> create(@Valid @RequestBody ConsumableCreateRequest req) {
        AssetConsumableStock stock = new AssetConsumableStock();
        stock.setConsumableType(req.consumableType());
        stock.setConsumableName(req.consumableName());
        stock.setStockQuantity(req.stockQuantity());
        stock.setWarningThreshold(req.warningThreshold());
        return ApiResponse.ok(stockService.create(stock));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<AssetConsumableStock> update(@PathVariable String id, @RequestBody ConsumableUpdateRequest req) {
        AssetConsumableStock patch = new AssetConsumableStock();
        patch.setConsumableType(req.consumableType());
        patch.setConsumableName(req.consumableName());
        patch.setStockQuantity(req.stockQuantity());
        patch.setWarningThreshold(req.warningThreshold());
        return ApiResponse.ok(stockService.update(id, patch));
    }

    @PostMapping("/{id}/in")
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<Void> inbound(@PathVariable String id, @Valid @RequestBody InboundRequest req) {
        stockService.inbound(id, req.quantity(), req.remark(), operatorId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/out")
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<Void> outbound(@PathVariable String id, @Valid @RequestBody OutboundRequest req) {
        stockService.outbound(id, req.quantity(), req.deptId(), req.userId(), req.remark(), operatorId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/txns")
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<Page<ConsumableTxn>> txns(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return ApiResponse.ok(stockService.txns(id, page, size));
    }

    @GetMapping("/stats/usage")
    @PreAuthorize("hasAuthority('consumable:manage')")
    public ApiResponse<List<AssetConsumableStockService.DailyUsage>> usage(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(stockService.usageTrend(days));
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

    public record InboundRequest(@Min(1) int quantity, String remark) {
    }

    public record OutboundRequest(@Min(1) int quantity, String deptId, String userId, String remark) {
    }

    private String operatorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p.getUserId();
        }
        return "system";
    }
}
