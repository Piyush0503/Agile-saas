package com.agileflow.api.controllers;

import com.agileflow.api.dto.CreateProjectRequest;
import com.agileflow.api.dto.ProjectResponse;
import com.agileflow.api.dto.UpdateProjectRequest;
import com.agileflow.api.service.ProjectService;
import com.agileflow.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orgs/{orgSlug}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> listProjects(
            @PathVariable String orgSlug,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.listProjects(orgSlug, pageable));
    }

    @GetMapping("/{projectKey}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable String orgSlug,
            @PathVariable String projectKey) {
        return ResponseEntity.ok(projectService.getProject(orgSlug, projectKey));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable String orgSlug,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(orgSlug, request, principal.getId()));
    }

    @PutMapping("/{projectKey}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String orgSlug,
            @PathVariable String projectKey,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.updateProject(orgSlug, projectKey, request, principal.getId()));
    }

    @DeleteMapping("/{projectKey}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String orgSlug,
            @PathVariable String projectKey,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(orgSlug, projectKey, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
