package com.zcxt.consumable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.IdUtil;
import com.zcxt.consumable.entity.AssetConsumableStock;
import com.zcxt.consumable.mapper.AssetConsumableStockMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssetConsumableStockService {
    private final AssetConsumableStockMapper stockMapper;

    public AssetConsumableStockService(AssetConsumableStockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    public Page<AssetConsumableStock> page(int page, int size, String keyword) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<AssetConsumableStock> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.like(AssetConsumableStock::getConsumableName, keyword)
                 .or()
                 .like(AssetConsumableStock::getConsumableType, keyword);
        }
        query.orderByDesc(AssetConsumableStock::getUpdateTime);

        return stockMapper.selectPage(new Page<>(current, pageSize), query);
    }

    public AssetConsumableStock getById(String consumableId) {
        return stockMapper.selectById(consumableId);
    }

    @Transactional
    public AssetConsumableStock create(AssetConsumableStock in) {
        in.setConsumableId(IdUtil.uuid32());
        LocalDateTime now = LocalDateTime.now();
        in.setCreateTime(now);
        in.setUpdateTime(now);
        if (in.getStockQuantity() == null) {
            in.setStockQuantity(0);
        }
        if (in.getWarningThreshold() == null) {
            in.setWarningThreshold(10);
        }
        stockMapper.insert(in);
        return in;
    }

    @Transactional
    public AssetConsumableStock update(String consumableId, AssetConsumableStock patch) {
        AssetConsumableStock existing = stockMapper.selectById(consumableId);
        if (existing == null) {
            throw new IllegalStateException("耗材不存在");
        }
        if (patch.getConsumableType() != null) existing.setConsumableType(patch.getConsumableType());
        if (patch.getConsumableName() != null) existing.setConsumableName(patch.getConsumableName());
        if (patch.getStockQuantity() != null) existing.setStockQuantity(patch.getStockQuantity());
        if (patch.getWarningThreshold() != null) existing.setWarningThreshold(patch.getWarningThreshold());
        
        existing.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(existing);
        return existing;
    }
}
