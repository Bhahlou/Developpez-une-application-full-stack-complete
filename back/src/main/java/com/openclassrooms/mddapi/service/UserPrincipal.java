package com.openclassrooms.mddapi.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.openclassrooms.mddapi.model.User;

import lombok.Getter;

/**
 * Adapts a {@link User} entity to Spring Security's {@link UserDetails},
 * so the domain entity can be retrieved from the security context via
 * {@code @AuthenticationPrincipal} in controllers.
 * <p>
 * MDD has a single role: every authenticated user is {@code ROLE_USER}, and
 * accounts never expire, lock, or need re-authentication.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final User user;

    /**
     * @param user the wrapped domain user
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
        return true;
    }
}
