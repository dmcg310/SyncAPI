package com.syncapi.repository;

import com.syncapi.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    List<Environment> findByWorkspaceId(Long workspaceId);

    Optional<Environment> findByWorkspaceIdAndIsActiveTrue(Long workspaceId);
}
