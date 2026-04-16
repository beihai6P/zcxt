package com.zcxt.stats.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.stats.entity.AssetStatsDaily;
import com.zcxt.stats.mapper.AssetStatsDailyMapper;
import com.zcxt.stats.service.AssetStatsService;
import com.zcxt.ai.service.AiWarningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final AssetStatsService statsService;
    private final AssetStatsDailyMapper dailyMapper;
    private final AiWarningService aiWarningService;

    public StatsController(AssetStatsService statsService, AssetStatsDailyMapper dailyMapper, AiWarningService aiWarningService) {
        this.statsService = statsService;
        this.dailyMapper = dailyMapper;
        this.aiWarningService = aiWarningService;
    }

    @GetMapping("/today")
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
    public ApiResponse<List<AssetStatsDaily>> daily(@RequestParam(defaultValue = "30") int days) {
        LocalDate from = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        List<AssetStatsDaily> list = dailyMapper.selectList(new LambdaQueryWrapper<AssetStatsDaily>()
                .ge(AssetStatsDaily::getStatDate, from)
                .orderByAsc(AssetStatsDaily::getStatDate));
        return ApiResponse.ok(list);
    }

    @GetMapping("/ai/idle-recommendations")
    public ApiResponse<List<AiWarningService.IdleRecommendationItem>> idleRecommendations() {
        return ApiResponse.ok(aiWarningService.getIdleRecommendations());
    }

    @GetMapping("/ai/consumable-predictions")
    public ApiResponse<List<AiWarningService.ConsumablePredictionItem>> consumablePredictions() {
        return ApiResponse.ok(aiWarningService.getConsumablePredictions());
    }
}

