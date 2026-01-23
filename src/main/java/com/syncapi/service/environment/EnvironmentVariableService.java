package com.syncapi.service.environment;

import com.syncapi.dto.environment.EnvironmentVariableRequest;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.exception.BadRequestException;
import com.syncapi.repository.environment.EnvironmentVariableRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for environment variable operations.
 */
@Service
public class EnvironmentVariableService {
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param environmentVariableRepository the environment variable repository
     * @param util                          the utility service
     */
    public EnvironmentVariableService(EnvironmentVariableRepository environmentVariableRepository, Util util) {
        this.environmentVariableRepository = environmentVariableRepository;
        this.util = util;
    }

    /**
     * Retrieves all variables for an environment.
     *
     * @param environmentId the environment ID
     * @param email         the user's email
     * @return the list of environment variable responses
     */
    public List<EnvironmentVariableResponse> getVariablesByEnvironment(Long environmentId, String email) {
        util.getEnvironmentWithAccessCheck(environmentId, email);

        List<EnvironmentVariable> variables = environmentVariableRepository.findByEnvironmentId(environmentId);

        return variables.stream()
                .map(v -> toResponse(v, environmentId))
                .toList();
    }

    /**
     * Adds a variable to an environment.
     *
     * @param environmentId the environment ID
     * @param request       the environment variable request
     * @param email         the user's email
     * @return the environment variable response
     */
    @Transactional
    public EnvironmentVariableResponse addVariable(Long environmentId, EnvironmentVariableRequest request,
                                                   String email) {
        Environment environment = util.getEnvironmentWithAccessCheck(environmentId, email);

        EnvironmentVariable variable = new EnvironmentVariable();
        variable.setKey(request.getKey());
        variable.setValue(request.getValue());
        variable.setEnvironment(environment);

        return toResponse(environmentVariableRepository.save(variable), environmentId);
    }

    /**
     * Updates a variable in an environment.
     *
     * @param environmentId the environment ID
     * @param variableId    the variable ID
     * @param request       the environment variable request
     * @param email         the user's email
     * @return the environment variable response
     */
    @Transactional
    public EnvironmentVariableResponse updateVariable(Long environmentId, Long variableId,
                                                      EnvironmentVariableRequest request, String email) {
        EnvironmentVariable variable = util.getEnvironmentVariableWithAccessCheck(variableId, email);
        if (!variable.getEnvironment().getId().equals(environmentId)) {
            throw new BadRequestException("Environment variable does not belong to this environment");
        }

        variable.setKey(request.getKey());
        variable.setValue(request.getValue());

        return toResponse(environmentVariableRepository.save(variable), environmentId);
    }

    /**
     * Deletes a variable from an environment.
     *
     * @param environmentId the environment ID
     * @param variableId    the variable ID
     * @param email         the user's email
     */
    @Transactional
    public void deleteVariable(Long environmentId, Long variableId, String email) {
        EnvironmentVariable variable = util.getEnvironmentVariableWithAccessCheck(variableId, email);
        if (!variable.getEnvironment().getId().equals(environmentId)) {
            throw new BadRequestException("Environment variable does not belong to this environment");
        }

        environmentVariableRepository.delete(variable);
    }

    /**
     * Converts an environment variable entity to an environment variable response.
     *
     * @param variable      the environment variable entity
     * @param environmentId the environment ID
     * @return the environment variable response
     */
    private EnvironmentVariableResponse toResponse(EnvironmentVariable variable, Long environmentId) {
        return new EnvironmentVariableResponse(
                variable.getId(),
                variable.getKey(),
                variable.getValue(),
                environmentId
        );
    }
}
