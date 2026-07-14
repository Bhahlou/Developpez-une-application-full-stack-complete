package com.openclassrooms.mddapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.User;

/**
 * Spring Data JPA repository for {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Used for login, where the identifier may be either a username or an email.
     *
     * @param username the username to match
     * @param email    the email to match
     * @return the matching user, if any
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * @param refreshToken the refresh token to look up
     * @return the user currently holding this refresh token, if any
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * @param username the username to check
     * @return {@code true} if a user with this username already exists
     */
    boolean existsByUsername(String username);

    /**
     * @param email the email to check
     * @return {@code true} if a user with this email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Used when updating a profile, to allow a user to keep their own username.
     *
     * @param username the username to check
     * @param id       the id of the user being updated, excluded from the check
     * @return {@code true} if another user already has this username
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * Used when updating a profile, to allow a user to keep their own email.
     *
     * @param email the email to check
     * @param id    the id of the user being updated, excluded from the check
     * @return {@code true} if another user already has this email
     */
    boolean existsByEmailAndIdNot(String email, Long id);
}
