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

import java.util.List;

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
        testUser = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
    }

    @Test
    void shouldGetUserWorkspaces() {
        // given
        Workspace workspace1 = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                testUser);
        Workspace workspace2 = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                testUser);

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
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.getWorkspace(workspaceId, testEmail);

        // then
        assertThat(result.getId()).isEqualTo(workspaceId);
        assertThat(result.getName()).isEqualTo(workspace.getName());
        assertThat(result.getMembers()).isNotNull();

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
    }

    @Test
    void shouldThrowWhenWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found with Id: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> workspaceService.getWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
    }

    @Test
    void shouldThrowWhenUserNotMemberOfWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found or access denied"));

        // when / then
        assertThatThrownBy(() -> workspaceService.getWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
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
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);
        workspace.setDescription(TestUtil.generateRandomDescription());

        String newName = TestUtil.generateRandomName();
        WorkspaceRequest request = new WorkspaceRequest(newName);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.updateWorkspace(workspaceId, request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isNull(); // description should be cleared as PUT

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenUpdatingWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found with Id: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingWorkspaceByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found or access denied"));

        // when / then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldPatchWorkspaceName() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        String workspaceDescription = TestUtil.generateRandomDescription();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);
        workspace.setDescription(workspaceDescription);

        String newName = TestUtil.generateRandomName();
        WorkspaceRequest request = new WorkspaceRequest(newName);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.patchWorkspace(workspaceId, request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(workspaceDescription);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldPatchWorkspaceDescription() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        String originalName = TestUtil.generateRandomName();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, originalName, testUser);

        String newDescription = TestUtil.generateRandomDescription();
        WorkspaceRequest request = new WorkspaceRequest();
        request.setDescription(newDescription);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.patchWorkspace(workspaceId, request, testEmail);

        // then
        assertThat(result.getDescription()).isEqualTo(newDescription);
        assertThat(result.getName()).isEqualTo(originalName);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldPatchWorkspaceBothFields() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        String newName = TestUtil.generateRandomName();
        String newDescription = TestUtil.generateRandomDescription();
        WorkspaceRequest request = new WorkspaceRequest(newName, newDescription);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.patchWorkspace(workspaceId, request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(newDescription);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldClearDescriptionWithEmptyString() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);
        workspace.setDescription(TestUtil.generateRandomDescription());

        WorkspaceRequest request = new WorkspaceRequest();
        request.setDescription(""); // empty string should clear (set to null)

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        WorkspaceResponse result = workspaceService.patchWorkspace(workspaceId, request, testEmail);

        // then
        assertThat(result.getDescription()).isNull();

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenPatchingNonExistentWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found with Id: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> workspaceService.patchWorkspace(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatchingWorkspaceByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found or access denied"));

        // when / then
        assertThatThrownBy(() -> workspaceService.patchWorkspace(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldDeleteWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);

        // when
        workspaceService.deleteWorkspace(workspaceId, testEmail);

        // then
        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void shouldThrowWhenDeletingWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found with Id: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingWorkspaceByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found or access denied"));

        // when / then
        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void shouldAddMember() {
        // given
        String newMemberEmail = TestUtil.generateRandomEmail();
        User newMember = TestUtil.createUser(TestUtil.generateRandomId(), newMemberEmail, TestUtil.generateRandomName());

        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(util.getUserByEmail(newMemberEmail)).thenReturn(newMember);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        AddMemberRequest request = new AddMemberRequest(newMemberEmail);

        // when
        workspaceService.addMember(workspaceId, request, testEmail);

        // then
        assertThat(workspace.getMembers()).contains(newMember);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenAddingMemberWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        AddMemberRequest request = new AddMemberRequest(TestUtil.generateRandomEmail());

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found with Id: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddingMemberByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        AddMemberRequest request = new AddMemberRequest(TestUtil.generateRandomEmail());

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found or access denied"));

        // when / then
        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddingExistingMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);
        AddMemberRequest request = new AddMemberRequest(testEmail);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already a member");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldRemoveMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        User otherMember = TestUtil.createRandomUser();
        Workspace workspace = TestUtil.createWorkspaceWithMembers(workspaceId, TestUtil.generateRandomName(), testUser,
                otherMember);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(util.getUserById(otherMember.getId())).thenReturn(otherMember);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // when
        workspaceService.removeMember(workspaceId, otherMember.getId(), testEmail);

        // then
        assertThat(workspace.getMembers()).doesNotContain(otherMember);
        assertThat(workspace.getMembers()).contains(testUser);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void shouldThrowWhenRemovingMemberWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found with Id: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, TestUtil.generateRandomId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingMemberByNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new RuntimeException("Workspace not found or access denied"));

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, TestUtil.generateRandomId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingNonMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        User nonMember = TestUtil.createRandomUser();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(util.getUserById(nonMember.getId())).thenReturn(nonMember);

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, nonMember.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not a member");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingLastMember() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Workspace workspace = TestUtil.createWorkspace(workspaceId, TestUtil.generateRandomName(), testUser);

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail)).thenReturn(workspace);
        when(util.getUserById(testUser.getId())).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> workspaceService.removeMember(workspaceId, testUser.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot remove the last member");

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verify(workspaceRepository, never()).save(any());
    }
}
