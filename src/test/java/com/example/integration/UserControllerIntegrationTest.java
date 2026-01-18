package com.example.integration;

import com.example.UserManagementApplication;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ValidRequest_ShouldReturnCreated() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setAge(30);

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        assertThat(userRepository.existsById(response.getId())).isTrue();
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Alice Smith");
        request.setEmail("alice@example.com");
        request.setAge(28);

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(responseJson, UserResponse.class);

        mockMvc.perform(get("/api/users/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.age").value(28));
    }

    @Test
    void updateUser_PartialUpdate_ShouldUpdateOnlyProvidedFields() throws Exception {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Original Name");
        createRequest.setEmail("original@example.com");
        createRequest.setAge(25);

        String createdJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(createdJson, UserResponse.class);

        String updateJson = "{\"name\":\"Updated Name\"}";

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("original@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void deleteUser_ExistingUser_ShouldReturnNoContent() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("To Delete");
        request.setEmail("delete@example.com");
        request.setAge(40);

        String createdJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(createdJson, UserResponse.class);

        mockMvc.perform(delete("/api/users/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void validation_InvalidPathVariable_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", -1L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/users/email/{email}", "invalid-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validation_InvalidRequestBody_ReturnsBadRequest() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setName("A");
        invalidRequest.setEmail("invalid");
        invalidRequest.setAge(150);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void getUserById_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"))
                .andExpect(jsonPath("$.path").value("/api/users/99999"));
    }

    @Test
    void updateUser_NonExistingUser_ReturnsNotFound() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");

        mockMvc.perform(put("/api/users/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"))
                .andExpect(jsonPath("$.path").value("/api/users/99999"));
    }

    @Test
    void getAllUsers_EmptyDatabase_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllUsers_WithUsers_ReturnsList() throws Exception {
        UserRequest request1 = new UserRequest();
        request1.setName("User One");
        request1.setEmail("user1@example.com");
        request1.setAge(25);

        UserRequest request2 = new UserRequest();
        request2.setName("User Two");
        request2.setEmail("user2@example.com");
        request2.setAge(30);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }
}