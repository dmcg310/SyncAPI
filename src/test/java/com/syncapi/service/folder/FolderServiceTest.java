package com.syncapi.service.folder;

import com.syncapi.TestUtil;
import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.exception.AccessDeniedException;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private Util util;

    private FolderService folderService;

    private String testEmail;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        folderService = new FolderService(folderRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        User testUser = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        testWorkspace = TestUtil.createRandomWorkspace(testUser);
    }

    @Test
    void shouldGetFoldersByWorkspace() {
        // given
        Folder folder1 = TestUtil.createRandomFolder(testWorkspace);
        Folder folder2 = TestUtil.createRandomFolder(testWorkspace);

        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
        when(folderRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(List.of(folder1, folder2));

        // when
        List<FolderResponse> result = folderService.getFoldersByWorkspace(testWorkspace.getId(), testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(folder1.getId());
        assertThat(result.get(1).getId()).isEqualTo(folder2.getId());

        verify(util).getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail);
        verify(folderRepository).findByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoFolders() {
        // given
        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
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
        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new ResourceNotFoundException("Workspace not found: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> folderService.getFoldersByWorkspace(workspaceId, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workspace not found: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verifyNoInteractions(folderRepository);
    }

    @Test
    void shouldThrowWhenGettingFoldersByNonMember() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);

        when(util.getWorkspaceWithAccessCheck(otherWorkspace.getId(), testEmail))
                .thenThrow(new AccessDeniedException("Access denied to workspace: " + otherWorkspace.getId()));

        // when / then
        assertThatThrownBy(() -> folderService.getFoldersByWorkspace(otherWorkspace.getId(), testEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to workspace");

        verifyNoInteractions(folderRepository);
    }

    @Test
    void shouldGetFolderById() {
        // given
        Folder folder = TestUtil.createRandomFolder(testWorkspace);
        folder.setDescription(TestUtil.generateRandomDescription());

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);

        // when
        FolderResponse result = folderService.getFolderById(folder.getId(), testEmail);

        // then
        assertThat(result.getId()).isEqualTo(folder.getId());
        assertThat(result.getName()).isEqualTo(folder.getName());
        assertThat(result.getDescription()).isEqualTo(folder.getDescription());
        assertThat(result.getWorkspaceId()).isEqualTo(testWorkspace.getId());

        verify(util).getFolderWithAccessCheck(folder.getId(), testEmail);
    }

    @Test
    void shouldThrowWhenGettingNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();
        when(util.getFolderWithAccessCheck(folderId, testEmail))
                .thenThrow(new ResourceNotFoundException("Folder not found: " + folderId));

        // when / then
        assertThatThrownBy(() -> folderService.getFolderById(folderId, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Folder not found");

        verify(util).getFolderWithAccessCheck(folderId, testEmail);
    }

    @Test
    void shouldThrowWhenGettingFolderByNonMember() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);
        Folder folder = TestUtil.createRandomFolder(otherWorkspace);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail))
                .thenThrow(new AccessDeniedException("Access denied to folder: " + folder.getId()));

        // when / then
        assertThatThrownBy(() -> folderService.getFolderById(folder.getId(), testEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to folder");

        verify(util).getFolderWithAccessCheck(folder.getId(), testEmail);
    }

    @Test
    void shouldCreateFolder() {
        // given
        String folderName = TestUtil.generateRandomName();
        String folderDescription = TestUtil.generateRandomDescription();
        FolderRequest request = new FolderRequest(folderName, folderDescription);

        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
        when(folderRepository.save(any(Folder.class))).thenAnswer(this::saveFolderStubbing);

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

        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
        when(folderRepository.save(any(Folder.class))).thenAnswer(this::saveFolderStubbing);

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

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new ResourceNotFoundException("Workspace not found: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> folderService.createFolder(workspaceId, request, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workspace not found: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verifyNoInteractions(folderRepository);
    }

    @Test
    void shouldThrowWhenCreatingFolderByNonMember() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(util.getWorkspaceWithAccessCheck(otherWorkspace.getId(), testEmail))
                .thenThrow(new AccessDeniedException("Access denied to workspace: " + otherWorkspace.getId()));

        // when / then
        assertThatThrownBy(() -> folderService.createFolder(otherWorkspace.getId(), request, testEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to workspace");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldUpdateFolder() {
        // given
        String newName = TestUtil.generateRandomName();
        String newDescription = TestUtil.generateRandomDescription();
        FolderRequest request = new FolderRequest(newName, newDescription);

        Folder folder = TestUtil.createRandomFolder(testWorkspace);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);
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
        Folder folder = TestUtil.createRandomFolder(testWorkspace);
        folder.setDescription(TestUtil.generateRandomDescription());

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);
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

        when(util.getFolderWithAccessCheck(folderId, testEmail))
                .thenThrow(new ResourceNotFoundException("Folder not found: " + folderId));

        // when / then
        assertThatThrownBy(() -> folderService.updateFolder(folderId, request, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Folder not found");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingFolderByNonMember() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);
        Folder folder = TestUtil.createRandomFolder(otherWorkspace);

        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail))
                .thenThrow(new AccessDeniedException("Access denied to folder: " + folder.getId()));

        // when / then
        assertThatThrownBy(() -> folderService.updateFolder(folder.getId(), request, testEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to folder");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldPatchFolderName() {
        // given
        String folderDescription = TestUtil.generateRandomDescription();
        Folder folder = TestUtil.createRandomFolder(testWorkspace);
        folder.setDescription(folderDescription);

        String newName = TestUtil.generateRandomName();
        FolderRequest request = new FolderRequest();
        request.setName(newName);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);
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
        Folder folder = TestUtil.createRandomFolder(testWorkspace);
        String originalName = folder.getName();

        String newDescription = TestUtil.generateRandomDescription();
        FolderRequest request = new FolderRequest();
        request.setDescription(newDescription);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);
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
        Folder folder = TestUtil.createRandomFolder(testWorkspace);

        String newName = TestUtil.generateRandomName();
        String newDescription = TestUtil.generateRandomDescription();
        FolderRequest request = new FolderRequest();
        request.setName(newName);
        request.setDescription(newDescription);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);
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
        Folder folder = TestUtil.createRandomFolder(testWorkspace);
        folder.setDescription(TestUtil.generateRandomDescription());

        FolderRequest request = new FolderRequest();
        request.setDescription(""); // empty string should clear (set to null)

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);
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

        when(util.getFolderWithAccessCheck(folderId, testEmail))
                .thenThrow(new ResourceNotFoundException("Folder not found: " + folderId));

        // when / then
        assertThatThrownBy(() -> folderService.patchFolder(folderId, request, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Folder not found");

        verify(util).getFolderWithAccessCheck(folderId, testEmail);
        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatchingFolderByNonMember() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);
        Folder folder = TestUtil.createRandomFolder(otherWorkspace);

        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail))
                .thenThrow(new AccessDeniedException("Access denied to folder: " + folder.getId()));

        // when / then
        assertThatThrownBy(() -> folderService.patchFolder(folder.getId(), request, testEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to folder");

        verify(folderRepository, never()).save(any());
    }

    @Test
    void shouldDeleteFolder() {
        // given
        Folder folder = TestUtil.createRandomFolder(testWorkspace);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail)).thenReturn(folder);

        // when
        folderService.deleteFolder(folder.getId(), testEmail);

        // then
        verify(folderRepository).delete(folder);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();

        when(util.getFolderWithAccessCheck(folderId, testEmail))
                .thenThrow(new ResourceNotFoundException("Folder not found: " + folderId));

        // when / then
        assertThatThrownBy(() -> folderService.deleteFolder(folderId, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Folder not found");

        verify(folderRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingFolderByNonMember() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);
        Folder folder = TestUtil.createRandomFolder(otherWorkspace);

        when(util.getFolderWithAccessCheck(folder.getId(), testEmail))
                .thenThrow(new AccessDeniedException("Access denied to folder: " + folder.getId()));

        // when / then
        assertThatThrownBy(() -> folderService.deleteFolder(folder.getId(), testEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to folder");

        verify(folderRepository, never()).delete(any());
    }

    private Folder saveFolderStubbing(InvocationOnMock invocation) {
        Folder folder = invocation.getArgument(0);
        setField(folder, "id", TestUtil.generateRandomId());

        return folder;
    }
}
