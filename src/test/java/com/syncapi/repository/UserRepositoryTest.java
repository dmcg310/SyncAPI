package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.syncapi.repository.RepositoryTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String testEmail, testPasswordHash, testName;

    @BeforeEach
    void setUp() {
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        testEmail = generateRandomEmail();
        testPasswordHash = generateRandomPasswordHash();
        testName = generateRandomName();

        testUser = new User(testEmail, testPasswordHash, testName);
    }

    @Test
    void shouldSaveUser() {
        // when
        User saved = userRepository.save(testUser);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(testEmail);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByEmail() {
        // given
        userRepository.save(testUser);

        // when
        Optional<User> found = userRepository.findByEmail(testEmail);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testEmail);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // given
        userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsByEmail(testEmail);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // given
        userRepository.save(testUser);

        User duplicate = new User(testEmail, generateRandomPasswordHash(), generateRandomName());

        // when / then
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicate);
        });
    }
}
