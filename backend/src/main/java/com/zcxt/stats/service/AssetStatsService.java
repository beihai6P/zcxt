package com.zcxt.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.mapper.AssetBaseMapper;
import com.zcxt.common.IdUtil;
import com.zcxt.config.AppProperties;
import com.zcxt.stats.entity.AssetStatsDaily;
import com.zcxt.stats.mapper.AssetStatsDailyMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AssetStatsService {
    private final AppProperties appProperties;
    private final AssetBaseMapper assetBaseMapper;
    private final AssetStatsDailyMapper statsDailyMapper;

    public AssetStatsService(AppProperties appProperties, AssetBaseMapper assetBaseMapper, AssetStatsDailyMapper statsDailyMapper) {
        this.appProperties = appProperties;
        this.assetBaseMapper = assetBaseMapper;
        this.statsDailyMapper = statsDailyMapper;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledRefresh() {
        if (!appProperties.getBigdata().isEnabled()) {
            return;
        }
        refreshToday();
    }

    @Transactional
    public AssetStatsDaily refreshToday() {
        LocalDate today = LocalDate.now();
        int total = Math.toIntExact(assetBaseMapper.selectCount(new LambdaQueryWrapper<>()));
        int inUse = Math.toIntExact(assetBaseMapper.selectCount(new LambdaQueryWrapper<AssetBase>().eq(AssetBase::getStatus, "在用")));
        int idle = Math.toIntExact(assetBaseMapper.selectCount(new LambdaQueryWrapper<AssetBase>().eq(AssetBase::getStatus, "闲置")));
        int repairing = Math.toIntExact(assetBaseMapper.selectCount(new LambdaQueryWrapper<AssetBase>().eq(AssetBase::getStatus, "维修")));
        int scrapped = Math.toIntExact(assetBaseMapper.selectCount(new LambdaQueryWrapper<AssetBase>().eq(AssetBase::getStatus, "报废")));

        AssetStatsDaily existing = statsDailyMapper.selectOne(new LambdaQueryWrapper<AssetStatsDaily>().eq(AssetStatsDaily::getStatDate, today));
        if (existing == null) {
            existing = new AssetStatsDaily();
            existing.setId(IdUtil.uuid32());
            existing.setStatDate(today);
            existing.setCreateTime(LocalDateTime.now());
            existing.setTotal(total);
            existing.setInUse(inUse);
            existing.setIdle(idle);
            existing.setRepairing(repairing);
            existing.setScrapped(scrapped);
            statsDailyMapper.insert(existing);
        } else {
            existing.setTotal(total);
            existing.setInUse(inUse);
            existing.setIdle(idle);
            existing.setRepairing(repairing);
            existing.setScrapped(scrapped);
            statsDailyMapper.updateById(existing);
        }
        return existing;
    }
}
