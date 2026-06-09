package com.nexaflow.project.service;

import com.nexaflow.project.domain.Project;
import com.nexaflow.project.repository.ProjectRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.service.dto.ProjectDTO;
import com.nexaflow.project.service.mapper.ProjectMapper;
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

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, OrganizationAccessService organizationAccessService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.organizationAccessService = organizationAccessService;
    }

    /**
     * Save a project.
     *
     * @param projectDTO the entity to save.
     * @return the persisted entity.
     */
    public ProjectDTO save(ProjectDTO projectDTO) {
        LOG.debug("Request to save Project : {}", projectDTO);

        organizationAccessService.assertMember(projectDTO.getOrganizationId());

        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    /**
     * Update a project.
     *
     * @param projectDTO the entity to save.
     * @return the persisted entity.
     */
    public ProjectDTO update(ProjectDTO projectDTO) {
        LOG.debug("Request to update Project : {}", projectDTO);

        Project existingProject = projectRepository
            .findById(projectDTO.getId())
            .orElseThrow(() -> new AccessDeniedException("Project not found"));

        Long organizationId = existingProject.getOrganizationId();
        organizationAccessService.assertMember(organizationId);

        projectDTO.setOrganizationId(organizationId);

        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    /**
     * Partially update a project.
     *
     * @param projectDTO the entity to update partially.
     * @return the persisted entity.
     */
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

    /**
     * Get all the projects.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Projects");
        return projectRepository.findAll(pageable).map(projectMapper::toDto);
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
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

    /**
     * Delete the project by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Project : {}", id);

        Project existingProject = projectRepository
            .findById(id)
            .orElseThrow(() -> new AccessDeniedException("Project not found"));

        organizationAccessService.assertMember(existingProject.getOrganizationId());

        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAllByOrganization(Long organizationId, Pageable pageable) {
        LOG.debug("Request to get Projects for organization : {}", organizationId);

        organizationAccessService.assertMember(organizationId);

        return projectRepository.findByOrganizationId(organizationId, pageable).map(projectMapper::toDto);
    }
}
