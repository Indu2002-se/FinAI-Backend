package com.finai.backend.repository;

import com.finai.backend.entity.ChildProfile;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildProfileRepository extends JpaRepository<ChildProfile, Long> {
    List<ChildProfile> findByParentUser(User parentUser);
    Optional<ChildProfile> findByChildUser(User childUser);
    Optional<ChildProfile> findByIdAndParentUser(Long id, User parentUser);
}
