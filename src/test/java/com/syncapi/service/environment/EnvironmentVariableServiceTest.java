package com.syncapi.service.environment;

import com.syncapi.TestUtil;
import com.syncapi.dto.environment.variable.EnvironmentVariableRequest;
import com.syncapi.dto.environment.variable.EnvironmentVariableResponse;
import com.syncapi.entity.environment.Environment;
import com.syncapi.entity.environment.variable.EnvironmentVariable;
import com.syncapi.entity.user.User;
import com.syncapi.entity.workspace.Workspace;
import com.syncapi.exception.BadRequestException;
import com.syncapi.repository.environment.variable.EnvironmentVariableRepository;
import com.syncapi.service.environment.variable.EnvironmentVariableService;
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
class EnvironmentVariableServiceTest {

    @Mock
    private EnvironmentVariableRepository environmentVariableRepository;

    @Mock
    private Util util;

    private EnvironmentVariableService environmentVariableService;

    private String testEmail;
    private Environment testEnvironment;

    @BeforeEach
    void setUp() {
        environmentVariableService = new EnvironmentVariableService(environmentVariableRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        User testUser = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        Workspace testWorkspace = TestUtil.createRandomWorkspace(testUser);
        testEnvironment = TestUtil.createRandomEnvironment(testWorkspace);
    }

    @Test
    void shouldGetVariablesByEnvironment() {
        // given
        EnvironmentVariable var1 = TestUtil.createRandomEnvironmentVariable(testEnvironment);
        EnvironmentVariable var2 = TestUtil.createRandomEnvironmentVariable(testEnvironment);

        when(util.getEnvironmentWithAccessCheck(testEnvironment.getId(), testEmail)).thenReturn(testEnvironment);
        when(environmentVariableRepository.findByEnvironmentId(testEnvironment.getId())).thenReturn(List.of(var1, var2));

        // when
        List<EnvironmentVariableResponse> result = environmentVariableService.getVariablesByEnvironment(
                testEnvironment.getId(), testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getEnvironmentId()).isEqualTo(testEnvironment.getId());

        verify(util).getEnvironmentWithAccessCheck(testEnvironment.getId(), testEmail);
        verify(environmentVariableRepository).findByEnvironmentId(testEnvironment.getId());
    }

    @Test
    void shouldAddVariable() {
        // given
        EnvironmentVariableRequest request = new EnvironmentVariableRequest(TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue());

        when(util.getEnvironmentWithAccessCheck(testEnvironment.getId(), testEmail)).thenReturn(testEnvironment);
        when(environmentVariableRepository.save(any(EnvironmentVariable.class))).thenAnswer(this::saveVariableStubbing);

        // when
        EnvironmentVariableResponse result = environmentVariableService.addVariable(testEnvironment.getId(), request,
                testEmail);

        // then
        assertThat(result.getKey()).isEqualTo(request.getKey());
        assertThat(result.getValue()).isEqualTo(request.getValue());
        assertThat(result.getEnvironmentId()).isEqualTo(testEnvironment.getId());

        ArgumentCaptor<EnvironmentVariable> captor = ArgumentCaptor.forClass(EnvironmentVariable.class);
        verify(environmentVariableRepository).save(captor.capture());

        EnvironmentVariable saved = captor.getValue();
        assertThat(saved.getKey()).isEqualTo(request.getKey());
        assertThat(saved.getValue()).isEqualTo(request.getValue());
        assertThat(saved.getEnvironment()).isEqualTo(testEnvironment);
    }

    @Test
    void shouldUpdateVariable() {
        // given
        Long variableId = TestUtil.generateRandomId();
        EnvironmentVariable variable = TestUtil.createRandomEnvironmentVariable(testEnvironment);
        setField(variable, "id", variableId);

        EnvironmentVariableRequest request = new EnvironmentVariableRequest(TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue());

        when(util.getEnvironmentVariableWithAccessCheck(variableId, testEmail)).thenReturn(variable);
        when(environmentVariableRepository.save(any(EnvironmentVariable.class))).thenReturn(variable);

        // when
        EnvironmentVariableResponse result = environmentVariableService.updateVariable(testEnvironment.getId(),
                variableId, request, testEmail);

        // then
        assertThat(result.getId()).isEqualTo(variableId);
        assertThat(result.getKey()).isEqualTo(request.getKey());
        assertThat(result.getValue()).isEqualTo(request.getValue());

        verify(environmentVariableRepository).save(variable);
    }

    @Test
    void shouldThrowWhenUpdatingVariableBelongsToDifferentEnvironment() {
        // given
        Long variableId = TestUtil.generateRandomId();

        Workspace otherWs = TestUtil.createRandomWorkspace(TestUtil.createRandomUser());
        Environment otherEnv = TestUtil.createRandomEnvironment(otherWs);

        EnvironmentVariable variable = TestUtil.createRandomEnvironmentVariable(otherEnv);
        setField(variable, "id", variableId);

        EnvironmentVariableRequest request = new EnvironmentVariableRequest(TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue());

        when(util.getEnvironmentVariableWithAccessCheck(variableId, testEmail)).thenReturn(variable);

        // when / then
        assertThatThrownBy(() ->
                environmentVariableService.updateVariable(testEnvironment.getId(), variableId, request, testEmail)
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong");

        verify(environmentVariableRepository, never()).save(any());
    }

    @Test
    void shouldDeleteVariable() {
        // given
        Long variableId = TestUtil.generateRandomId();

        EnvironmentVariable variable = TestUtil.createRandomEnvironmentVariable(testEnvironment);
        setField(variable, "id", variableId);

        when(util.getEnvironmentVariableWithAccessCheck(variableId, testEmail)).thenReturn(variable);

        // when
        environmentVariableService.deleteVariable(testEnvironment.getId(), variableId, testEmail);

        // then
        verify(environmentVariableRepository).delete(variable);
    }

    private EnvironmentVariable saveVariableStubbing(InvocationOnMock invocation) {
        EnvironmentVariable variable = invocation.getArgument(0);
        setField(variable, "id", TestUtil.generateRandomId());

        return variable;
    }
}
