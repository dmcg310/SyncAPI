package com.syncapi.util;

import com.syncapi.TestUtil;
import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.environment.EnvironmentRepository;
import com.syncapi.repository.environment.EnvironmentVariableRepository;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.repository.workspace.WorkspaceRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private EnvironmentVariableRepository environmentVariableRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private RequestRepository requestRepository;

    private Util util;

    @BeforeEach
    void setUp() {
        util = new Util(userRepository, workspaceRepository, environmentRepository, environmentVariableRepository,
                folderRepository, requestRepository);
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

    @Test
    void defaultFalse_shouldReturnFalseWhenNull() {
        // when
        boolean result = util.defaultFalse(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void defaultFalse_shouldReturnFalseWhenFalse() {
        // when
        boolean result = util.defaultFalse(Boolean.FALSE);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void defaultFalse_shouldReturnTrueWhenTrue() {
        // when
        boolean result = util.defaultFalse(Boolean.TRUE);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetWorkspaceWithAccessCheck_whenMember() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());
        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);

        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        Workspace result = util.getWorkspaceWithAccessCheck(workspaceId, email);

        // then
        assertThat(result).isEqualTo(workspace);

        verify(workspaceRepository).findById(workspaceId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowWorkspaceNotFound_whenWorkspaceMissing() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        String email = TestUtil.generateRandomEmail();

        // when / then
        assertThatThrownBy(() -> util.getWorkspaceWithAccessCheck(workspaceId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found")
                .hasMessageContaining(workspaceId.toString());

        verify(workspaceRepository).findById(workspaceId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowAccessDenied_whenUserNotMemberOfWorkspace() {
        // given
        String email = TestUtil.generateRandomEmail();
        User member = new User("member-" + email, TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName());
        User notMember = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(member);

        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(notMember));

        // when / then
        assertThatThrownBy(() -> util.getWorkspaceWithAccessCheck(workspaceId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository).findById(workspaceId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetEnvironmentWithAccessCheck_whenMember() {
        // given
        String email = TestUtil.generateRandomEmail();

        User user = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());
        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);

        Environment environment = new Environment();
        environment.setName(TestUtil.generateRandomName());
        environment.setWorkspace(workspace);

        Long environmentId = TestUtil.generateRandomId();

        when(environmentRepository.findById(environmentId)).thenReturn(Optional.of(environment));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        Environment result = util.getEnvironmentWithAccessCheck(environmentId, email);

        // then
        assertThat(result).isEqualTo(environment);

        verify(environmentRepository).findById(environmentId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowEnvironmentNotFound_whenMissing() {
        // given
        Long environmentId = TestUtil.generateRandomId();

        when(environmentRepository.findById(environmentId)).thenReturn(Optional.empty());

        String email = TestUtil.generateRandomEmail();

        // when / then
        assertThatThrownBy(() -> util.getEnvironmentWithAccessCheck(environmentId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Environment not found")
                .hasMessageContaining(environmentId.toString());

        verify(environmentRepository).findById(environmentId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowAccessDenied_whenUserNotMemberOfEnvironmentWorkspace() {
        // given
        String email = TestUtil.generateRandomEmail();
        User member = new User("member-" + email, TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName());
        User notMember = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(member);

        Environment environment = new Environment();
        environment.setName(TestUtil.generateRandomName());
        environment.setWorkspace(workspace);

        Long environmentId = TestUtil.generateRandomId();

        when(environmentRepository.findById(environmentId)).thenReturn(Optional.of(environment));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(notMember));

        // when / then
        assertThatThrownBy(() -> util.getEnvironmentWithAccessCheck(environmentId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Environment not found or access denied");

        verify(environmentRepository).findById(environmentId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetEnvironmentVariableWithAccessCheck_whenMember() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());
        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);

        Environment env = new Environment();
        env.setName(TestUtil.generateRandomName());
        env.setWorkspace(workspace);

        EnvironmentVariable variable = new EnvironmentVariable();
        variable.setKey(TestUtil.generateRandomKey());
        variable.setValue(TestUtil.generateRandomValue());
        variable.setEnvironment(env);

        Long variableId = TestUtil.generateRandomId();

        when(environmentVariableRepository.findById(variableId)).thenReturn(Optional.of(variable));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        EnvironmentVariable result = util.getEnvironmentVariableWithAccessCheck(variableId, email);

        // then
        assertThat(result).isEqualTo(variable);

        verify(environmentVariableRepository).findById(variableId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowEnvironmentVariableNotFound_whenMissing() {
        // given
        Long variableId = TestUtil.generateRandomId();

        when(environmentVariableRepository.findById(variableId)).thenReturn(Optional.empty());

        String email = TestUtil.generateRandomEmail();

        // when / then
        assertThatThrownBy(() -> util.getEnvironmentVariableWithAccessCheck(variableId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Environment variable not found")
                .hasMessageContaining(variableId.toString());

        verify(environmentVariableRepository).findById(variableId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowAccessDenied_whenUserNotMemberOfVariableWorkspace() {
        // given
        String email = TestUtil.generateRandomEmail();
        User member = new User("member-" + email, TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName());
        User notMember = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(member);

        Environment env = new Environment();
        env.setName(TestUtil.generateRandomName());
        env.setWorkspace(workspace);

        EnvironmentVariable variable = new EnvironmentVariable();
        variable.setKey(TestUtil.generateRandomKey());
        variable.setValue(TestUtil.generateRandomValue());
        variable.setEnvironment(env);

        Long variableId = TestUtil.generateRandomId();

        when(environmentVariableRepository.findById(variableId)).thenReturn(Optional.of(variable));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(notMember));

        // when / then
        assertThatThrownBy(() -> util.getEnvironmentVariableWithAccessCheck(variableId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Environment variable not found or access denied");

        verify(environmentVariableRepository).findById(variableId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetFolderWithAccessCheck_whenMember() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());
        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);

        Folder folder = new Folder();
        folder.setName(TestUtil.generateRandomName());
        folder.setWorkspace(workspace);

        Long folderId = TestUtil.generateRandomId();

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        Folder result = util.getFolderWithAccessCheck(folderId, email);

        // then
        assertThat(result).isEqualTo(folder);

        verify(folderRepository).findById(folderId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowFolderNotFound_whenMissing() {
        // given
        Long folderId = TestUtil.generateRandomId();

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        String email = TestUtil.generateRandomEmail();

        // when / then
        assertThatThrownBy(() -> util.getFolderWithAccessCheck(folderId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found")
                .hasMessageContaining(folderId.toString());

        verify(folderRepository).findById(folderId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowAccessDenied_whenUserNotMemberOfFolderWorkspace() {
        // given
        String email = TestUtil.generateRandomEmail();
        User member = new User("member-" + email, TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName());
        User notMember = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(member);

        Folder folder = new Folder();
        folder.setName(TestUtil.generateRandomName());
        folder.setWorkspace(workspace);

        Long folderId = TestUtil.generateRandomId();

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(notMember));

        // when / then
        assertThatThrownBy(() -> util.getFolderWithAccessCheck(folderId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found or access denied");

        verify(folderRepository).findById(folderId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetRequestWithAccessCheck_whenMember() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());
        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);

        Folder folder = new Folder();
        folder.setName(TestUtil.generateRandomName());
        folder.setWorkspace(workspace);

        Request request = new Request();
        request.setName(TestUtil.generateRandomName());
        request.setMethod(RequestMethod.GET);
        request.setUrl(TestUtil.generateRandomUrl());
        request.setFolder(folder);

        Long requestId = TestUtil.generateRandomId();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        Request result = util.getRequestWithAccessCheck(requestId, email);

        // then
        assertThat(result).isEqualTo(request);

        verify(requestRepository).findById(requestId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowRequestNotFound_whenMissing() {
        // given
        Long requestId = TestUtil.generateRandomId();

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        String email = TestUtil.generateRandomEmail();
        // when / then
        assertThatThrownBy(() -> util.getRequestWithAccessCheck(requestId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found")
                .hasMessageContaining(requestId.toString());

        verify(requestRepository).findById(requestId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowAccessDenied_whenUserNotMemberOfRequestWorkspace() {
        // given
        String email = TestUtil.generateRandomEmail();
        User member = new User("member-" + email, TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName());
        User notMember = new User(email, TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName());

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(member);

        Folder folder = new Folder();
        folder.setName(TestUtil.generateRandomName());
        folder.setWorkspace(workspace);

        Request request = new Request();
        request.setName(TestUtil.generateRandomName());
        request.setMethod(RequestMethod.GET);
        request.setUrl(TestUtil.generateRandomUrl());
        request.setFolder(folder);

        Long requestId = TestUtil.generateRandomId();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(notMember));

        // when / then
        assertThatThrownBy(() -> util.getRequestWithAccessCheck(requestId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository).findById(requestId);
        verify(userRepository).findByEmail(email);
    }

    private void setAuthentication(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
