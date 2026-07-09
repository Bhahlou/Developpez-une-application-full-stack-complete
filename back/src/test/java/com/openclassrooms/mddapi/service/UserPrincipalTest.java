package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import com.openclassrooms.mddapi.model.User;

class UserPrincipalTest {

    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("johndoe").email("john@doe.com").password("encoded-password").build();
        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void getUser_returnsWrappedUser() {
        assertThat(userPrincipal.getUser()).isEqualTo(user);
    }

    @Test
    void getAuthorities_returnsRoleUser() {
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority authority : userPrincipal.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }

        assertThat(authorities).containsExactly("ROLE_USER");
    }

    @Test
    void getPassword_delegatesToUser() {
        assertThat(userPrincipal.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void getUsername_delegatesToUser() {
        assertThat(userPrincipal.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void accountAndCredentialFlags_areAlwaysTrue() {
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
        assertThat(userPrincipal.isEnabled()).isTrue();
    }
}
