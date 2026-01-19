package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.TestUtil;
import com.syncapi.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private String testEmail, testPasswordHash, testName;

    @BeforeEach
    void setUp() {
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        testEmail = TestUtil.generateRandomEmail();
        testPasswordHash = TestUtil.generateRandomPasswordHash();
        testName = TestUtil.generateRandomName();

        user = new User(testEmail, testPasswordHash, testName);
        userRepository.save(user);
    }

    @Test
    void shouldSaveUser() {
        // given
        String email = TestUtil.generateRandomEmail();
        String name = TestUtil.generateRandomName();
        String passwordHash = TestUtil.generateRandomPasswordHash();

        // when
        User newUser = userRepository.save(new User(email, passwordHash, name));

        // then
        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getEmail()).isEqualTo(email);
        assertThat(newUser.getName()).isEqualTo(name);
        assertThat(newUser.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(newUser.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByEmail() {
        // when
        Optional<User> found = userRepository.findByEmail(testEmail);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testEmail);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // when
        Optional<User> found = userRepository.findByEmail(TestUtil.generateRandomEmail());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // when
        boolean exists = userRepository.existsByEmail(testEmail);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // when
        boolean exists = userRepository.existsByEmail(TestUtil.generateRandomEmail());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // given
        User duplicate = new User(testEmail, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());

        // when / then
        assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicate));
    }
}
