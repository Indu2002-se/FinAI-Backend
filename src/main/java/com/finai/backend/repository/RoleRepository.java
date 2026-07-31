package com.finai.backend.repository;

import com.finai.backend.entity.Role;
import com.finai.backend.entity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role repository
 * Handles database operations for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by name
     * @param name the role name
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(RoleType name);
}
