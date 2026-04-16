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

    public AssetApprovalService(AssetApprovalMapper assetApprovalMapper) {
        this.assetApprovalMapper = assetApprovalMapper;
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
    public void create(AssetApproval approval) {
        approval.setApprovalId(IdUtil.uuid32());
        approval.setStatus("待审批");
        approval.setApplyTime(LocalDateTime.now());
        assetApprovalMapper.insert(approval);
    }

    @Transactional
    public void approve(String approvalId, String approverId, String status, String comment) {
        AssetApproval approval = assetApprovalMapper.selectById(approvalId);
        if (approval != null) {
            approval.setApproverId(approverId);
            approval.setStatus(status);
            approval.setComment(comment);
            approval.setApproveTime(LocalDateTime.now());
            assetApprovalMapper.updateById(approval);
        }
    }
}
