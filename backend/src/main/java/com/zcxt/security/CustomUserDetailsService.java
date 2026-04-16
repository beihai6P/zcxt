package com.zcxt.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcxt.system.entity.SysUser;
import com.zcxt.system.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final SysUserMapper sysUserMapper;

    public CustomUserDetailsService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new UserPrincipal(user.getUserId(), user.getUsername(), user.getPasswordHash(), user.isEnabled(), user.getRoleId());
    }
}

