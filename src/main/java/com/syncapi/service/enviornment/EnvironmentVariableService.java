package com.syncapi.service.enviornment;

import com.syncapi.dto.environment.EnvironmentVariableRequest;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.repository.environment.EnvironmentVariableRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvironmentVariableService {
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final Util util;

    public EnvironmentVariableService(EnvironmentVariableRepository environmentVariableRepository, Util util) {
        this.environmentVariableRepository = environmentVariableRepository;
        this.util = util;
    }

    public List<EnvironmentVariableResponse> getVariablesByEnvironment(Long environmentId, String email) {
        util.getEnvironmentWithAccessCheck(environmentId, email);

        List<EnvironmentVariable> variables = environmentVariableRepository.findByEnvironmentId(environmentId);

        return variables.stream()
                .map(v -> toResponse(v, environmentId))
                .toList();
    }

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

    @Transactional
    public EnvironmentVariableResponse updateVariable(Long environmentId, Long variableId,
                                                      EnvironmentVariableRequest request, String email) {
        EnvironmentVariable variable = util.getEnvironmentVariableWithAccessCheck(variableId, email);
        if (!variable.getEnvironment().getId().equals(environmentId)) {
            throw new RuntimeException("Environment variable does not belong to this environment");
        }

        variable.setKey(request.getKey());
        variable.setValue(request.getValue());

        return toResponse(environmentVariableRepository.save(variable), environmentId);
    }

    @Transactional
    public void deleteVariable(Long environmentId, Long variableId, String email) {
        EnvironmentVariable variable = util.getEnvironmentVariableWithAccessCheck(variableId, email);
        if (!variable.getEnvironment().getId().equals(environmentId)) {
            throw new RuntimeException("Environment variable does not belong to this environment");
        }

        environmentVariableRepository.delete(variable);
    }

    private EnvironmentVariableResponse toResponse(EnvironmentVariable variable, Long environmentId) {
        return new EnvironmentVariableResponse(
                variable.getId(),
                variable.getKey(),
                variable.getValue(),
                environmentId
        );
    }
}
