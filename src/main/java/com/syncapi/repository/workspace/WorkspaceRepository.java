package com.syncapi.repository.workspace;

import com.syncapi.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.id = :userId")
    List<Workspace> findByMemberId(@Param("userId") Long userId);
}
