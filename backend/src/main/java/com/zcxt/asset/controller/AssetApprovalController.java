package com.zcxt.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetApproval;
import com.zcxt.asset.service.AssetApprovalService;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.security.UserPrincipal;
import com.zcxt.system.annotation.LogOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('approval:read')")
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
    @PreAuthorize("hasAuthority('approval:write')")
    public ApiResponse<Void> create(@Valid @RequestBody CreateRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String applicantId = "system";
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            applicantId = p.getUserId();
        }
        assetApprovalService.createRequest(req.assetId(), applicantId, req.applyType(), req.applyReason(), req.targetStatus(), req.targetDeptId(), req.targetUserId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{approvalId}/approve")
    @LogOperation("处理资产审批")
    @PreAuthorize("hasAuthority('approval:approve')")
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

    public record ApproveRequest(@NotBlank String status, String comment) {}

    public record CreateRequest(
            @NotBlank String assetId,
            @NotBlank String applyType,
            String applyReason,
            @NotBlank String targetStatus,
            String targetDeptId,
            String targetUserId
    ) {}
}
