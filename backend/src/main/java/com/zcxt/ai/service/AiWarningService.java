package com.zcxt.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.ai.entity.AiWarning;
import com.zcxt.ai.mapper.AiWarningMapper;
import com.zcxt.common.IdUtil;
import com.zcxt.config.AppProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiWarningService {
    private final AppProperties appProperties;
    private final RestClient restClient;
    private final AiWarningMapper warningMapper;

    public AiWarningService(AppProperties appProperties, RestClient restClient, AiWarningMapper warningMapper) {
        this.appProperties = appProperties;
        this.restClient = restClient;
        this.warningMapper = warningMapper;
    }

    @Scheduled(cron = "0 0 0 */3 * *")
    public void scheduledSync() {
        if (!appProperties.getAi().isEnabled()) {
            return;
        }
        syncWarnings();
    }

    @Transactional
    public int syncWarnings() {
        if (!appProperties.getAi().isEnabled() || appProperties.getAi().getBaseUrl() == null) {
            return 0;
        }

        String url = appProperties.getAi().getBaseUrl() + "/warnings";
        WarningItem[] items = restClient.get().uri(url).retrieve().body(WarningItem[].class);
        if (items == null || items.length == 0) {
            return 0;
        }

        int inserted = 0;
        for (WarningItem item : items) {
            if (item == null || item.type() == null || item.content() == null) {
                continue;
            }
            long count = warningMapper.selectCount(new LambdaQueryWrapper<AiWarning>()
                    .eq(AiWarning::getWarningType, item.type())
                    .eq(AiWarning::getContent, item.content())
                    .ge(AiWarning::getWarningTime, LocalDateTime.now().minusHours(1)));
            if (count > 0) {
                continue;
            }

            AiWarning w = new AiWarning();
            w.setWarningId(IdUtil.uuid32());
            w.setWarningType(item.type());
            w.setContent(item.content());
            w.setWarningTime(LocalDateTime.now());
            w.setStatus("未处理");
            warningMapper.insert(w);
            inserted++;
        }
        return inserted;
    }

    public List<AiWarning> latest(int limit) {
        int l = Math.min(Math.max(limit, 1), 100);
        return warningMapper.selectList(new LambdaQueryWrapper<AiWarning>()
                .orderByDesc(AiWarning::getWarningTime)
                .last("limit " + l));
    }

    public List<IdleRecommendationItem> getIdleRecommendations() {
        if (!appProperties.getAi().isEnabled() || appProperties.getAi().getBaseUrl() == null) {
            return List.of();
        }
        String url = appProperties.getAi().getBaseUrl() + "/recommendations/idle";
        try {
            IdleRecommendationItem[] items = restClient.get().uri(url).retrieve().body(IdleRecommendationItem[].class);
            return items != null ? List.of(items) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<ConsumablePredictionItem> getConsumablePredictions() {
        if (!appProperties.getAi().isEnabled() || appProperties.getAi().getBaseUrl() == null) {
            return List.of();
        }
        String url = appProperties.getAi().getBaseUrl() + "/predictions/consumables";
        try {
            ConsumablePredictionItem[] items = restClient.get().uri(url).retrieve().body(ConsumablePredictionItem[].class);
            return items != null ? List.of(items) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public record WarningItem(String type, String content) {
    }

    public record IdleRecommendationItem(String asset_id, String asset_name, Integer idle_days, String recommendation) {
    }

    public record ConsumablePredictionItem(String date, Integer predicted_usage) {
    }
}
