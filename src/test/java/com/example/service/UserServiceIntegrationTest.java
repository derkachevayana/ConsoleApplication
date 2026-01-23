package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.exception.EmailValidationException;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EmbeddedKafka(partitions = 1, ports = 9092, topics = {"user-events"})
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ValidRequest_ShouldCreateUser() {
        UserRequest request = new UserRequest();
        String email = "test-" + UUID.randomUUID() + "@example.com";
        request.setName("John Doe");
        request.setEmail(email);
        request.setAge(30);

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getAge()).isEqualTo(30);
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() {
        UserRequest request = new UserRequest();
        String email = "getbyid-" + UUID.randomUUID() + "@example.com";
        request.setName("Test User");
        request.setEmail(email);
        request.setAge(25);

        UserResponse created = userService.createUser(request);

        UserResponse found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo(email);
    }

    @Test
    void updateUser_ValidUpdate_ShouldUpdateUser() {
        UserRequest createRequest = new UserRequest();
        String email = "original-" + UUID.randomUUID() + "@example.com";
        createRequest.setName("Original");
        createRequest.setEmail(email);
        createRequest.setAge(25);
        UserResponse created = userService.createUser(createRequest);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");
        updateRequest.setAge(30);

        UserResponse updated = userService.updateUser(created.getId(), updateRequest);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getAge()).isEqualTo(30);
        assertThat(updated.getEmail()).isEqualTo(email);
    }

    @Test
    void deleteUser_ExistingUser_ShouldDelete() {
        UserRequest request = new UserRequest();
        String email = "delete-" + UUID.randomUUID() + "@example.com";
        request.setName("To Delete");
        request.setEmail(email);
        request.setAge(40);

        UserResponse created = userService.createUser(request);

        userService.deleteUser(created.getId());

        assertThatThrownBy(() -> userService.getUserById(created.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        int count = 3;
        for (int i = 0; i < count; i++) {
            UserRequest request = new UserRequest();
            request.setName("User " + i);
            request.setEmail("user" + i + "-" + UUID.randomUUID() + "@example.com");
            request.setAge(20 + i);
            userService.createUser(request);
        }

        List<UserResponse> allUsers = userService.getAllUsers();

        assertThat(allUsers).hasSize(count);
    }


    @Test
    void createUser_DuplicateEmail_ShouldThrowException() {
        String email = "duplicate-" + UUID.randomUUID() + "@example.com";

        UserRequest request1 = new UserRequest();
        request1.setName("User One");
        request1.setEmail(email);
        request1.setAge(25);
        userService.createUser(request1);

        UserRequest request2 = new UserRequest();
        request2.setName("User Two");
        request2.setEmail(email);
        request2.setAge(30);

        assertThatThrownBy(() -> userService.createUser(request2))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_InvalidEmailFormat_ShouldThrowException() {
        UserRequest request = new UserRequest();
        request.setName("Test");
        request.setEmail("invalid-email-format");
        request.setAge(30);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailValidationException.class)
                .hasMessageContaining("Invalid email format");
    }


    @Test
    void createUser_ShouldSendKafkaEvent() throws Exception {
        UserRequest request = new UserRequest();
        String email = "kafka-" + UUID.randomUUID() + "@example.com";
        request.setName("Kafka User");
        request.setEmail(email);
        request.setAge(28);

        try (Consumer<String, String> consumer = consumerFactory.createConsumer("test-group", "clientId")) {
            consumer.subscribe(java.util.Collections.singletonList("user-events"));

            userService.createUser(request);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            assertThat(records.count()).isGreaterThan(0);

            boolean hasEvent = false;
            for (var record : records) {
                if (record.key().equals(email) &&
                        record.value().contains("\"eventType\":\"USER_CREATED\"")) {
                    hasEvent = true;
                    break;
                }
            }
            assertThat(hasEvent).isTrue();
        }
    }

    @Test
    void deleteUser_ShouldSendKafkaEvent() throws Exception {
        UserRequest request = new UserRequest();
        String email = "delete-kafka-" + UUID.randomUUID() + "@example.com";
        request.setName("Kafka Delete");
        request.setEmail(email);
        request.setAge(35);

        UserResponse created = userService.createUser(request);

        try (Consumer<String, String> consumer = consumerFactory.createConsumer("test-group", "clientId")) {
            consumer.subscribe(java.util.Collections.singletonList("user-events"));
            consumer.poll(Duration.ofMillis(100));

            userService.deleteUser(created.getId());

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            assertThat(records.count()).isGreaterThan(0);

            boolean hasEvent = false;
            for (var record : records) {
                if (record.key().equals(email) &&
                        record.value().contains("\"eventType\":\"USER_DELETED\"")) {
                    hasEvent = true;
                    break;
                }
            }
            assertThat(hasEvent).isTrue();
        }
    }


    @Test
    void getUserById_NonExisting_ShouldThrowException() {
        assertThatThrownBy(() -> userService.getUserById(99999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 99999");
    }

    @Test
    void updateUser_NonExistingUser_ShouldThrowException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");

        assertThatThrownBy(() -> userService.updateUser(99999L, updateRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_NonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> userService.deleteUser(99999L))
                .isInstanceOf(UserNotFoundException.class);
    }
}