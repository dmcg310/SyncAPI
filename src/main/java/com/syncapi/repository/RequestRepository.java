package com.syncapi.repository;

import com.syncapi.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByFolderId(Long folderId);

    Request findByIdAndFolderId(Long id, Long folderId);
}
