package com.zcxt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.security.JwtService;
import com.zcxt.security.UserPrincipal;
import com.zcxt.system.entity.SysUser;
import com.zcxt.system.mapper.SysUserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SysUserMapper sysUserMapper;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, SysUserMapper sysUserMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.sysUserMapper = sysUserMapper;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.issueToken(principal.getUsername(), Map.of(
                    "uid", principal.getUserId(),
                    "rid", principal.getRoleId()
            ));

            SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, principal.getUserId()));
            return ApiResponse.ok(new LoginResponse(token, user.getUserId(), user.getUsername(), user.getDisplayName(), user.getRoleId(), user.getDeptId()));
        } catch (AuthenticationException e) {
            return ApiResponse.fail("用户名或密码错误");
        }
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, String userId, String username, String displayName, String roleId, String deptId) {
    }
}
