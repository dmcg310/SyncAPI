package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.TestUtil;
import com.syncapi.entity.Folder;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FolderRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private Workspace workspace;
    private Folder folder1, folder2;

    @BeforeEach
    void setUp() {
        folderRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User(TestUtil.generateRandomEmail(), TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName()));

        workspace = new Workspace(TestUtil.generateRandomName());
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        folder1 = new Folder(TestUtil.generateRandomName(), workspace);
        folderRepository.save(folder1);

        folder2 = new Folder(TestUtil.generateRandomName(), workspace);
        folderRepository.save(folder2);
    }

    @Test
    void shouldSaveFolder() {
        // given
        String folderName = TestUtil.generateRandomName();

        // when
        Folder saved = folderRepository.save(new Folder(folderName, workspace));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(folderName);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getWorkspace()).isEqualTo(workspace);
    }

    @Test
    void shouldFindFoldersByWorkspaceId() {
        // when
        List<Folder> folders = folderRepository.findByWorkspaceId(workspace.getId());

        // then
        assertThat(folders).hasSize(2);
        assertThat(folders).extracting(Folder::getName)
                .containsExactlyInAnyOrder(folder1.getName(), folder2.getName());
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoFolders() {
        // given
        Workspace emptyWorkspace = workspaceRepository.save(new Workspace(TestUtil.generateRandomName()));

        // when
        List<Folder> folders = folderRepository.findByWorkspaceId(emptyWorkspace.getId());

        // then
        assertThat(folders).isEmpty();
    }

    @Test
    void shouldFindFolderByIdAndWorkspaceId() {
        // when
        Folder found = folderRepository.findByIdAndWorkspaceId(folder1.getId(), workspace.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(folder1.getName());
    }

    @Test
    void shouldReturnNullWhenFolderNotInWorkspace() {
        // given
        Workspace anotherWorkspace = workspaceRepository.save(new Workspace(TestUtil.generateRandomName()));

        // when
        Folder found = folderRepository.findByIdAndWorkspaceId(folder1.getId(), anotherWorkspace.getId());

        // then
        assertThat(found).isNull();
    }

    @Test
    void shouldCascadeDeleteWhenWorkspaceDeleted() {
        // given
        Long folderId = folder1.getId();

        // when
        workspaceRepository.deleteById(workspace.getId());

        // then
        assertThat(folderRepository.findById(folderId)).isEmpty();
    }
}