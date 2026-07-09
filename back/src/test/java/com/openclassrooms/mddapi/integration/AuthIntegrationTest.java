package com.openclassrooms.mddapi.integration;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RefreshRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    AuthIntegrationTest(MockMvc mockMvc, UserRepository userRepository) {
        super(mockMvc, userRepository);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_persistsUserWithEncodedPassword_andReturnsTokens() throws Exception {
        RegisterRequest request = new RegisterRequest("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        User saved = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("john@doe.com");
        assertThat(saved.getPassword()).isNotEqualTo("Passw0rd!");
    }

    @Test
    void register_returns409_whenUsernameAlreadyTaken() throws Exception {
        registerUser("johndoe", "john@doe.com", "Passw0rd!");

        RegisterRequest duplicate = new RegisterRequest("johndoe", "other@doe.com", "Passw0rd!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_USERNAME_TAKEN"));
    }

    @Test
    void register_returns409_whenEmailAlreadyTaken() throws Exception {
        registerUser("johndoe", "john@doe.com", "Passw0rd!");

        RegisterRequest duplicate = new RegisterRequest("janedoe", "john@doe.com", "Passw0rd!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_EMAIL_TAKEN"));
    }

    @Test
    void login_returns200WithTokens_whenCredentialsValid() throws Exception {
        registerUser("johndoe", "john@doe.com", "Passw0rd!");

        LoginRequest request = new LoginRequest("johndoe", "Passw0rd!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void login_returns401_whenPasswordIncorrect() throws Exception {
        registerUser("johndoe", "john@doe.com", "Passw0rd!");

        LoginRequest request = new LoginRequest("johndoe", "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"));
    }

    @Test
    void me_returns200WithUserData_whenAccessTokenValid() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@doe.com"));
    }

    @Test
    void me_returns401_whenNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }

    @Test
    void refresh_returnsNewTokens_andRotatesStoredRefreshToken() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        RefreshRequest request = new RefreshRequest(tokens.refreshToken());
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(not(tokens.refreshToken())));
    }

    @Test
    void refresh_returns401_whenTokenUnknown() throws Exception {
        RefreshRequest request = new RefreshRequest("unknown-token");
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void refresh_returns401_whenTokenExpired() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        User user = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow();
        user.setRefreshTokenExpiry(Instant.now().minusSeconds(1));
        userRepository.save(user);

        RefreshRequest request = new RefreshRequest(tokens.refreshToken());
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REFRESH_TOKEN_EXPIRED"));
    }

    @Test
    void logout_revokesRefreshToken_soItCannotBeReusedForRefresh() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(tokens.refreshToken()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(tokens.refreshToken()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMe_persistsChangesAndReturnsFreshTokens_soTheSessionKeepsWorkingAfterAUsernameChange() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        UpdateProfileRequest request = new UpdateProfileRequest("janedoe", "jane@doe.com", "Passw0rd!", "NewPassw0rd!");
        String updateBody = mockMvc.perform(put("/api/auth/me")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(not(tokens.refreshToken())))
                .andReturn().getResponse().getContentAsString();
        AuthResponse updatedTokens = objectMapper.readValue(updateBody, AuthResponse.class);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + updatedTokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.email").value("jane@doe.com"));

        LoginRequest loginRequest = new LoginRequest("janedoe", "NewPassw0rd!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void updateMe_returns401_whenCurrentPasswordIncorrect() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        UpdateProfileRequest request = new UpdateProfileRequest("janedoe", "jane@doe.com", "wrong-password", "");
        mockMvc.perform(put("/api/auth/me")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"));
    }

    @Test
    void updateMe_returns409_whenUsernameAlreadyTakenByAnotherUser() throws Exception {
        registerUser("janedoe", "jane@doe.com", "Passw0rd!");
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        UpdateProfileRequest request = new UpdateProfileRequest("janedoe", "john@doe.com", "Passw0rd!", "");
        mockMvc.perform(put("/api/auth/me")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_USERNAME_TAKEN"));
    }

    @Test
    void updateMe_returns409_whenEmailAlreadyTakenByAnotherUser() throws Exception {
        registerUser("janedoe", "jane@doe.com", "Passw0rd!");
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        UpdateProfileRequest request = new UpdateProfileRequest("johndoe", "jane@doe.com", "Passw0rd!", "");
        mockMvc.perform(put("/api/auth/me")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_EMAIL_TAKEN"));
    }

    @Test
    void updateMe_keepsExistingPassword_whenNewPasswordIsBlank() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        UpdateProfileRequest request = new UpdateProfileRequest("johndoe", "john2@doe.com", "Passw0rd!", "");
        mockMvc.perform(put("/api/auth/me")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("johndoe", "Passw0rd!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
