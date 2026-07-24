package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.Epic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EpicRepository extends JpaRepository<Epic, UUID> {
    Page<Epic> findByProjectId(UUID projectId, Pageable pageable);
}
