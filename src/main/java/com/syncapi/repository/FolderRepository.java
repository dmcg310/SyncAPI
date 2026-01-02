package com.syncapi.repository;

import com.syncapi.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByWorkspaceId(Long workspaceId);

    Folder findByIdAndWorkspaceId(Long id, Long workspaceId);
}
