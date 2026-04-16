package com.zcxt.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final String roleId;

    public UserPrincipal(String userId, String username, String passwordHash, boolean enabled, String roleId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roleId = roleId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoleId() {
        return roleId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleId));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

