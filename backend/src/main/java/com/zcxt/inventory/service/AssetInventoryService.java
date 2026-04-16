package com.zcxt.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.IdUtil;
import com.zcxt.inventory.entity.AssetInventory;
import com.zcxt.inventory.entity.AssetInventoryDetail;
import com.zcxt.inventory.mapper.AssetInventoryDetailMapper;
import com.zcxt.inventory.mapper.AssetInventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssetInventoryService {
    private final AssetInventoryMapper inventoryMapper;
    private final AssetInventoryDetailMapper detailMapper;

    public AssetInventoryService(AssetInventoryMapper inventoryMapper, AssetInventoryDetailMapper detailMapper) {
        this.inventoryMapper = inventoryMapper;
        this.detailMapper = detailMapper;
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
}
