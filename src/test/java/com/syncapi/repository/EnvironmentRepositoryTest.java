package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.TestUtil;
import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private Workspace workspace;
    private Environment environment1, environment2;

    @BeforeEach
    void setUp() {
        environmentRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User(TestUtil.generateRandomEmail(), TestUtil.generateRandomPasswordHash(),
                TestUtil.generateRandomName()));

        workspace = new Workspace(TestUtil.generateRandomName());
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        environment1 = new Environment(TestUtil.generateRandomName(), workspace);
        environment1.setActive(true);
        environmentRepository.save(environment1);

        environment2 = new Environment(TestUtil.generateRandomName(), workspace);
        environment2.setActive(false);
        environmentRepository.save(environment2);
    }

    @Test
    void shouldSaveEnvironment() {
        // given
        String environmentName = TestUtil.generateRandomName();
        Environment environment = new Environment(environmentName, workspace);

        // when
        Environment saved = environmentRepository.save(environment);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(environmentName);
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindEnvironmentsByWorkspaceId() {
        // when
        List<Environment> environments = environmentRepository.findByWorkspaceId(workspace.getId());

        // then
        assertThat(environments).hasSize(2);
        assertThat(environments).extracting(Environment::getName)
                .containsExactlyInAnyOrder(environment1.getName(), environment2.getName());
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoEnvironments() {
        // given
        Workspace emptyWorkspace = workspaceRepository.save(new Workspace(TestUtil.generateRandomName()));

        // when
        List<Environment> environments = environmentRepository.findByWorkspaceId(emptyWorkspace.getId());

        // then
        assertThat(environments).isEmpty();
    }

    @Test
    void shouldFindActiveEnvironment() {
        // when
        Optional<Environment> active = environmentRepository.findByWorkspaceIdAndIsActiveTrue(workspace.getId());

        // then
        assertThat(active).isPresent();
        assertThat(active.get().getName()).isEqualTo(environment1.getName());
        assertThat(active.get().isActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNoActiveEnvironment() {
        // given
        environment1.setActive(false);
        environmentRepository.save(environment1);

        // when
        Optional<Environment> active = environmentRepository.findByWorkspaceIdAndIsActiveTrue(workspace.getId());

        // then
        assertThat(active).isEmpty();
    }

    @Test
    void shouldSaveEnvironmentWithVariables() {
        // given
        Environment environment = new Environment(TestUtil.generateRandomName(), workspace);

        EnvironmentVariable var1 = new EnvironmentVariable(TestUtil.generateRandomKey(), TestUtil.generateRandomValue(),
                environment);
        environment.getVariables().add(var1);

        EnvironmentVariable var2 = new EnvironmentVariable(TestUtil.generateRandomKey(), TestUtil.generateRandomValue(),
                environment);
        environment.getVariables().add(var2);

        // when
        Environment saved = environmentRepository.save(environment);

        // then
        assertThat(saved.getVariables()).hasSize(2);
    }

    @Test
    void shouldCascadeDeleteVariables() {
        // given
        Environment environment = new Environment(TestUtil.generateRandomName(), workspace);

        EnvironmentVariable var = new EnvironmentVariable(TestUtil.generateRandomKey(), TestUtil.generateRandomValue(),
                environment);
        environment.getVariables().add(var);

        environment = environmentRepository.save(environment);

        Long envId = environment.getId();

        // when
        environmentRepository.delete(environment);

        // then
        assertThat(environmentRepository.findById(envId)).isEmpty();
    }

    @Test
    void shouldCascadeDeleteWhenWorkspaceDeleted() {
        // given
        Long envId = environment1.getId();

        // when
        workspaceRepository.deleteById(workspace.getId());

        // then
        assertThat(environmentRepository.findById(envId)).isEmpty();
    }
}
