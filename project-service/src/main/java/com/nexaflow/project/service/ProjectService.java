package com.nexaflow.project.service;

import com.nexaflow.project.domain.Project;
import com.nexaflow.project.domain.enumeration.ActivityAction;
import com.nexaflow.project.domain.enumeration.ActivityEntityType;
import com.nexaflow.project.domain.enumeration.ProjectStatus;
import com.nexaflow.project.repository.ProjectRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.security.SecurityUtils;
import com.nexaflow.project.service.dto.CreateProjectRequest;
import com.nexaflow.project.service.dto.ProjectDTO;
import com.nexaflow.project.service.dto.UpdateProjectRequest;
import com.nexaflow.project.service.mapper.ProjectMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.nexaflow.project.domain.Project}.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final OrganizationAccessService organizationAccessService;
    private final ActivityLogService activityLogService;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMapper projectMapper,
        OrganizationAccessService organizationAccessService,
        ActivityLogService activityLogService
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.organizationAccessService = organizationAccessService;
        this.activityLogService = activityLogService;
    }

    public ProjectDTO create(Long organizationId, CreateProjectRequest request) {
        LOG.debug("Request to create Project in organization {} : {}", organizationId, request);
        organizationAccessService.assertMember(organizationId);

        Project project = new Project();
        project.setOrganizationId(organizationId);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());
        project.setCreatedBy(SecurityUtils.getCurrentUserLogin().orElse("system"));
        project.setCreatedAt(Instant.now());

        project = projectRepository.save(project);
        activityLogService.record(organizationId, ActivityEntityType.PROJECT, project.getId(), ActivityAction.PROJECT_CREATED);
        return projectMapper.toDto(project);
    }

    public ProjectDTO save(ProjectDTO projectDTO) {
        LOG.debug("Request to save Project : {}", projectDTO);

        organizationAccessService.assertMember(projectDTO.getOrganizationId());

        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    public ProjectDTO update(Long id, UpdateProjectRequest request) {
        LOG.debug("Request to update Project : {}, {}", id, request);
        Project project = getExistingProject(id);
        organizationAccessService.assertMember(project.getOrganizationId());

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());
        project.setUpdatedAt(Instant.now());

        project = projectRepository.save(project);
        activityLogService.record(project.getOrganizationId(), ActivityEntityType.PROJECT, project.getId(), ActivityAction.PROJECT_UPDATED);
        return projectMapper.toDto(project);
    }

    public ProjectDTO update(ProjectDTO projectDTO) {
        LOG.debug("Request to update Project : {}", projectDTO);

        Project existingProject = getExistingProject(projectDTO.getId());
        Long organizationId = existingProject.getOrganizationId();
        organizationAccessService.assertMember(organizationId);

        projectDTO.setOrganizationId(organizationId);

        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    public Optional<ProjectDTO> partialUpdate(ProjectDTO projectDTO) {
        LOG.debug("Request to partially update Project : {}", projectDTO);

        return projectRepository
            .findById(projectDTO.getId())
            .map(existingProject -> {
                Long organizationId = existingProject.getOrganizationId();
                organizationAccessService.assertMember(organizationId);

                projectDTO.setOrganizationId(organizationId);
                projectMapper.partialUpdate(existingProject, projectDTO);
                existingProject.setOrganizationId(organizationId);

                return existingProject;
            })
            .map(projectRepository::save)
            .map(projectMapper::toDto);
    }

    public ProjectDTO archive(Long id) {
        Project project = changeStatus(id, ProjectStatus.ARCHIVED, ActivityAction.PROJECT_ARCHIVED);
        return projectMapper.toDto(project);
    }

    public ProjectDTO complete(Long id) {
        Project project = changeStatus(id, ProjectStatus.COMPLETED, ActivityAction.PROJECT_COMPLETED);
        return projectMapper.toDto(project);
    }

    private Project changeStatus(Long id, ProjectStatus status, ActivityAction action) {
        Project project = getExistingProject(id);
        organizationAccessService.assertMember(project.getOrganizationId());
        project.setStatus(status);
        project.setUpdatedAt(Instant.now());
        project = projectRepository.save(project);
        activityLogService.record(project.getOrganizationId(), ActivityEntityType.PROJECT, project.getId(), action);
        return project;
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Projects");
        return projectRepository.findAll(pageable).map(projectMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOne(Long id) {
        LOG.debug("Request to get Project : {}", id);

        return projectRepository
            .findById(id)
            .map(project -> {
                organizationAccessService.assertMember(project.getOrganizationId());
                return projectMapper.toDto(project);
            });
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Project : {}", id);

        Project existingProject = getExistingProject(id);
        organizationAccessService.assertMember(existingProject.getOrganizationId());

        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAllByOrganization(Long organizationId, Pageable pageable) {
        LOG.debug("Request to get Projects for organization : {}", organizationId);

        organizationAccessService.assertMember(organizationId);

        return projectRepository.findByOrganizationId(organizationId, pageable).map(projectMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Project getExistingProject(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new AccessDeniedException("Project not found"));
    }
}
