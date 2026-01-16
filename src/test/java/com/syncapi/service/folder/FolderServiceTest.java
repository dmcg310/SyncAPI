package com.syncapi.service.folder;

import com.syncapi.TestUtil;
import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.FolderRepository;
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
class FolderServiceTest {
    @Mock
    private FolderRepository folderRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private Util util;

    private FolderService folderService;

    private User testUser;
    private String testEmail;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        folderService = new FolderService(folderRepository, workspaceRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        testUser = createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        testWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testUser);
    }

    @Test
    void shouldGetFoldersByWorkspace() {
        // given
        Folder folder1 = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
        Folder folder2 = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);

        when(workspaceRepository.findById(testWorkspace.getId())).thenReturn(Optional.of(testWorkspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(List.of(folder1, folder2));

        // when
        List<FolderResponse> result = folderService.getFoldersByWorkspace(testWorkspace.getId(), testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(folder1.getId());
        assertThat(result.get(1).getId()).isEqualTo(folder2.getId());

        verify(workspaceRepository).findById(testWorkspace.getId());
        verify(util).getUserByEmail(testEmail);
        verify(folderRepository).findByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoFolders() {
        // given
        when(workspaceRepository.findById(testWorkspace.getId())).thenReturn(Optional.of(testWorkspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(List.of());

        // when
        List<FolderResponse> result = folderService.getFoldersByWorkspace(testWorkspace.getId(), testEmail);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowWhenGettingFoldersFromNonExistentWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> folderService.getFoldersByWorkspace(workspaceId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(workspaceRepository).findById(workspaceId);
        verifyNoInteractions(folderRepository);
    }

    @Test
    void shouldThrowWhenGettingFoldersByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);

        when(workspaceRepository.findById(otherWorkspace.getId())).thenReturn(Optional.of(otherWorkspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> folderService.getFoldersByWorkspace(otherWorkspace.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verifyNoInteractions(folderRepository);
    }

    @Test
    void shouldGetFolderById() {
        // given
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
        folder.setDescription(TestUtil.generateRandomValue("description"));

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when
        FolderResponse result = folderService.getFolderById(folder.getId(), testEmail);

        // then
        assertThat(result.getId()).isEqualTo(folder.getId());
        assertThat(result.getName()).isEqualTo(folder.getName());
        assertThat(result.getDescription()).isEqualTo(folder.getDescription());
        assertThat(result.getWorkspaceId()).isEqualTo(testWorkspace.getId());

        verify(folderRepository).findById(folder.getId());
        verify(util).getUserByEmail(testEmail);
    }

    @Test
    void shouldThrowWhenGettingNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();
        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> folderService.getFolderById(folderId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found");

        verify(folderRepository).findById(folderId);
        verifyNoInteractions(util);
    }

    @Test
    void shouldThrowWhenGettingFolderByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> folderService.getFolderById(folder.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("access denied");

        verify(folderRepository).findById(folder.getId());
    }

    @Test
    void shouldCreateFolder() {
        // given
        String folderName = TestUtil.generateRandomName();
        String folderDescription = TestUtil.generateRandomValue("description");
        FolderRequest request = new FolderRequest(folderName, folderDescription);

        when(workspaceRepository.findById(testWorkspace.getId())).thenReturn(Optional.of(testWorkspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder folder = invocation.getArgument(0);
            setField(folder, "id", TestUtil.generateRandomId());
            return folder;
        });

        // when
        FolderResponse result = folderService.createFolder(testWorkspace.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(folderName);
        assertThat(result.getDescription()).isEqualTo(folderDescription);
        assertThat(result.getWorkspaceId()).isEqualTo(testWorkspace.getId());

        ArgumentCaptor<Folder> captor = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepository).save(captor.capture());

        Folder savedFolder = captor.getValue();
        assertThat(savedFolder.getName()).isEqualTo(folderName);
        assertThat(savedFolder.getDescription()).isEqualTo(folderDescription);
        assertThat(savedFolder.getWorkspace()).isEqualTo(testWorkspace);
    }

    @Test
    void shouldCreateFolderWithoutDescription() {
        // given
        String folderName = TestUtil.generateRandomName();
        FolderRequest request = new FolderRequest(folderName);

        when(workspaceRepository.findById(testWorkspace.getId())).thenReturn(Optional.of(testWorkspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder folder = invocation.getArgument(0);
            setField(folder, "id", TestUtil.generateRandomId());
            return folder;
        });

        // when
        FolderResponse result = folderService.createFolder(testWorkspace.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(folderName);
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void shouldThrowWhenCreatingFolderInNonExistentWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> folderService.createFolder(workspaceId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found with Id: " + workspaceId);

        verify(workspaceRepository).findById(workspaceId);
        verifyNoInteractions(folderRepository);
    }

    @Test
    void shouldThrowWhenCreatingFolderByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(workspaceRepository.findById(otherWorkspace.getId())).thenReturn(Optional.of(otherWorkspace));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> folderService.createFolder(otherWorkspace.getId(), request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found or access denied");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldUpdateFolder() {
        // given
        String newName = TestUtil.generateRandomName();
        String newDescription = TestUtil.generateRandomValue("description");
        FolderRequest request = new FolderRequest(newName, newDescription);

        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        // when
        FolderResponse result = folderService.updateFolder(folder.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(newDescription);

        verify(folderRepository).save(folder);
    }

    @Test
    void shouldUpdateFolderNameOnly() {
        // given
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
        folder.setDescription(TestUtil.generateRandomValue("old-description"));

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        String newName = TestUtil.generateRandomName();
        FolderRequest request = new FolderRequest(newName);

        // when
        FolderResponse result = folderService.updateFolder(folder.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isNull(); // description should be cleared as PUT
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> folderService.updateFolder(folderId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingFolderByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);

        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> folderService.updateFolder(folder.getId(), request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("access denied");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldPatchFolderName() {
        // given
        String folderDescription = TestUtil.generateRandomValue("description");
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), folderDescription,
                testWorkspace);

        String newName = TestUtil.generateRandomName();
        FolderRequest request = new FolderRequest();
        request.setName(newName);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        // when
        FolderResponse result = folderService.patchFolder(folder.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(folderDescription);

        verify(folderRepository).save(folder);
    }

    @Test
    void shouldPatchFolderDescription() {
        // given
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
        String originalName = folder.getName();

        String newDescription = TestUtil.generateRandomValue("description");
        FolderRequest request = new FolderRequest();
        request.setDescription(newDescription);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        // when
        FolderResponse result = folderService.patchFolder(folder.getId(), request, testEmail);

        // then
        assertThat(result.getDescription()).isEqualTo(newDescription);
        assertThat(result.getName()).isEqualTo(originalName);

        verify(folderRepository).save(folder);
    }

    @Test
    void shouldPatchFolderBothFields() {
        // given
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);

        String newName = TestUtil.generateRandomName();
        String newDescription = TestUtil.generateRandomValue("description");
        FolderRequest request = new FolderRequest();
        request.setName(newName);
        request.setDescription(newDescription);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        // when
        FolderResponse result = folderService.patchFolder(folder.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(newDescription);

        verify(folderRepository).save(folder);
    }

    @Test
    void shouldClearDescriptionWithEmptyString() {
        // given
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
        folder.setDescription(TestUtil.generateRandomValue("description"));

        FolderRequest request = new FolderRequest();
        request.setDescription(""); // empty string should clear (set to null)

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(folderRepository.save(any(Folder.class))).thenReturn(folder);

        // when
        FolderResponse result = folderService.patchFolder(folder.getId(), request, testEmail);

        // then
        assertThat(result.getDescription()).isNull();

        verify(folderRepository).save(folder);
    }

    @Test
    void shouldThrowWhenPatchingNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> folderService.patchFolder(folderId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found");

        verify(folderRepository).findById(folderId);
        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatchingFolderByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherUser);
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);

        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> folderService.patchFolder(folder.getId(), request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("access denied");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldDeleteFolder() {
        // given
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when
        folderService.deleteFolder(folder.getId(), testEmail);

        // then
        verify(folderRepository).delete(folder);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> folderService.deleteFolder(folderId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found");

        verify(folderRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingFolderByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder folder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);

        when(folderRepository.findById(folder.getId())).thenReturn(Optional.of(folder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> folderService.deleteFolder(folder.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("access denied");

        verify(folderRepository, never()).delete(any());
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

    private Folder createFolder(Long id, String name, Workspace workspace) {
        Folder folder = new Folder(name, workspace);
        setField(folder, "id", id);

        folder.setRequests(new ArrayList<>());

        return folder;
    }

    private Folder createFolder(Long id, String name, String description, Workspace workspace) {
        Folder folder = new Folder(name, workspace);
        setField(folder, "id", id);

        folder.setDescription(description);
        folder.setRequests(new ArrayList<>());

        return folder;
    }
}
