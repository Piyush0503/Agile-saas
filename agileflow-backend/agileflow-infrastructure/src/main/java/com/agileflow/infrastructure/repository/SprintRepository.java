package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    // 3. Analytics query: Active sprint with velocity (issues fetched to calculate velocity in service)
    @Query("SELECT s FROM Sprint s LEFT JOIN FETCH s.issues i WHERE s.project.id = :projectId AND s.status = 'ACTIVE'")
    Optional<Sprint> findActiveSprintWithVelocity(@Param("projectId") UUID projectId);

}
