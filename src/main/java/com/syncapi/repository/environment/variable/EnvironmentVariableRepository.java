package com.syncapi.repository.environment.variable;

import com.syncapi.entity.environment.variable.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for EnvironmentVariable entities.
 */
public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariable, Long> {
    /**
     * Finds all variables in an environment.
     *
     * @param environmentId the environment ID
     * @return a list of environment variables
     */
    List<EnvironmentVariable> findByEnvironmentId(Long environmentId);
}
