package com.syncapi.repository.request;

import com.syncapi.entity.request.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Request entities.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    /**
     * Finds all requests in a folder.
     *
     * @param folderId the folder ID
     * @return a list of requests
     */
    List<Request> findByFolderId(Long folderId);
}
