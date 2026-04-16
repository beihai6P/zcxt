package com.zcxt.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcxt.common.IdUtil;
import com.zcxt.system.entity.SysLog;
import com.zcxt.system.mapper.SysLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SysLogService {
    private final SysLogMapper sysLogMapper;

    public SysLogService(SysLogMapper sysLogMapper) {
        this.sysLogMapper = sysLogMapper;
    }

    public Page<SysLog> page(int page, int size, String username, String operation) {
        int current = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        
        LambdaQueryWrapper<SysLog> q = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            q.like(SysLog::getUsername, username);
        }
        if (operation != null && !operation.isBlank()) {
            q.like(SysLog::getOperation, operation);
        }
        q.orderByDesc(SysLog::getCreateTime);

        Page<SysLog> p = new Page<>(current, pageSize);
        return sysLogMapper.selectPage(p, q);
    }

    @Transactional
    public void save(SysLog sysLog) {
        sysLog.setLogId(IdUtil.uuid32());
        sysLog.setCreateTime(LocalDateTime.now());
        sysLogMapper.insert(sysLog);
    }
}
