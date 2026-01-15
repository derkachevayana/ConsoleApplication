package com.example.integration;

import com.example.UserManagementApplication;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest(
        classes = UserManagementApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }


    @Test
    void createUser_ValidData_ShouldReturnCreated() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturnConflict() throws Exception {
        UserRequest request1 = new UserRequest();
        request1.setName("John");
        request1.setEmail("same@example.com");
        request1.setAge(25);

        UserRequest request2 = new UserRequest();
        request2.setName("Alice");
        request2.setEmail("same@example.com");
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
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAge(25);

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
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        UserRequest request1 = new UserRequest();
        request1.setName("John");
        request1.setEmail("john@example.com");
        request1.setAge(25);

        UserRequest request2 = new UserRequest();
        request2.setName("Alice");
        request2.setEmail("alice@example.com");
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
    void getUserByEmail_ShouldReturnUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/users/email/{email}", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }


    @Test
    void updateUser_ValidData_ShouldReturnUpdated() throws Exception {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("John");
        createRequest.setEmail("john@example.com");
        createRequest.setAge(25);

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(responseJson, UserResponse.class);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("john.updated@example.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                .andExpect(jsonPath("$.age").value(30));
    }


    @Test
    void deleteUser_ShouldRemoveUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAge(25);

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(responseJson, UserResponse.class);

        mockMvc.perform(delete("/api/users/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
}
