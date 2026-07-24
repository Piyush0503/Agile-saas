package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.Issue;
import com.agileflow.infrastructure.repository.projections.IssueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    // 1. Grouped by status for kanban board using Projection for lightweight reads
    // Returns projections mapped directly from JPA without loading full heavy entity tree
    @Query("SELECT i FROM Issue i LEFT JOIN FETCH i.assignee WHERE i.sprint.id = :sprintId ORDER BY i.position ASC")
    List<IssueProjection> findBySprintAndStatusGrouped(@Param("sprintId") UUID sprintId);

    // 2. Backlog issues (unassigned to sprint)
    @Query("SELECT i FROM Issue i LEFT JOIN FETCH i.assignee WHERE i.project.id = :projectId AND i.sprint IS NULL ORDER BY i.position ASC")
    Page<Issue> findBacklogIssues(@Param("projectId") UUID projectId, Pageable pageable);

    // 4. Full text search using PostgreSQL tsvector
    // (Native query projection mapping might require interface mapping support enabled in Spring Data)
    @Query(value = "SELECT * FROM issues WHERE fts_document @@ to_tsquery('english', :query) AND project_id = :projectId", nativeQuery = true)
    Page<Issue> searchByText(@Param("projectId") UUID projectId, @Param("query") String query, Pageable pageable);

    // 5. My open issues filter
    @Query("SELECT i FROM Issue i LEFT JOIN FETCH i.project p LEFT JOIN FETCH p.organization o WHERE i.assignee.id = :userId AND o.id = :orgId AND i.status != 'DONE'")
    Page<Issue> findByAssigneeAndOrg(@Param("userId") UUID userId, @Param("orgId") UUID orgId, Pageable pageable);
}
