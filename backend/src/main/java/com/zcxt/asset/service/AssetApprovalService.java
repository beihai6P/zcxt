package com.zcxt.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetApproval;
import com.zcxt.asset.mapper.AssetApprovalMapper;
import com.zcxt.common.IdUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssetApprovalService {
    private final AssetApprovalMapper assetApprovalMapper;
    private final AssetService assetService;

    public AssetApprovalService(AssetApprovalMapper assetApprovalMapper, AssetService assetService) {
        this.assetApprovalMapper = assetApprovalMapper;
        this.assetService = assetService;
    }

    public Page<AssetApproval> page(int page, int size, String assetId, String status) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<AssetApproval> q = new LambdaQueryWrapper<>();
        if (assetId != null && !assetId.isBlank()) {
            q.eq(AssetApproval::getAssetId, assetId);
        }
        if (status != null && !status.isBlank()) {
            q.eq(AssetApproval::getStatus, status);
        }
        q.orderByDesc(AssetApproval::getApplyTime);

        Page<AssetApproval> p = new Page<>(current, pageSize);
        return assetApprovalMapper.selectPage(p, q);
    }

    @Transactional
    public void createRequest(String assetId, String applicantId, String applyType, String applyReason, String targetStatus, String targetDeptId, String targetUserId) {
        AssetApproval approval = new AssetApproval();
        approval.setApprovalId(IdUtil.uuid32());
        approval.setAssetId(assetId);
        approval.setApplicantId(applicantId);
        approval.setApplyType(applyType);
        approval.setApplyReason(applyReason);
        approval.setTargetStatus(targetStatus);
        approval.setTargetDeptId(targetDeptId);
        approval.setTargetUserId(targetUserId);
        approval.setStatus("待审批");
        approval.setApplyTime(LocalDateTime.now());
        assetApprovalMapper.insert(approval);
    }

    @Transactional
    public void approve(String approvalId, String approverId, String status, String remark) {
        AssetApproval approval = assetApprovalMapper.selectById(approvalId);
        if (approval == null) {
            throw new IllegalStateException("审批单不存在");
        }
        if (!"待审批".equals(approval.getStatus())) {
            throw new IllegalStateException("审批单已处理");
        }

        approval.setApproverId(approverId);
        approval.setStatus(status);
        approval.setApproveRemark(remark);
        approval.setApproveTime(LocalDateTime.now());
        assetApprovalMapper.updateById(approval);

        if ("已通过".equals(status)) {
            assetService.changeStatus(approval.getAssetId(), new AssetService.ChangeRequest(
                    approval.getApplyType(),
                    approval.getTargetStatus(),
                    approval.getTargetDeptId(),
                    approval.getTargetUserId(),
                    approverId,
                    approval.getApplyReason()
            ));
        }
    }
}
