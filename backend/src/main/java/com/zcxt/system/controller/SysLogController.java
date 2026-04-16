package com.zcxt.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.system.entity.SysLog;
import com.zcxt.system.service.SysLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/logs")
public class SysLogController {
    private final SysLogService sysLogService;

    public SysLogController(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }

    @GetMapping
    public ApiResponse<Page<SysLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation
    ) {
        return ApiResponse.ok(sysLogService.page(page, size, username, operation));
    }
}
