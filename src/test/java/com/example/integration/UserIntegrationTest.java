package com.example.integration;

import com.example.UserManagementApplication;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
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

@SpringBootTest(classes = UserManagementApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

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
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        assertThat(userRepository.existsById(response.getId())).isTrue();
    }

    @Test
    void createUser_AgeBoundaryValues_ShouldWork() throws Exception {
        UserRequest minAgeRequest = new UserRequest();
        minAgeRequest.setName("Min Age");
        minAgeRequest.setEmail("min@example.com");
        minAgeRequest.setAge(0);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minAgeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.age").value(0));

        UserRequest maxAgeRequest = new UserRequest();
        maxAgeRequest.setName("Max Age");
        maxAgeRequest.setEmail("max@example.com");
        maxAgeRequest.setAge(120);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maxAgeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.age").value(120));
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturnConflict() throws Exception {
        UserRequest request1 = new UserRequest();
        request1.setName("First User");
        request1.setEmail("duplicate@example.com");
        request1.setAge(25);

        UserRequest request2 = new UserRequest();
        request2.setName("Second User");
        request2.setEmail("duplicate@example.com");
        request2.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("invalid-email");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void createUser_EmptyName_ShouldReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("");
        request.setEmail("john@example.com");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void createUser_InvalidAge_ShouldReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAge(150);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.age").exists());
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
    void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
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

    @Test
    void getAllUsers_EmptyDatabase_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserByEmail_ExistingEmail_ShouldReturnUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Bob Johnson");
        request.setEmail("bob@example.com");
        request.setAge(35);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/users/email/{email}", "bob@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.name").value("Bob Johnson"));
    }

    @Test
    void getUserByEmail_NonExistingEmail_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", "nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_AllFields_ShouldUpdateAllFields() throws Exception {
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

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.age").value(30));
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
    void updateUser_WithWhitespace_ShouldTrimValues() throws Exception {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Original");
        createRequest.setEmail("original@example.com");
        createRequest.setAge(25);

        String createdJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(createdJson, UserResponse.class);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("  Updated Name  ");
        updateRequest.setEmail("  updated@example.com  ");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUser_NonExistingId_ShouldReturnNotFound() throws Exception {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_DuplicateEmail_ShouldReturnConflict() throws Exception {
        UserRequest request1 = new UserRequest();
        request1.setName("First User");
        request1.setEmail("first@example.com");
        request1.setAge(25);

        UserRequest request2 = new UserRequest();
        request2.setName("Second User");
        request2.setEmail("second@example.com");
        request2.setAge(30);

        String created1Json = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created1 = objectMapper.readValue(created1Json, UserResponse.class);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("second@example.com");
        updateRequest.setAge(35);

        mockMvc.perform(put("/api/users/{id}", created1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_ExistingUser_ShouldReturnNoContent() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("To Be Deleted");
        request.setEmail("todelete@example.com");
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
    void deleteUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound());
    }
}
