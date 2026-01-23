package com.syncapi.service.environment;

import com.syncapi.dto.environment.EnvironmentRequest;
import com.syncapi.dto.environment.EnvironmentResponse;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.entity.Environment;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.environment.EnvironmentRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for environment operations.
 */
@Service
public class EnvironmentService {
    private final EnvironmentRepository environmentRepository;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param environmentRepository the environment repository
     * @param util                  the utility service
     */
    public EnvironmentService(EnvironmentRepository environmentRepository, Util util) {
        this.environmentRepository = environmentRepository;
        this.util = util;
    }

    /**
     * Retrieves all environments for a workspace.
     *
     * @param workspaceId the workspace ID
     * @param email       the user's email
     * @return the list of environment responses
     */
    public List<EnvironmentResponse> getEnvironmentsByWorkspace(Long workspaceId, String email) {
        util.getWorkspaceWithAccessCheck(workspaceId, email);

        List<Environment> environments = environmentRepository.findByWorkspaceId(workspaceId);

        return environments.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves an environment by ID.
     *
     * @param environmentId the environment ID
     * @param email         the user's email
     * @return the environment response
     */
    @Transactional
    public EnvironmentResponse getEnvironmentById(Long environmentId, String email) {
        return toDetailedResponse(util.getEnvironmentWithAccessCheck(environmentId, email));
    }

    /**
     * Creates a new environment.
     *
     * @param workspaceId the workspace ID
     * @param request     the environment request
     * @param email       the user's email
     * @return the environment response
     */
    @Transactional
    public EnvironmentResponse createEnvironment(Long workspaceId, EnvironmentRequest request, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);

        Environment environment = new Environment();
        environment.setName(request.getName());
        environment.setDescription(request.getDescription());
        environment.setIsActive(util.defaultFalse(request.getIsActive()));
        environment.setWorkspace(workspace);

        return toResponse(environmentRepository.save(environment));
    }

    /**
     * Updates an environment.
     *
     * @param environmentId the environment ID
     * @param request       the environment request
     * @param email         the user's email
     * @return the environment response
     */
    @Transactional
    public EnvironmentResponse updateEnvironment(Long environmentId, EnvironmentRequest request, String email) {
        Environment environment = util.getEnvironmentWithAccessCheck(environmentId, email);

        environment.setName(request.getName());
        environment.setDescription(request.getDescription());
        environment.setIsActive(util.defaultFalse(request.getIsActive()));

        return toResponse(environmentRepository.save(environment));
    }

    /**
     * Partially updates an environment.
     *
     * @param environmentId the environment ID
     * @param request       the environment request
     * @param email         the user's email
     * @return the environment response
     */
    @Transactional
    public EnvironmentResponse patchEnvironment(Long environmentId, EnvironmentRequest request, String email) {
        Environment environment = util.getEnvironmentWithAccessCheck(environmentId, email);

        if (request.getName() != null) {
            environment.setName(request.getName());
        }
        if (request.getDescription() != null) {
            String description = request.getDescription().isBlank()
                    ? null
                    : request.getDescription();

            environment.setDescription(description);
        }
        if (request.getIsActive() != null) {
            environment.setIsActive(request.getIsActive());
        }

        return toResponse(environmentRepository.save(environment));
    }

    /**
     * Deletes an environment.
     *
     * @param environmentId the environment ID
     * @param email         the user's email
     */
    @Transactional
    public void deleteEnvironment(Long environmentId, String email) {
        environmentRepository.delete(util.getEnvironmentWithAccessCheck(environmentId, email));
    }

    /**
     * Sets the active status of an environment.
     *
     * @param environmentId the environment ID
     * @param isActive      the active status
     * @param email         the user's email
     * @return the environment response
     */
    @Transactional
    public EnvironmentResponse setEnvironmentActiveStatus(Long environmentId, boolean isActive, String email) {
        Environment environment = util.getEnvironmentWithAccessCheck(environmentId, email);

        if (isActive) {
            Long workspaceId = environment.getWorkspace().getId();
            environmentRepository.deactivateAllInWorkspace(workspaceId);

            environment.setIsActive(true);
        } else {
            environment.setIsActive(false);
        }

        return toResponse(environmentRepository.save(environment));
    }

    /**
     * Converts an environment entity to an environment response.
     *
     * @param environment the environment entity
     * @return the environment response
     */
    private EnvironmentResponse toResponse(Environment environment) {
        return new EnvironmentResponse(
                environment.getId(),
                environment.getName(),
                environment.getDescription(),
                environment.getIsActive(),
                environment.getCreatedAt(),
                environment.getWorkspace().getId(),
                environment.getVariables().size()
        );
    }

    /**
     * Converts an environment entity to a detailed environment response.
     *
     * @param environment the environment entity
     * @return the environment response
     */
    private EnvironmentResponse toDetailedResponse(Environment environment) {
        List<EnvironmentVariableResponse> vars =
                environment.getVariables().stream()
                        .map(v -> new EnvironmentVariableResponse(
                                v.getId(),
                                v.getKey(),
                                v.getValue(),
                                environment.getId()
                        ))
                        .toList();

        EnvironmentResponse response = toResponse(environment);
        response.setVariables(vars);
        response.setVariableCount(vars.size());

        return response;
    }
}
