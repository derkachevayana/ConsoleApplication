package com.example.integration;

import com.example.dto.UserRequest;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, ports = 9092)
@Transactional
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ValidRequest_ShouldReturnCreated() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturnConflict() throws Exception {
        String email = "duplicate-" + UUID.randomUUID() + "@example.com";

        UserRequest request = new UserRequest();
        request.setName("User One");
        request.setEmail(email);
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: " + email));
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        UserRequest request = new UserRequest();
        String email = "getbyid-" + UUID.randomUUID() + "@example.com";
        request.setName("Test User");
        request.setEmail(email);
        request.setAge(25);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        UserRequest request = new UserRequest();
        String email = "delete-" + UUID.randomUUID() + "@example.com";
        request.setName("To Delete");
        request.setEmail(email);
        request.setAge(40);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ValidUpdate_ShouldReturnUpdatedUser() throws Exception {
        UserRequest createRequest = new UserRequest();
        String email = "original-" + UUID.randomUUID() + "@example.com";
        createRequest.setName("Original");
        createRequest.setEmail(email);
        createRequest.setAge(25);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        String updateJson = "{\"name\":\"Updated Name\",\"age\":30}";

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void createUser_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Test");
        request.setEmail("invalid-email");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void createUser_InvalidAge_ShouldReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Test");
        request.setEmail("test-" + UUID.randomUUID() + "@example.com");
        request.setAge(150);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByEmail_ExistingUser_ShouldReturnUser() throws Exception {
        String email = "findbyemail-" + UUID.randomUUID() + "@example.com";
        UserRequest request = new UserRequest();
        request.setName("Email User");
        request.setEmail(email);
        request.setAge(35);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Email User"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.age").value(35));
    }

    @Test
    void getUserByEmail_NonExistingUser_ShouldReturnNotFound() throws Exception {
        String email = "nonexisting-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(get("/api/users/email/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with email: " + email));
    }

    @Test
    void createUser_EmptyName_ShouldReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("");
        request.setEmail("test-" + UUID.randomUUID() + "@example.com");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }
}