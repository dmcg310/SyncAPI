package com.syncapi.service.environment;

import com.syncapi.TestUtil;
import com.syncapi.dto.environment.EnvironmentRequest;
import com.syncapi.dto.environment.EnvironmentResponse;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.repository.environment.EnvironmentRepository;
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
class EnvironmentServiceTest {
    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private Util util;

    private EnvironmentService environmentService;

    private String testEmail;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        environmentService = new EnvironmentService(environmentRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        User testUser = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        testWorkspace = TestUtil.createRandomWorkspace(testUser);
    }

    @Test
    void shouldGetEnvironmentsByWorkspace() {
        // given
        Environment env1 = TestUtil.createRandomEnvironment(testWorkspace);
        Environment env2 = TestUtil.createRandomEnvironment(testWorkspace);

        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
        when(environmentRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(List.of(env1, env2));

        // when
        List<EnvironmentResponse> result = environmentService.getEnvironmentsByWorkspace(testWorkspace.getId(),
                testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(env1.getId());
        assertThat(result.get(1).getId()).isEqualTo(env2.getId());

        verify(util).getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail);
        verify(environmentRepository).findByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoEnvironments() {
        // given
        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
        when(environmentRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(List.of());

        // when
        List<EnvironmentResponse> result = environmentService.getEnvironmentsByWorkspace(testWorkspace.getId(),
                testEmail);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowWhenGettingEnvironmentsFromNonExistentWorkspace() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, testEmail))
                .thenThrow(new ResourceNotFoundException("Workspace not found: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> environmentService.getEnvironmentsByWorkspace(workspaceId, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workspace not found: " + workspaceId);

        verify(util).getWorkspaceWithAccessCheck(workspaceId, testEmail);
        verifyNoInteractions(environmentRepository);
    }

    @Test
    void shouldGetEnvironmentById_includesVariables() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        env.setDescription(TestUtil.generateRandomDescription());
        env.setIsActive(true);

        EnvironmentVariable v1 = TestUtil.createRandomEnvironmentVariable(env);
        EnvironmentVariable v2 = TestUtil.createRandomEnvironmentVariable(env);
        env.setVariables(List.of(v1, v2));

        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);

        // when
        EnvironmentResponse result = environmentService.getEnvironmentById(env.getId(), testEmail);

        // then
        assertThat(result.getId()).isEqualTo(env.getId());
        assertThat(result.getName()).isEqualTo(env.getName());
        assertThat(result.getDescription()).isEqualTo(env.getDescription());
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getWorkspaceId()).isEqualTo(testWorkspace.getId());
        assertThat(result.getVariableCount()).isEqualTo(2);

        assertThat(result.getVariables()).hasSize(2);
        assertThat(result.getVariables())
                .extracting(EnvironmentVariableResponse::getEnvironmentId)
                .containsOnly(env.getId());

        verify(util).getEnvironmentWithAccessCheck(env.getId(), testEmail);
    }

    @Test
    void shouldThrowWhenGettingNonExistentEnvironment() {
        // given
        Long environmentId = TestUtil.generateRandomId();

        when(util.getEnvironmentWithAccessCheck(environmentId, testEmail))
                .thenThrow(new ResourceNotFoundException("Environment not found: " + environmentId));

        // when / then
        assertThatThrownBy(() -> environmentService.getEnvironmentById(environmentId, testEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Environment not found");

        verify(util).getEnvironmentWithAccessCheck(environmentId, testEmail);
    }

    @Test
    void shouldCreateEnvironment_defaultsIsActiveFalseWhenNull() {
        // given
        String name = TestUtil.generateRandomName();
        String description = TestUtil.generateRandomDescription();

        EnvironmentRequest request = new EnvironmentRequest();
        request.setName(name);
        request.setDescription(description);
        request.setIsActive(null);

        when(util.getWorkspaceWithAccessCheck(testWorkspace.getId(), testEmail)).thenReturn(testWorkspace);
        when(environmentRepository.save(any(Environment.class))).thenAnswer(this::saveEnvironmentStubbing);
        when(util.defaultFalse(null)).thenReturn(false);

        // when
        EnvironmentResponse result = environmentService.createEnvironment(testWorkspace.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getIsActive()).isFalse();

        ArgumentCaptor<Environment> captor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository).save(captor.capture());

        Environment saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getDescription()).isEqualTo(description);
        assertThat(saved.getIsActive()).isFalse();
        assertThat(saved.getWorkspace()).isEqualTo(testWorkspace);
    }

    @Test
    void shouldUpdateEnvironment_fullPut_clearsDescriptionWhenNull() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        env.setDescription(TestUtil.generateRandomDescription());
        env.setIsActive(true);

        EnvironmentRequest request = new EnvironmentRequest();
        request.setName(TestUtil.generateRandomName());
        request.setDescription(null);
        request.setIsActive(null);

        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);
        when(environmentRepository.save(any(Environment.class))).thenReturn(env);
        when(util.defaultFalse(null)).thenReturn(false);

        // when
        EnvironmentResponse result = environmentService.updateEnvironment(env.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getDescription()).isNull();
        assertThat(result.getIsActive()).isFalse();

        verify(environmentRepository).save(env);
    }

    @Test
    void shouldPatchEnvironment_name_only() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        String originalDescription = TestUtil.generateRandomDescription();
        env.setDescription(originalDescription);
        env.setIsActive(false);

        EnvironmentRequest request = new EnvironmentRequest();
        request.setName(TestUtil.generateRandomName());

        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);
        when(environmentRepository.save(any(Environment.class))).thenReturn(env);

        // when
        EnvironmentResponse result = environmentService.patchEnvironment(env.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getDescription()).isEqualTo(originalDescription);
        assertThat(result.getIsActive()).isFalse();

        verify(environmentRepository).save(env);
    }

    @Test
    void shouldPatchEnvironment_clearDescriptionWithEmptyString() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        env.setDescription(TestUtil.generateRandomDescription());

        EnvironmentRequest request = new EnvironmentRequest();
        request.setDescription("");

        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);
        when(environmentRepository.save(any(Environment.class))).thenReturn(env);

        // when
        EnvironmentResponse result = environmentService.patchEnvironment(env.getId(), request, testEmail);

        // then
        assertThat(result.getDescription()).isNull();

        verify(environmentRepository).save(env);
    }

    @Test
    void shouldPatchEnvironment_isActive_only() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        env.setIsActive(false);

        EnvironmentRequest request = new EnvironmentRequest();
        request.setIsActive(true);

        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);
        when(environmentRepository.save(any(Environment.class))).thenReturn(env);

        // when
        EnvironmentResponse result = environmentService.patchEnvironment(env.getId(), request, testEmail);

        // then
        assertThat(result.getIsActive()).isTrue();
        verify(environmentRepository).save(env);
    }

    @Test
    void shouldDeleteEnvironment() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);

        // when
        environmentService.deleteEnvironment(env.getId(), testEmail);

        // then
        verify(environmentRepository).delete(env);
    }

    @Test
    void shouldSetEnvironmentActiveStatus_onlyOneActivePerWorkspace() {
        // given
        Environment env = TestUtil.createRandomEnvironment(testWorkspace);
        env.setIsActive(false);

        when(util.getEnvironmentWithAccessCheck(env.getId(), testEmail)).thenReturn(env);
        when(environmentRepository.save(any(Environment.class))).thenReturn(env);

        // when
        EnvironmentResponse result = environmentService.setEnvironmentActiveStatus(env.getId(), true, testEmail);

        // then
        assertThat(result.getIsActive()).isTrue();

        verify(environmentRepository).deactivateAllInWorkspace(testWorkspace.getId());
        verify(environmentRepository).save(env);
    }

    private Environment saveEnvironmentStubbing(InvocationOnMock invocation) {
        Environment env = invocation.getArgument(0);
        setField(env, "id", TestUtil.generateRandomId());

        return env;
    }
}
