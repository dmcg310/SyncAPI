package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.TestUtil;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

        user1 = userRepository.save(new User(TestUtil.generateRandomEmail(), TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName()));

        user2 = userRepository.save(new User(TestUtil.generateRandomEmail(), TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName()));

        workspace1 = new Workspace(TestUtil.generateRandomName());
        workspace1.getMembers().add(user1);
        workspace1 = workspaceRepository.save(workspace1);

        workspace2 = new Workspace(TestUtil.generateRandomName());
        workspace2.getMembers().add(user1);
        workspace2.getMembers().add(user2);
        workspace2 = workspaceRepository.save(workspace2);
    }

    @Test
    void shouldSaveWorkspace() {
        // given
        String workspaceName = TestUtil.generateRandomName();

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
        User newUser = userRepository.save(new User(TestUtil.generateRandomEmail(),
                TestUtil.generateRandomPasswordHash(), TestUtil.generateRandomName()));

        // when
        List<Workspace> workspaces = workspaceRepository.findByMemberId(newUser.getId());

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
        Workspace newWorkspace = new Workspace(TestUtil.generateRandomName());
        newWorkspace.getMembers().add(user1);
        newWorkspace = workspaceRepository.save(newWorkspace);
        Long workspaceId = newWorkspace.getId();

        // when
        workspaceRepository.delete(newWorkspace);

        // then
        assertThat(workspaceRepository.findById(workspaceId)).isEmpty();
    }
}
