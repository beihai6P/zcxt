package com.zcxt.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.asset.entity.AssetAssembly;
import com.zcxt.asset.mapper.AssetAssemblyMapper;
import com.zcxt.common.IdUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssetAssemblyService {
    private final AssetAssemblyMapper assetAssemblyMapper;

    public AssetAssemblyService(AssetAssemblyMapper assetAssemblyMapper) {
        this.assetAssemblyMapper = assetAssemblyMapper;
    }

    public Page<AssetAssembly> page(int page, int size, String assetId) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<AssetAssembly> q = new LambdaQueryWrapper<>();
        if (assetId != null && !assetId.isBlank()) {
            q.eq(AssetAssembly::getAssetId, assetId);
        }
        q.orderByDesc(AssetAssembly::getCreateTime);

        Page<AssetAssembly> p = new Page<>(current, pageSize);
        return assetAssemblyMapper.selectPage(p, q);
    }

    @Transactional
    public void create(AssetAssembly assembly) {
        assembly.setAssemblyId(IdUtil.uuid32());
        assembly.setCreateTime(LocalDateTime.now());
        assetAssemblyMapper.insert(assembly);
    }
}
