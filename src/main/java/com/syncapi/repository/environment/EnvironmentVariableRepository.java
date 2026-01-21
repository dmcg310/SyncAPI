package com.syncapi.repository.environment;

import com.syncapi.entity.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariable, Long> {
    List<EnvironmentVariable> findByEnvironmentId(Long environmentId);
}
