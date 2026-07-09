package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_returnsUserPrincipal_whenUserFoundByUsernameOrEmail() {
        User user = User.builder().id(1L).username("johndoe").email("john@doe.com").password("encoded").build();
        when(userRepository.findByUsernameOrEmail("johndoe", "johndoe")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(((UserPrincipal) result).getUser()).isEqualTo(user);
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenNoUserMatches() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
