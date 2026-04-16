package com.zcxt.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetApproval;
import com.zcxt.asset.service.AssetApprovalService;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.security.UserPrincipal;
import com.zcxt.system.annotation.LogOperation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets/approval")
public class AssetApprovalController {
    private final AssetApprovalService assetApprovalService;

    public AssetApprovalController(AssetApprovalService assetApprovalService) {
        this.assetApprovalService = assetApprovalService;
    }

    @GetMapping
    public ApiResponse<Page<AssetApproval>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(assetApprovalService.page(page, size, assetId, status));
    }

    @PostMapping
    @LogOperation("提交资产审批")
    public ApiResponse<Void> create(@RequestBody AssetApproval approval) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            approval.setApplicantId(p.getUserId());
        }
        assetApprovalService.create(approval);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{approvalId}/approve")
    @LogOperation("处理资产审批")
    public ApiResponse<Void> approve(
            @PathVariable String approvalId,
            @RequestBody ApproveRequest req
    ) {
        String approverId = "system";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            approverId = p.getUserId();
        }
        assetApprovalService.approve(approvalId, approverId, req.status(), req.comment());
        return ApiResponse.ok(null);
    }

    public record ApproveRequest(String status, String comment) {}
}
