package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByOrganizationIdAndKey(UUID orgId, String key);
    Page<Project> findByOrganizationId(UUID orgId, Pageable pageable);
}
