package com.openclassrooms.mddapi.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Bridges {@link User} entities into Spring Security's {@link UserDetails}
 * abstraction, used by the authentication manager and the JWT filter.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @param identifier a username or email, since MDD allows logging in with either
     * @return the matching user, wrapped as a {@link UserPrincipal}
     * @throws UsernameNotFoundException if no user matches the identifier
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(identifier, identifier)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user found for identifier: " + identifier));
    }
}
