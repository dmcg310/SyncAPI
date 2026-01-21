package com.syncapi.repository.environment;

import com.syncapi.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    List<Environment> findByWorkspaceId(Long workspaceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Environment e SET e.isActive = false WHERE e.workspace.id = :workspaceId")
    void deactivateAllInWorkspace(@Param("workspaceId") Long workspaceId);
}
