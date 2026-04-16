package com.zcxt.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.entity.AssetChangeHistory;
import com.zcxt.asset.mapper.AssetBaseMapper;
import com.zcxt.asset.mapper.AssetChangeHistoryMapper;
import com.zcxt.common.IdUtil;
import com.zcxt.common.crypto.AesGcmService;
import com.zcxt.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
public class AssetService {
    private final AssetBaseMapper assetBaseMapper;
    private final AssetChangeHistoryMapper historyMapper;
    private final ObjectMapper objectMapper;
    private final AesGcmService aesGcmService;
    private final QrcodeService qrcodeService;

    public AssetService(AssetBaseMapper assetBaseMapper, AssetChangeHistoryMapper historyMapper, ObjectMapper objectMapper, AesGcmService aesGcmService, QrcodeService qrcodeService) {
        this.assetBaseMapper = assetBaseMapper;
        this.historyMapper = historyMapper;
        this.objectMapper = objectMapper;
        this.aesGcmService = aesGcmService;
        this.qrcodeService = qrcodeService;
    }

    public Page<AssetBase> page(int page, int size, String assetType, String status, String deptId, String userId, String keyword) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        long offset = (long) (current - 1) * pageSize;

        LambdaQueryWrapper<AssetBase> countQ = buildAssetQuery(assetType, status, deptId, userId, keyword, false);
        long total = assetBaseMapper.selectCount(countQ);

        LambdaQueryWrapper<AssetBase> listQ = buildAssetQuery(assetType, status, deptId, userId, keyword, true);
        listQ.last("limit " + pageSize + " offset " + offset);
        var records = assetBaseMapper.selectList(listQ);

        Page<AssetBase> p = new Page<>(current, pageSize);
        p.setTotal(total);
        p.setRecords(records);
        return p;
    }

    public AssetBase getById(String assetId) {
        return assetBaseMapper.selectById(assetId);
    }

    @Transactional
    public AssetBase create(AssetBase in) {
        if (in.getAssetId() == null || in.getAssetId().isBlank()) {
            in.setAssetId(generateAssetId(in.getAssetType()));
        }
        if (assetBaseMapper.selectById(in.getAssetId()) != null) {
            throw new IllegalStateException("资产编号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        in.setCreateTime(now);
        in.setUpdateTime(now);

        if (in.getStatus() == null || in.getStatus().isBlank()) {
            in.setStatus("闲置");
        }

        refreshQrcode(in);
        assetBaseMapper.insert(in);
        insertHistory(in.getAssetId(), "录入", "{}", toJson(in), operatorId(), null, now, "资产录入");
        return in;
    }

    @Transactional
    public AssetBase update(String assetId, AssetBase patch) {
        AssetBase existing = assetBaseMapper.selectById(assetId);
        if (existing == null) {
            throw new IllegalStateException("资产不存在");
        }

        String oldJson = toJson(existing);
        merge(existing, patch);
        existing.setUpdateTime(LocalDateTime.now());
        refreshQrcode(existing);
        assetBaseMapper.updateById(existing);
        insertHistory(assetId, "修改", oldJson, toJson(existing), operatorId(), null, existing.getUpdateTime(), "资产信息修改");
        return existing;
    }

    @Transactional
    public void changeStatus(String assetId, ChangeRequest req) {
        AssetBase existing = assetBaseMapper.selectById(assetId);
        if (existing == null) {
            throw new IllegalStateException("资产不存在");
        }

        String oldJson = toJson(existing);

        if (req.deptId() != null) {
            existing.setDeptId(req.deptId());
        }
        if (req.userId() != null) {
            existing.setUserId(req.userId());
        }
        if (req.newStatus() != null) {
            existing.setStatus(req.newStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        refreshQrcode(existing);
        assetBaseMapper.updateById(existing);

        insertHistory(assetId, req.changeType(), oldJson, toJson(existing), operatorId(), req.approverId(), existing.getUpdateTime(), req.reason());
    }

    public Page<AssetChangeHistory> history(String assetId, int page, int size) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        long offset = (long) (current - 1) * pageSize;

        LambdaQueryWrapper<AssetChangeHistory> countQ = new LambdaQueryWrapper<AssetChangeHistory>()
                .eq(AssetChangeHistory::getAssetId, assetId);
        long total = historyMapper.selectCount(countQ);

        LambdaQueryWrapper<AssetChangeHistory> listQ = new LambdaQueryWrapper<AssetChangeHistory>()
                .eq(AssetChangeHistory::getAssetId, assetId)
                .orderByDesc(AssetChangeHistory::getChangeTime)
                .last("limit " + pageSize + " offset " + offset);
        var records = historyMapper.selectList(listQ);

        Page<AssetChangeHistory> p = new Page<>(current, pageSize);
        p.setTotal(total);
        p.setRecords(records);
        return p;
    }

    private LambdaQueryWrapper<AssetBase> buildAssetQuery(String assetType, String status, String deptId, String userId, String keyword, boolean withOrder) {
        var q = new LambdaQueryWrapper<AssetBase>();
        if (assetType != null && !assetType.isBlank()) {
            q.eq(AssetBase::getAssetType, assetType);
        }
        if (status != null && !status.isBlank()) {
            q.eq(AssetBase::getStatus, status);
        }
        if (deptId != null && !deptId.isBlank()) {
            q.eq(AssetBase::getDeptId, deptId);
        }
        if (userId != null && !userId.isBlank()) {
            q.eq(AssetBase::getUserId, userId);
        }
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(AssetBase::getAssetName, keyword).or().like(AssetBase::getModel, keyword));
        }
        if (withOrder) {
            q.orderByDesc(AssetBase::getUpdateTime);
        }
        return q;
    }

    private void refreshQrcode(AssetBase asset) {
        String payload = toJson(Map.of(
                "assetId", asset.getAssetId(),
                "assetName", asset.getAssetName(),
                "model", asset.getModel(),
                "status", asset.getStatus(),
                "deptId", asset.getDeptId(),
                "userId", asset.getUserId(),
                "updateTime", Objects.toString(asset.getUpdateTime(), null)
        ));
        String encrypted = aesGcmService.encryptToBase64(payload);
        var qr = qrcodeService.generateAndStore(asset.getAssetId(), encrypted);
        asset.setQrcodeContent(encrypted);
        asset.setQrcodeUrl(qr.url());
    }

    private void merge(AssetBase target, AssetBase patch) {
        if (patch.getAssetType() != null) target.setAssetType(patch.getAssetType());
        if (patch.getAssetName() != null) target.setAssetName(patch.getAssetName());
        if (patch.getModel() != null) target.setModel(patch.getModel());
        if (patch.getSpecification() != null) target.setSpecification(patch.getSpecification());
        if (patch.getPurchaseDate() != null) target.setPurchaseDate(patch.getPurchaseDate());
        if (patch.getPurchasePrice() != null) target.setPurchasePrice(patch.getPurchasePrice());
        if (patch.getSupplier() != null) target.setSupplier(patch.getSupplier());
        if (patch.getDeptId() != null) target.setDeptId(patch.getDeptId());
        if (patch.getUserId() != null) target.setUserId(patch.getUserId());
        if (patch.getStatus() != null) target.setStatus(patch.getStatus());
        if (patch.getImageUrl() != null) target.setImageUrl(patch.getImageUrl());
        if (patch.getRemark() != null) target.setRemark(patch.getRemark());
    }

    private void insertHistory(String assetId, String changeType, String oldInfo, String newInfo, String operatorId, String approverId, LocalDateTime time, String reason) {
        AssetChangeHistory h = new AssetChangeHistory();
        h.setHistoryId(IdUtil.uuid32());
        h.setAssetId(assetId);
        h.setChangeType(changeType);
        h.setOldInfo(oldInfo);
        h.setNewInfo(newInfo);
        h.setOperatorId(operatorId);
        h.setApproverId(approverId);
        h.setChangeTime(time);
        h.setReason(reason);
        historyMapper.insert(h);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("JSON序列化失败");
        }
    }

    private String operatorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
            return "system";
        }
        return p.getUserId();
    }

    private String generateAssetId(String assetType) {
        String prefix = assetType == null || assetType.isBlank() ? "ASSET" : assetType.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return prefix + "-" + IdUtil.uuid32().substring(0, 8);
    }

    public record ChangeRequest(String changeType, String newStatus, String deptId, String userId, String approverId, String reason) {
    }
}
