package com.example.repository;

import com.example.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .age(25)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void existsByEmail_ShouldReturnTrueForExistingEmail() {
        User user = User.builder()
                .name("Test User")
                .email("exists@example.com")
                .age(25)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalseForNonExistingEmail() {
        boolean exists = userRepository.existsByEmail("nonexisting@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void findById_ExistingId_ShouldReturnUser() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .age(25)
                .build();
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userRepository.save(User.builder().name("User1").email("user1@example.com").age(20).build());
        userRepository.save(User.builder().name("User2").email("user2@example.com").age(25).build());

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void delete_ShouldRemoveUser() {
        User user = User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .age(30)
                .build();
        User saved = userRepository.save(user);

        userRepository.delete(saved);
        userRepository.flush();

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_DuplicateEmail_ShouldThrowException() {
        String email = "duplicate@example.com";
        userRepository.save(User.builder()
                .name("User1")
                .email(email)
                .age(25)
                .build());

        User user2 = User.builder()
                .name("User2")
                .email(email)
                .age(30)
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}