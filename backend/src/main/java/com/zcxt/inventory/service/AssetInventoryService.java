package com.zcxt.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.mapper.AssetBaseMapper;
import com.zcxt.common.IdUtil;
import com.zcxt.inventory.entity.AssetInventory;
import com.zcxt.inventory.entity.AssetInventoryDetail;
import com.zcxt.inventory.mapper.AssetInventoryDetailMapper;
import com.zcxt.inventory.mapper.AssetInventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetInventoryService {
    private final AssetInventoryMapper inventoryMapper;
    private final AssetInventoryDetailMapper detailMapper;
    private final AssetBaseMapper assetBaseMapper;

    public AssetInventoryService(AssetInventoryMapper inventoryMapper, AssetInventoryDetailMapper detailMapper, AssetBaseMapper assetBaseMapper) {
        this.inventoryMapper = inventoryMapper;
        this.detailMapper = detailMapper;
        this.assetBaseMapper = assetBaseMapper;
    }

    public Page<AssetInventory> page(int page, int size, String keyword) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<AssetInventory> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.like(AssetInventory::getInventoryName, keyword);
        }
        query.orderByDesc(AssetInventory::getCreateTime);

        return inventoryMapper.selectPage(new Page<>(current, pageSize), query);
    }

    public AssetInventory getById(String inventoryId) {
        return inventoryMapper.selectById(inventoryId);
    }

    @Transactional
    public AssetInventory create(AssetInventory in) {
        in.setInventoryId(IdUtil.uuid32());
        LocalDateTime now = LocalDateTime.now();
        in.setCreateTime(now);
        in.setUpdateTime(now);
        
        if (in.getStatus() == null || in.getStatus().isBlank()) {
            in.setStatus("进行中");
        }
        
        inventoryMapper.insert(in);
        generateDetails(in, now);
        return in;
    }

    @Transactional
    public AssetInventory update(String inventoryId, AssetInventory patch) {
        AssetInventory existing = inventoryMapper.selectById(inventoryId);
        if (existing == null) {
            throw new IllegalStateException("盘点任务不存在");
        }
        if (patch.getInventoryName() != null) existing.setInventoryName(patch.getInventoryName());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        
        inventoryMapper.updateById(existing);
        return existing;
    }

    public Page<AssetInventoryDetail> getDetails(String inventoryId, int page, int size) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        
        LambdaQueryWrapper<AssetInventoryDetail> query = new LambdaQueryWrapper<>();
        query.eq(AssetInventoryDetail::getInventoryId, inventoryId);
        
        return detailMapper.selectPage(new Page<>(current, pageSize), query);
    }

    @Transactional
    public void check(String inventoryId, String assetId, String status, String abnormalType, String abnormalReason, String checkerId) {
        AssetInventory inv = inventoryMapper.selectById(inventoryId);
        if (inv == null) {
            throw new IllegalStateException("盘点任务不存在");
        }
        if (!"进行中".equals(inv.getStatus())) {
            throw new IllegalStateException("盘点任务非进行中状态");
        }

        AssetInventoryDetail d = detailMapper.selectOne(new LambdaQueryWrapper<AssetInventoryDetail>()
                .eq(AssetInventoryDetail::getInventoryId, inventoryId)
                .eq(AssetInventoryDetail::getAssetId, assetId));
        if (d == null) {
            throw new IllegalStateException("资产不在本次盘点范围内");
        }
        d.setStatus(status);
        d.setAbnormalType(abnormalType);
        d.setAbnormalReason(abnormalReason);
        d.setCheckerId(checkerId);
        d.setCheckTime(LocalDateTime.now());
        detailMapper.updateById(d);
    }

    @Transactional
    public void finish(String inventoryId) {
        AssetInventory inv = inventoryMapper.selectById(inventoryId);
        if (inv == null) {
            throw new IllegalStateException("盘点任务不存在");
        }
        inv.setStatus("已完成");
        inv.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inv);
    }

    private void generateDetails(AssetInventory inv, LocalDateTime now) {
        LambdaQueryWrapper<AssetBase> q = new LambdaQueryWrapper<>();
        String scope = inv.getScopeType();
        if ("部门盘点".equals(scope)) {
            if (inv.getDeptId() == null || inv.getDeptId().isBlank()) {
                throw new IllegalStateException("部门盘点需要填写部门ID");
            }
            q.eq(AssetBase::getDeptId, inv.getDeptId());
        } else if ("分类盘点".equals(scope)) {
            if (inv.getAssetType() == null || inv.getAssetType().isBlank()) {
                throw new IllegalStateException("分类盘点需要填写资产类型");
            }
            q.eq(AssetBase::getAssetType, inv.getAssetType());
        }
        List<AssetBase> assets = assetBaseMapper.selectList(q);
        for (AssetBase a : assets) {
            AssetInventoryDetail d = new AssetInventoryDetail();
            d.setDetailId(IdUtil.uuid32());
            d.setInventoryId(inv.getInventoryId());
            d.setAssetId(a.getAssetId());
            d.setStatus("待盘点");
            detailMapper.insert(d);
        }
    }
}
