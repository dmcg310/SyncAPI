package com.syncapi.repository.folder;

import com.syncapi.entity.folder.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Folder entities.
 */
@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    /**
     * Finds all folders in a workspace.
     *
     * @param workspaceId the workspace ID
     * @return a list of folders
     */
    List<Folder> findByWorkspaceId(Long workspaceId);
}
