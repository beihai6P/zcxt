package com.zcxt.stats.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.asset.entity.AssetBase;
import com.zcxt.asset.entity.AssetChangeHistory;
import com.zcxt.asset.mapper.AssetBaseMapper;
import com.zcxt.asset.mapper.AssetChangeHistoryMapper;
import com.zcxt.consumable.service.AssetConsumableStockService;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.stats.entity.AssetStatsDaily;
import com.zcxt.stats.mapper.AssetStatsDailyMapper;
import com.zcxt.stats.service.AssetStatsService;
import com.zcxt.ai.service.AiWarningService;
import com.zcxt.system.entity.SysDept;
import com.zcxt.system.mapper.SysDeptMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final AssetStatsService statsService;
    private final AssetStatsDailyMapper dailyMapper;
    private final AiWarningService aiWarningService;
    private final AssetBaseMapper assetBaseMapper;
    private final AssetChangeHistoryMapper historyMapper;
    private final SysDeptMapper sysDeptMapper;
    private final AssetConsumableStockService consumableStockService;

    public StatsController(AssetStatsService statsService, AssetStatsDailyMapper dailyMapper, AiWarningService aiWarningService, AssetBaseMapper assetBaseMapper, AssetChangeHistoryMapper historyMapper, SysDeptMapper sysDeptMapper, AssetConsumableStockService consumableStockService) {
        this.statsService = statsService;
        this.dailyMapper = dailyMapper;
        this.aiWarningService = aiWarningService;
        this.assetBaseMapper = assetBaseMapper;
        this.historyMapper = historyMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.consumableStockService = consumableStockService;
    }

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<AssetStatsDaily> today(@RequestParam(defaultValue = "false") boolean refresh) {
        if (refresh) {
            return ApiResponse.ok(statsService.refreshToday());
        }
        LocalDate today = LocalDate.now();
        AssetStatsDaily existing = dailyMapper.selectOne(new LambdaQueryWrapper<AssetStatsDaily>().eq(AssetStatsDaily::getStatDate, today));
        if (existing == null) {
            existing = statsService.refreshToday();
        }
        return ApiResponse.ok(existing);
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<AssetStatsDaily>> daily(@RequestParam(defaultValue = "30") int days) {
        LocalDate from = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        List<AssetStatsDaily> list = dailyMapper.selectList(new LambdaQueryWrapper<AssetStatsDaily>()
                .ge(AssetStatsDaily::getStatDate, from)
                .orderByAsc(AssetStatsDaily::getStatDate));
        return ApiResponse.ok(list);
    }

    @GetMapping("/by-dept")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<DeptStats>> byDept() {
        List<SysDept> depts = sysDeptMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> deptNames = depts.stream().collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName, (a, b) -> a));
        List<AssetBase> assets = assetBaseMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, List<AssetBase>> grouped = assets.stream().collect(Collectors.groupingBy(a -> a.getDeptId() == null ? "UNKNOWN" : a.getDeptId()));
        return ApiResponse.ok(grouped.entrySet().stream()
                .map(e -> {
                    String deptId = e.getKey();
                    List<AssetBase> list = e.getValue();
                    int total = list.size();
                    int inUse = (int) list.stream().filter(a -> "在用".equals(a.getStatus())).count();
                    int idle = (int) list.stream().filter(a -> "闲置".equals(a.getStatus())).count();
                    int repairing = (int) list.stream().filter(a -> "维修".equals(a.getStatus())).count();
                    int scrapped = (int) list.stream().filter(a -> "报废".equals(a.getStatus())).count();
                    return new DeptStats(deptId, deptNames.getOrDefault(deptId, deptId), total, inUse, idle, repairing, scrapped);
                })
                .sorted((a, b) -> Integer.compare(b.total(), a.total()))
                .toList());
    }

    @GetMapping("/by-type")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<TypeStats>> byType() {
        List<AssetBase> assets = assetBaseMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Long> grouped = assets.stream().collect(Collectors.groupingBy(a -> a.getAssetType() == null ? "UNKNOWN" : a.getAssetType(), Collectors.counting()));
        return ApiResponse.ok(grouped.entrySet().stream()
                .map(e -> new TypeStats(e.getKey(), e.getValue().intValue()))
                .sorted((a, b) -> Integer.compare(b.total(), a.total()))
                .toList());
    }

    @GetMapping("/change-trend")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<TrendPoint>> changeTrend(@RequestParam(defaultValue = "30") int days) {
        int d = Math.min(Math.max(days, 1), 90);
        LocalDate from = LocalDate.now().minusDays(d - 1L);
        LocalDateTime fromTime = from.atStartOfDay();
        List<AssetChangeHistory> list = historyMapper.selectList(new LambdaQueryWrapper<AssetChangeHistory>()
                .ge(AssetChangeHistory::getChangeTime, fromTime)
                .orderByAsc(AssetChangeHistory::getChangeTime));
        Map<LocalDate, Long> map = list.stream().collect(Collectors.groupingBy(h -> h.getChangeTime().toLocalDate(), Collectors.counting()));
        java.util.ArrayList<TrendPoint> out = new java.util.ArrayList<>();
        for (int i = 0; i < d; i++) {
            LocalDate day = from.plusDays(i);
            out.add(new TrendPoint(day.toString(), map.getOrDefault(day, 0L).intValue()));
        }
        return ApiResponse.ok(out);
    }

    @GetMapping("/consumable-usage")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<AssetConsumableStockService.DailyUsage>> consumableUsage(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(consumableStockService.usageTrend(days));
    }

    @GetMapping("/ai/idle-recommendations")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<AiWarningService.IdleRecommendationItem>> idleRecommendations() {
        return ApiResponse.ok(aiWarningService.getIdleRecommendations());
    }

    @GetMapping("/ai/consumable-predictions")
    @PreAuthorize("hasAuthority('stats:view')")
    public ApiResponse<List<AiWarningService.ConsumablePredictionItem>> consumablePredictions() {
        return ApiResponse.ok(aiWarningService.getConsumablePredictions());
    }

    public record DeptStats(String deptId, String deptName, int total, int inUse, int idle, int repairing, int scrapped) {
    }

    public record TypeStats(String assetType, int total) {
    }

    public record TrendPoint(String date, int count) {
    }
}
