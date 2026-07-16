package com.openclassrooms.mddapi.config;

import java.time.Instant;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.openclassrooms.mddapi.dto.ApiErrorResponse;

import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Central Spring Security configuration.
 * <p>
 * Configures stateless JWT authentication: CSRF disabled (no cookie-based
 * session to protect), CORS restricted to the front-end origin, a JSON
 * {@link AuthenticationEntryPoint} for unauthenticated requests, and
 * {@link JwtAuthenticationFilter} wired in before the standard username/password
 * filter. Only the auth endpoints (register/login/refresh/logout) and the
 * Swagger/OpenAPI documentation are public; everything else requires a valid
 * bearer token.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Builds the HTTP security filter chain applied to every request.
     *
     * @param http                      the security configuration builder
     * @param authenticationEntryPoint  the entry point invoked for unauthenticated requests
     * @return the assembled filter chain
     */
    @Bean
    @SuppressWarnings("java:S4502") // stateless JWT auth, no cookie-based session: CSRF protection doesn't apply here
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh",
                                "/api/auth/logout")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Writes a JSON {@link com.openclassrooms.mddapi.dto.ApiErrorResponse} with a
     * 401 status whenever an unauthenticated request hits a protected endpoint.
     *
     * @param objectMapper the mapper used to serialize the error body
     * @return the JSON-emitting authentication entry point
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiErrorResponse body = new ApiErrorResponse(
                    Instant.now(),
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "AUTH_UNAUTHENTICATED",
                    "Authentication is required to access this resource",
                    request.getRequestURI());
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    /**
     * Restricts cross-origin requests to the Angular dev server origin.
     *
     * @return the CORS configuration applied to every route
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * @return the BCrypt encoder used to hash and verify user passwords
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes Spring Security's default {@link AuthenticationManager} as a bean
     * so it can be injected into {@link com.openclassrooms.mddapi.service.AuthService}.
     *
     * @param config the authentication configuration to source the manager from
     * @return the authentication manager
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * @param passwordEncoder the encoder used to verify stored password hashes
     * @return the provider that authenticates users against {@link UserDetailsService}
     */
    @Bean
    DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
