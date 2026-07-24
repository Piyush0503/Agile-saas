package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.IssueComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IssueCommentRepository extends JpaRepository<IssueComment, UUID> {
    Page<IssueComment> findByIssueIdOrderByCreatedAtAsc(UUID issueId, Pageable pageable);
}
