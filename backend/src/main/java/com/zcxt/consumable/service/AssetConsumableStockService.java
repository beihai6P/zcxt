package com.zcxt.consumable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.IdUtil;
import com.zcxt.consumable.entity.AssetConsumableStock;
import com.zcxt.consumable.entity.ConsumableTxn;
import com.zcxt.consumable.mapper.AssetConsumableStockMapper;
import com.zcxt.consumable.mapper.ConsumableTxnMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssetConsumableStockService {
    private final AssetConsumableStockMapper stockMapper;
    private final ConsumableTxnMapper txnMapper;

    public AssetConsumableStockService(AssetConsumableStockMapper stockMapper, ConsumableTxnMapper txnMapper) {
        this.stockMapper = stockMapper;
        this.txnMapper = txnMapper;
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

    @Transactional
    public void inbound(String consumableId, int qty, String remark, String operatorId) {
        if (qty <= 0) {
            throw new IllegalStateException("数量必须大于0");
        }
        AssetConsumableStock c = stockMapper.selectById(consumableId);
        if (c == null) {
            throw new IllegalStateException("耗材不存在");
        }
        c.setStockQuantity(c.getStockQuantity() + qty);
        c.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(c);

        ConsumableTxn t = new ConsumableTxn();
        t.setTxnId(IdUtil.uuid32());
        t.setConsumableId(consumableId);
        t.setTxnType("IN");
        t.setQuantity(qty);
        t.setUserId(operatorId);
        t.setRemark(remark);
        t.setTxnTime(LocalDateTime.now());
        txnMapper.insert(t);
    }

    @Transactional
    public void outbound(String consumableId, int qty, String deptId, String userId, String remark, String operatorId) {
        if (qty <= 0) {
            throw new IllegalStateException("数量必须大于0");
        }
        AssetConsumableStock c = stockMapper.selectById(consumableId);
        if (c == null) {
            throw new IllegalStateException("耗材不存在");
        }
        if (c.getStockQuantity() < qty) {
            throw new IllegalStateException("库存不足");
        }
        c.setStockQuantity(c.getStockQuantity() - qty);
        c.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(c);

        ConsumableTxn t = new ConsumableTxn();
        t.setTxnId(IdUtil.uuid32());
        t.setConsumableId(consumableId);
        t.setTxnType("OUT");
        t.setQuantity(qty);
        t.setDeptId(deptId);
        t.setUserId(userId);
        t.setRemark(remark);
        t.setTxnTime(LocalDateTime.now());
        txnMapper.insert(t);
    }

    public Page<ConsumableTxn> txns(String consumableId, int page, int size) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<ConsumableTxn> q = new LambdaQueryWrapper<ConsumableTxn>()
                .eq(ConsumableTxn::getConsumableId, consumableId)
                .orderByDesc(ConsumableTxn::getTxnTime);
        return txnMapper.selectPage(new Page<>(current, pageSize), q);
    }

    public List<DailyUsage> usageTrend(int days) {
        int d = Math.min(Math.max(days, 1), 90);
        LocalDate from = LocalDate.now().minusDays(d - 1L);
        LocalDateTime fromTime = from.atStartOfDay();
        List<ConsumableTxn> txns = txnMapper.selectList(new LambdaQueryWrapper<ConsumableTxn>()
                .ge(ConsumableTxn::getTxnTime, fromTime)
                .in(ConsumableTxn::getTxnType, List.of("OUT"))
                .orderByAsc(ConsumableTxn::getTxnTime));
        Map<LocalDate, Integer> map = txns.stream().collect(Collectors.groupingBy(
                t -> t.getTxnTime().toLocalDate(),
                Collectors.summingInt(ConsumableTxn::getQuantity)
        ));
        java.util.ArrayList<DailyUsage> out = new java.util.ArrayList<>();
        for (int i = 0; i < d; i++) {
            LocalDate day = from.plusDays(i);
            out.add(new DailyUsage(day.toString(), map.getOrDefault(day, 0)));
        }
        return out;
    }

    public record DailyUsage(String date, int quantity) {
    }
}
