package com.zcxt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zcxt.common.web.ApiResponse;
import com.zcxt.system.entity.SysRole;
import com.zcxt.system.entity.SysUser;
import com.zcxt.system.mapper.SysRoleMapper;
import com.zcxt.system.mapper.SysUserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<List<SysUser>> getUsers() {
        return ApiResponse.ok(sysUserMapper.selectList(Wrappers.emptyWrapper()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<SysUser> createUser(@Valid @RequestBody CreateUserRequest request) {
        SysUser user = new SysUser();
        user.setUserId("user-" + System.currentTimeMillis());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setDeptId(request.deptId());
        user.setRoleId(request.roleId());
        user.setEmail(request.email());
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<SysUser> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        user.setDisplayName(request.displayName());
        user.setDeptId(request.deptId());
        user.setRoleId(request.roleId());
        user.setEmail(request.email());
        user.setEnabled(request.enabled());
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return ApiResponse.ok(user);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<Void> deleteUser(@PathVariable String userId) {
        sysUserMapper.deleteById(userId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('sys:manage')")
    public ApiResponse<List<SysRole>> getRoles() {
        return ApiResponse.ok(sysRoleMapper.selectList(Wrappers.emptyWrapper()));
    }

    public record CreateUserRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String displayName,
            String deptId,
            @NotBlank String roleId,
            String email
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank String displayName,
            String deptId,
            @NotBlank String roleId,
            String email,
            boolean enabled,
            String password
    ) {
    }
}
