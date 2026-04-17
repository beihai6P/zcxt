package com.zcxt.ai.controller;

import com.zcxt.ai.entity.AiWarning;
import com.zcxt.ai.service.AiWarningService;
import com.zcxt.common.web.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiWarningController {
    private final AiWarningService warningService;

    public AiWarningController(AiWarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping("/warnings")
    public ApiResponse<List<AiWarning>> warnings(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(warningService.latest(limit));
    }

    @GetMapping("/sync")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<Integer> sync() {
        return ApiResponse.ok(warningService.syncWarnings());
    }
}

