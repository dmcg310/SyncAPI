package com.syncapi.repository;

import com.syncapi.SyncapiApplication;
import com.syncapi.TestUtil;
import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.environment.EnvironmentRepository;
import com.syncapi.repository.environment.EnvironmentVariableRepository;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.repository.workspace.WorkspaceRepository;
import com.syncapi.util.RequestMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SyncapiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RepositoryIntegrationTest {
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private EnvironmentVariableRepository environmentVariableRepository;

    @Test
    void userRepositoryFindByEmailAndExistsByEmailWork() {
        User user = new User();
        user.setName(TestUtil.generateRandomName());
        user.setEmail(TestUtil.generateRandomEmail());
        user.setPasswordHash(TestUtil.generateRandomPasswordHash());
        userRepository.save(user);

        assertThat(userRepository.existsByEmail(user.getEmail())).isTrue();
        assertThat(userRepository.findByEmail(user.getEmail())).isPresent();
        assertThat(userRepository.findByEmail("missing-" + TestUtil.generateRandomEmail())).isEmpty();
    }

    @Test
    void workspaceRepositoryFindByMemberIdWorks() {
        User user = new User();
        user.setName(TestUtil.generateRandomName());
        user.setEmail(TestUtil.generateRandomEmail());
        user.setPasswordHash(TestUtil.generateRandomPasswordHash());
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        List<Workspace> found = workspaceRepository.findByMemberId(user.getId());
        assertThat(found).extracting(Workspace::getId).contains(workspace.getId());
    }

    @Test
    void folderRepositoryFindByWorkspaceIdWorks() {
        Workspace workspace = createWorkspaceWithOneMember();

        Folder folder = new Folder();
        folder.setName(TestUtil.generateRandomName());
        folder.setWorkspace(workspace);
        folderRepository.save(folder);

        List<Folder> found = folderRepository.findByWorkspaceId(workspace.getId());
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getWorkspace().getId()).isEqualTo(workspace.getId());
    }

    @Test
    void requestRepositoryFindByFolderIdWorks() {
        Workspace workspace = createWorkspaceWithOneMember();

        Folder folder = new Folder();
        folder.setName(TestUtil.generateRandomName());
        folder.setWorkspace(workspace);
        folder = folderRepository.save(folder);

        Request request = new Request();
        request.setName(TestUtil.generateRandomName());
        request.setMethod(RequestMethod.GET);
        request.setUrl(TestUtil.generateRandomUrl());
        request.setFolder(folder);
        requestRepository.save(request);

        List<Request> found = requestRepository.findByFolderId(folder.getId());
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getFolder().getId()).isEqualTo(folder.getId());
    }

    @Test
    void environmentVariableRepositoryFindByEnvironmentIdWorks() {
        Workspace workspace = createWorkspaceWithOneMember();

        Environment environment = new Environment();
        environment.setName(TestUtil.generateRandomName());
        environment.setWorkspace(workspace);
        environment = environmentRepository.save(environment);

        EnvironmentVariable var = new EnvironmentVariable();
        var.setKey(TestUtil.generateRandomKey());
        var.setValue(TestUtil.generateRandomValue());
        var.setEnvironment(environment);
        environmentVariableRepository.save(var);

        List<EnvironmentVariable> found = environmentVariableRepository.findByEnvironmentId(environment.getId());
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getEnvironment().getId()).isEqualTo(environment.getId());
    }

    @Test
    void environmentRepositoryDeactivateAllInWorkspaceSetsAllToFalse() {
        Workspace workspace = createWorkspaceWithOneMember();

        Environment environment1 = new Environment();
        environment1.setName(TestUtil.generateRandomName());
        environment1.setWorkspace(workspace);
        environment1.setIsActive(true);
        environment1 = environmentRepository.save(environment1);

        Environment environment2 = new Environment();
        environment2.setName(TestUtil.generateRandomName());
        environment2.setWorkspace(workspace);
        environment2.setIsActive(true);
        environment2 = environmentRepository.save(environment2);

        assertThat(environmentRepository.findByWorkspaceId(workspace.getId()))
                .extracting(Environment::getIsActive)
                .containsExactlyInAnyOrder(true, true);

        environmentRepository.deactivateAllInWorkspace(workspace.getId());
        environmentRepository.flush();

        List<Environment> updated = environmentRepository.findByWorkspaceId(workspace.getId());
        assertThat(updated).extracting(Environment::getIsActive).containsOnly(false);
    }

    private Workspace createWorkspaceWithOneMember() {
        User user = new User();
        user.setName(TestUtil.generateRandomName());
        user.setEmail(TestUtil.generateRandomEmail());
        user.setPasswordHash(TestUtil.generateRandomPasswordHash());
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setName(TestUtil.generateRandomName());
        workspace.getMembers().add(user);
        return workspaceRepository.save(workspace);
    }
}
