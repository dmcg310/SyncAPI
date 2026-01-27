package com.syncapi.repository.environment;

import com.syncapi.entity.environment.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Environment entities.
 */
@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    /**
     * Finds all environments in a workspace.
     *
     * @param workspaceId the workspace ID
     * @return a list of environments
     */
    List<Environment> findByWorkspaceId(Long workspaceId);

    /**
     * Deactivates all environments in a workspace.
     *
     * @param workspaceId the workspace ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Environment e SET e.isActive = false WHERE e.workspace.id = :workspaceId")
    void deactivateAllInWorkspace(@Param("workspaceId") Long workspaceId);
}
