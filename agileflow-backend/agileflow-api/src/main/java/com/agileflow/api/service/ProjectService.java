package com.agileflow.api.service;

import com.agileflow.api.dto.CreateProjectRequest;
import com.agileflow.api.dto.ProjectResponse;
import com.agileflow.api.dto.UpdateProjectRequest;
import com.agileflow.core.domain.Organization;
import com.agileflow.core.domain.Project;
import com.agileflow.core.domain.User;
import com.agileflow.core.domain.enums.OrgRole;
import com.agileflow.infrastructure.repository.ProjectRepository;
import com.agileflow.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;

    public Page<ProjectResponse> listProjects(String orgSlug, Pageable pageable) {
        Organization org = organizationService.findBySlug(orgSlug);
        return projectRepository.findByOrganizationId(org.getId(), pageable)
                .map(this::toResponse);
    }

    public ProjectResponse getProject(String orgSlug, String projectKey) {
        Organization org = organizationService.findBySlug(orgSlug);
        Project project = projectRepository.findByOrganizationIdAndKey(org.getId(), projectKey)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectKey));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(String orgSlug, CreateProjectRequest request, UUID currentUserId) {
        Organization org = organizationService.findBySlug(orgSlug);
        organizationService.requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN, OrgRole.MEMBER);

        // Check for duplicate key within org
        if (projectRepository.findByOrganizationIdAndKey(org.getId(), request.getKey()).isPresent()) {
            throw new IllegalStateException("A project with key '" + request.getKey() + "' already exists in this organization");
        }

        User lead = null;
        if (request.getLeadId() != null) {
            lead = userRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new EntityNotFoundException("Lead user not found"));
        }

        Project project = Project.builder()
                .organization(org)
                .name(request.getName())
                .key(request.getKey())
                .description(request.getDescription())
                .lead(lead)
                .build();

        project = projectRepository.save(project);
        log.info("Project '{}' ({}) created in org '{}' by user {}", request.getName(), request.getKey(), orgSlug, currentUserId);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(String orgSlug, String projectKey, UpdateProjectRequest request, UUID currentUserId) {
        Organization org = organizationService.findBySlug(orgSlug);
        organizationService.requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN, OrgRole.MEMBER);

        Project project = projectRepository.findByOrganizationIdAndKey(org.getId(), projectKey)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectKey));

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getLeadId() != null) {
            User lead = userRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new EntityNotFoundException("Lead user not found"));
            project.setLead(lead);
        }

        project = projectRepository.save(project);
        log.info("Project '{}' updated in org '{}' by user {}", projectKey, orgSlug, currentUserId);
        return toResponse(project);
    }

    @Transactional
    public void deleteProject(String orgSlug, String projectKey, UUID currentUserId) {
        Organization org = organizationService.findBySlug(orgSlug);
        organizationService.requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN);

        Project project = projectRepository.findByOrganizationIdAndKey(org.getId(), projectKey)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectKey));

        projectRepository.delete(project);
        log.info("Project '{}' deleted from org '{}' by user {}", projectKey, orgSlug, currentUserId);
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .key(project.getKey())
                .description(project.getDescription())
                .leadName(project.getLead() != null ? project.getLead().getName() : null)
                .leadId(project.getLead() != null ? project.getLead().getId() : null)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
