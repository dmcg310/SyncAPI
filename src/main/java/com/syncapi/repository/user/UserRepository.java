package com.syncapi.repository.user;

import com.syncapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by email.
     *
     * @param email the user's email
     * @return an optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by email.
     *
     * @param email the user's email
     * @return true if a user with the email exists
     */
    boolean existsByEmail(String email);
}
