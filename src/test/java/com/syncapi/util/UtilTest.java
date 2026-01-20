package com.syncapi.util;

import com.syncapi.TestUtil;
import com.syncapi.entity.User;
import com.syncapi.repository.FolderRepository;
import com.syncapi.repository.RequestRepository;
import com.syncapi.repository.UserRepository;
import com.syncapi.repository.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private RequestRepository requestRepository;

    private Util util;

    @BeforeEach
    void setUp() {
        util = new Util(userRepository, workspaceRepository, folderRepository, requestRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetCurrentUserEmail() {
        // given
        String email = TestUtil.generateRandomEmail();
        setAuthentication(email);

        // when
        String result = Util.getCurrentUserEmail();

        // then
        assertThat(result).isEqualTo(email);
    }

    @Test
    void shouldThrowWhenNoAuthentication() {
        // when / then
        assertThatThrownBy(Util::getCurrentUserEmail)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void shouldGetUserByEmail() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = TestUtil.createUser(TestUtil.generateRandomId(), email, TestUtil.generateRandomName());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        User result = util.getUserByEmail(email);

        // then
        assertThat(result).isEqualTo(user);
        assertThat(result.getEmail()).isEqualTo(email);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowWhenUserNotFoundByEmail() {
        // given
        String email = TestUtil.generateRandomEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> util.getUserByEmail(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining(email);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetUserById() {
        // given
        Long userId = TestUtil.generateRandomId();
        User user = TestUtil.createRandomUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = util.getUserById(userId);

        // then
        assertThat(result).isEqualTo(user);

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {
        // given
        Long userId = TestUtil.generateRandomId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> util.getUserById(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
    }

    private void setAuthentication(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
