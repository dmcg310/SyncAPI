package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.syncapi.repository.RepositoryTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;
    private Workspace workspace1, workspace2;

    @BeforeEach
    void setUp() {
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User(generateRandomEmail(), generateRandomPasswordHash(), generateRandomName());
        user1 = userRepository.save(user1);

        user2 = new User(generateRandomEmail(), generateRandomPasswordHash(), generateRandomName());
        user2 = userRepository.save(user2);

        workspace1 = new Workspace("Workspace 1");
        workspace1.getMembers().add(user1);
        workspace1 = workspaceRepository.save(workspace1);

        workspace2 = new Workspace("Workspace 2");
        workspace2.getMembers().addAll(List.of(user1, user2));
        workspace2 = workspaceRepository.save(workspace2);
    }

    @Test
    void shouldSaveWorkspace() {
        // given
        String workspaceName = "New Workspace";

        // when
        Workspace saved = workspaceRepository.save(new Workspace(workspaceName));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(workspaceName);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindWorkspacesByMemberId() {
        // when
        List<Workspace> workspaces = workspaceRepository.findByMemberId(user1.getId());

        // then
        assertThat(workspaces).hasSize(2);
        assertThat(workspaces).extracting(Workspace::getName)
                .containsExactlyInAnyOrder(workspace1.getName(), workspace2.getName());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoWorkspaces() {
        // given
        User user3 = userRepository.save(new User(generateRandomEmail(), generateRandomPasswordHash(),
                generateRandomName()));

        // when
        List<Workspace> workspaces = workspaceRepository.findByMemberId(user3.getId());

        // then
        assertThat(workspaces).isEmpty();
    }

    @Test
    @Transactional
    void shouldHandleManyToManyRelationship() {
        // when
        Workspace found = workspaceRepository.findById(workspace2.getId()).orElseThrow();

        // then
        assertThat(found.getMembers()).hasSize(2);
        assertThat(found.getMembers()).contains(user1, user2);
    }

    @Test
    void shouldCascadeDeleteToFolders() {
        // given
        Workspace workspace = new Workspace("Test Workspace");
        workspace = workspaceRepository.save(workspace);

        Long workspaceId = workspace.getId();

        // when
        workspaceRepository.delete(workspace);

        // then
        assertThat(workspaceRepository.findById(workspaceId)).isEmpty();
    }
}
