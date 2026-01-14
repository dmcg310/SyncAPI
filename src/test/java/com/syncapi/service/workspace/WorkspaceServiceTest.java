package com.syncapi.service.workspace;

import com.syncapi.TestUtil;
import com.syncapi.dto.workspace.AddMemberRequest;
import com.syncapi.dto.workspace.WorkspaceRequest;
import com.syncapi.dto.workspace.WorkspaceResponse;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.WorkspaceRepository;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {
    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private Util util;

    private WorkspaceService workspaceService;

    private User testUser;
    private String testEmail;

    @BeforeEach
    void setUp() {
        workspaceService = new WorkspaceService(workspaceRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        testUser = createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
    }

    @Test
    void shouldGetUserWorkspaces() {
        // given
        Workspace workspace1 = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testUser);
        Workspace workspace2 = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testUser);

        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(workspaceRepository.findByMemberId(testUser.getId())).thenReturn(List.of(workspace1, workspace2));

        // when
        List<WorkspaceResponse> result = workspaceService.getUserWorkspaces(testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(workspace1.getId());
        assertThat(result.get(1).getId()).isEqualTo(workspace2.getId());

        verify(util).getUserByEmail(testEmail);
        verify(workspaceRepository).findByMemberId(testUser.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoWorkspaces() {
        // given
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(workspaceRepository.findByMemberId(testUser.getId())).thenReturn(List.of());

        // when
        List<WorkspaceResponse> result = workspaceService.getUserWorkspaces(testEmail);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetWorkspaceById() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when
        WorkspaceResponse result = workspaceService.getWorkspace(workspaceId, testEmail);

        // then
        assertThat(result.getId()).isEqualTo(workspaceId);
        assertThat(result.getName()).isEqualTo(workspace.getName());
        assertThat(result.getMembers()).isNotNull();
    }

    @Test
    void shouldThrowWhenWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> workspaceService.getWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");
    }

    @Test
    void shouldThrowWhenUserNotMemberOfWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), otherUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.getWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");
    }

    @Test
    void shouldCreateWorkspace() {
        // given
        String workspaceName = TestUtil.generateRandomName();
        WorkspaceRequest request = new WorkspaceRequest(workspaceName);

        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace ws = invocation.getArgument(0);
            setField(ws, "id", TestUtil.generateRandomId());
            return ws;
        });

        // when
        WorkspaceResponse result = workspaceService.createWorkspace(request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(workspaceName);
        assertThat(result.getMemberCount()).isEqualTo(1);

        ArgumentCaptor<Workspace> captor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(captor.capture());

        Workspace saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(workspaceName);
        assertThat(saved.getMembers()).contains(testUser);
    }

    @Test
    void shouldUpdateWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        String newName = TestUtil.generateRandomName();
        WorkspaceRequest request = new WorkspaceRequest(newName);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.updateWorkspace(workspaceId, request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);

        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenUpdatingWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        // when / then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository).findById(workspaceId);
    }

    @Test
    void shouldThrowWhenUpdatingWorkspaceByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), otherUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        String newName = TestUtil.generateRandomName();
        WorkspaceRequest request = new WorkspaceRequest(newName);

        // when / then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");
    }

    @Test
    void shouldDeleteWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when
        workspaceService.deleteWorkspace(workspaceId, testEmail);

        // then
        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void shouldThrowWhenDeletingWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingWorkspaceByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), otherUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void shouldAddMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        String newMemberEmail = TestUtil.generateRandomEmail();
        User newMember = createUser(TestUtil.generateRandomId(), newMemberEmail, TestUtil.generateRandomName());

        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(util.getUserByEmail(newMemberEmail)).thenReturn(newMember);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        AddMemberRequest request = new AddMemberRequest(newMemberEmail);

        // when
        workspaceService.addMember(workspaceId, request, testEmail);

        // then
        assertThat(workspace.getMembers()).contains(newMember);

        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenAddingMemberWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        AddMemberRequest request = new AddMemberRequest(TestUtil.generateRandomEmail());

        // when / then
        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddingMemberByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), otherUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        AddMemberRequest request = new AddMemberRequest(TestUtil.generateRandomEmail());

        // when / then
        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddingExistingMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);
        AddMemberRequest request = new AddMemberRequest(testEmail);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already a member");

        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldRemoveMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherMember = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace workspace = createWorkspaceWithMembers(workspaceId, TestUtil.generateRandomName(),
                testUser, otherMember);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(util.getUserById(otherMember.getId())).thenReturn(otherMember);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        workspaceService.removeMember(workspaceId, otherMember.getId(), testEmail);

        // then
        assertThat(workspace.getMembers()).doesNotContain(otherMember);
        assertThat(workspace.getMembers()).contains(testUser);

        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenRemovingMemberWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, TestUtil.generateRandomId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingMemberByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(), TestUtil.generateRandomName());
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), otherUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, TestUtil.generateRandomId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        User nonMember = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(util.getUserById(nonMember.getId())).thenReturn(nonMember);

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, nonMember.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not a member");

        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingLastMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(util.getUserById(testUser.getId())).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, testUser.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot remove the last member");

        verify(workspaceRepository, never()).save(any());
    }

    private User createUser(Long id, String email, String name) {
        User user = new User(email, TestUtil.generateRandomPasswordHash(), name);
        setField(user, "id", id);

        return user;
    }

    private Workspace createWorkspace(Long id, String name, User member) {
        Workspace workspace = new Workspace(name);
        setField(workspace, "id", id);

        workspace.setMembers(new ArrayList<>(List.of(member)));

        return workspace;
    }

    private Workspace createWorkspaceWithMembers(Long id, String name, User... members) {
        Workspace workspace = new Workspace(name);
        setField(workspace, "id", id);

        workspace.setMembers(new ArrayList<>(List.of(members)));

        return workspace;
    }
}
