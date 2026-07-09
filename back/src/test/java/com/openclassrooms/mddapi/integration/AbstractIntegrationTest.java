package com.openclassrooms.mddapi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.repository.UserRepository;

/**
 * Shared base for full-stack integration tests: real Spring context, real MySQL (Testcontainers), no mocks.
 * The container is started once here (singleton pattern) instead of letting JUnit's {@code @Testcontainers}
 * extension manage it per test class: that would stop it at the end of each class, while Spring's test
 * context cache keeps reusing the same DataSource across classes with identical configuration, leaving it
 * pointed at a container that no longer exists. Ryuk reaps this container when the JVM exits.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:9.7"));

    static {
        mysql.start();
    }

    protected final MockMvc mockMvc;
    protected final UserRepository userRepository;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractIntegrationTest(MockMvc mockMvc, UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
    }

    protected AuthResponse registerUser(String username, String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(username, email, password);
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, AuthResponse.class);
    }
}
