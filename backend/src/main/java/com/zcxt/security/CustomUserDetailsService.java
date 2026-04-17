package com.zcxt.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.system.entity.SysUser;
import com.zcxt.system.mapper.SysPermissionMapper;
import com.zcxt.system.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public CustomUserDetailsService(SysUserMapper sysUserMapper, SysPermissionMapper sysPermissionMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        var authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleId()));
        for (String perm : sysPermissionMapper.selectPermissionKeysByRoleId(user.getRoleId())) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }
        return new UserPrincipal(user.getUserId(), user.getUsername(), user.getPasswordHash(), user.isEnabled(), user.getRoleId(), authorities);
    }
}
