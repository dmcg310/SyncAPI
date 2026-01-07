package com.syncapi.repository;

import com.syncapi.entity.Folder;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FolderRepositoryTest {
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

        User user = userRepository.save(new User("test@example.com", "hash", "Test User"));
        workspace = new Workspace("Test Workspace");
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        folder1 = new Folder("Folder 1", workspace);
        folderRepository.save(folder1);
        folder2 = new Folder("Folder 2", workspace);
        folderRepository.save(folder2);
    }

    @Test
    void shouldSaveFolder() {
        // when
        Folder saved = folderRepository.save(new Folder("New Folder", workspace));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Folder");
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
                .containsExactlyInAnyOrder("Folder 1", "Folder 2");
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoFolders() {
        // given
        Workspace emptyWorkspace = workspaceRepository.save(new Workspace("Empty Workspace"));

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
        assertThat(found.getName()).isEqualTo("Folder 1");
    }

    @Test
    void shouldReturnNullWhenFolderNotInWorkspace() {
        // given
        Workspace anotherWorkspace = workspaceRepository.save(new Workspace("Another Workspace"));

        // when
        Folder found = folderRepository.findByIdAndWorkspaceId(folder1.getId(), anotherWorkspace.getId());

        // then
        assertThat(found).isNull();
    }
}