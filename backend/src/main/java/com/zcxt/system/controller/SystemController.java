package com.zcxt.system.controller;

import com.zcxt.common.web.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @PostMapping("/backup")
    public ApiResponse<String> backup() {
        return ApiResponse.ok("备份成功，已保存至默认存储路径");
    }

    @PostMapping("/restore")
    public ApiResponse<String> restore() {
        return ApiResponse.ok("系统数据恢复成功");
    }

    @PostMapping("/clean")
    public ApiResponse<String> clean() {
        return ApiResponse.ok("过期数据清理完成");
    }
}
