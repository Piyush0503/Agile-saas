package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.IssueActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IssueActivityRepository extends JpaRepository<IssueActivity, UUID> {
    Page<IssueActivity> findByIssueIdOrderByCreatedAtDesc(UUID issueId, Pageable pageable);
}
