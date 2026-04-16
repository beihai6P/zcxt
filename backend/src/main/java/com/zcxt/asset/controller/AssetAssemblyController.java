package com.zcxt.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetAssembly;
import com.zcxt.asset.service.AssetAssemblyService;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.system.annotation.LogOperation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets/assembly")
public class AssetAssemblyController {
    private final AssetAssemblyService assetAssemblyService;

    public AssetAssemblyController(AssetAssemblyService assetAssemblyService) {
        this.assetAssemblyService = assetAssemblyService;
    }

    @GetMapping
    public ApiResponse<Page<AssetAssembly>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String assetId
    ) {
        return ApiResponse.ok(assetAssemblyService.page(page, size, assetId));
    }

    @PostMapping
    @LogOperation("新增资产组装记录")
    public ApiResponse<Void> create(@RequestBody AssetAssembly assembly) {
        assetAssemblyService.create(assembly);
        return ApiResponse.ok(null);
    }
}
