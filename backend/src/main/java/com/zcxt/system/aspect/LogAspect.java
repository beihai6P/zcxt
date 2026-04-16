package com.zcxt.system.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcxt.security.UserPrincipal;
import com.zcxt.system.annotation.LogOperation;
import com.zcxt.system.entity.SysLog;
import com.zcxt.system.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {

    private final SysLogService sysLogService;
    private final ObjectMapper objectMapper;

    public LogAspect(SysLogService sysLogService, ObjectMapper objectMapper) {
        this.sysLogService = sysLogService;
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(com.zcxt.system.annotation.LogOperation)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = point.proceed();
        long time = System.currentTimeMillis() - beginTime;
        saveLog(point, time);
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SysLog sysLog = new SysLog();
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        if (logOperation != null) {
            sysLog.setOperation(logOperation.value());
        }

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");

        Object[] args = joinPoint.getArgs();
        try {
            if (args.length > 0) {
                String params = objectMapper.writeValueAsString(args[0]);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
                sysLog.setParams(params);
            }
        } catch (Exception e) {
            sysLog.setParams("无法序列化参数");
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        sysLog.setIp(getIpAddr(request));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            sysLog.setUserId(p.getUserId());
            sysLog.setUsername(p.getUsername());
        } else {
            sysLog.setUserId("system");
            sysLog.setUsername("system");
        }

        sysLog.setTime(time);
        sysLogService.save(sysLog);
    }

    private String getIpAddr(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
