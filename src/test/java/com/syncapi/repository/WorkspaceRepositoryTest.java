package com.syncapi.repository;

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
class WorkspaceRepositoryTest {
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

        user1 = new User("user1@example.com", "hash1", "User 1");
        user2 = new User("user2@example.com", "hash2", "User 2");
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        workspace1 = new Workspace("Workspace 1");
        workspace1.getMembers().add(user1);
        workspace1 = workspaceRepository.save(workspace1);

        workspace2 = new Workspace("Workspace 2");
        workspace2.getMembers().add(user1);
        workspace2.getMembers().add(user2);
        workspace2 = workspaceRepository.save(workspace2);
    }

    @Test
    void shouldSaveWorkspace() {
        // when
        Workspace saved = workspaceRepository.save(new Workspace("New Workspace"));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Workspace");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindWorkspacesByMemberId() {
        // when
        List<Workspace> workspaces = workspaceRepository.findByMemberId(user1.getId());

        // then
        assertThat(workspaces).hasSize(2);
        assertThat(workspaces).extracting(Workspace::getName)
                .containsExactlyInAnyOrder("Workspace 1", "Workspace 2");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoWorkspaces() {
        // given
        User user3 = userRepository.save(new User("user3@example.com", "hash3", "User 3"));

        // when
        List<Workspace> workspaces = workspaceRepository.findByMemberId(user3.getId());

        // then
        assertThat(workspaces).isEmpty();
    }

    @Test
    void shouldHandleManyToManyRelationship() {
        // when
        Workspace found = workspaceRepository.findById(workspace2.getId()).orElseThrow();

        // then
        assertThat(found.getMembers()).hasSize(2);
        assertThat(found.getMembers()).contains(user1, user2);
    }
}
