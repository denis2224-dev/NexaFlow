package com.nexaflow.project.web.rest;

import com.nexaflow.project.service.ActivityLogService;
import com.nexaflow.project.service.ProjectService;
import com.nexaflow.project.service.TaskService;
import com.nexaflow.project.service.dto.ActivityLogDTO;
import com.nexaflow.project.service.dto.CreateProjectRequest;
import com.nexaflow.project.service.dto.ProjectDTO;
import com.nexaflow.project.service.dto.TaskDTO;
import com.nexaflow.project.service.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/projects")
public class ProjectResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectResource.class);
    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    private final ProjectService projectService;
    private final TaskService taskService;
    private final ActivityLogService activityLogService;

    public ProjectResource(ProjectService projectService, TaskService taskService, ActivityLogService activityLogService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.activityLogService = activityLogService;
    }

    @PostMapping("")
    public ResponseEntity<ProjectDTO> createProject(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @Valid @RequestBody CreateProjectRequest request
    ) throws URISyntaxException {
        LOG.debug("REST request to create Project in organization {} : {}", organizationId, request);
        ProjectDTO result = projectService.create(organizationId, request);
        return ResponseEntity.created(new URI("/api/projects/" + result.getId())).body(result);
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDTO>> getAllProjects(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Projects for organization : {}", organizationId);
        Page<ProjectDTO> page = projectService.findAllByOrganization(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Project : {}", id);
        return ResponseUtil.wrapOrNotFound(projectService.findOne(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable("id") Long id, @Valid @RequestBody UpdateProjectRequest request) {
        LOG.debug("REST request to update Project : {}, {}", id, request);
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ProjectDTO> archiveProject(@PathVariable("id") Long id) {
        LOG.debug("REST request to archive Project : {}", id);
        return ResponseEntity.ok(projectService.archive(id));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<ProjectDTO> unarchiveProject(@PathVariable("id") Long id) {
        LOG.debug("REST request to unarchive Project : {}", id);
        return ResponseEntity.ok(projectService.unarchive(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ProjectDTO> completeProject(@PathVariable("id") Long id) {
        LOG.debug("REST request to complete Project : {}", id);
        return ResponseEntity.ok(projectService.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Project : {}", id);
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskDTO>> getProjectTasks(
        @PathVariable("id") Long id,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Tasks for Project : {}", id);
        Page<TaskDTO> page = taskService.findByProject(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}/activity-logs")
    public ResponseEntity<List<ActivityLogDTO>> getProjectActivityLogs(
        @PathVariable("id") Long id,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ActivityLogs for Project : {}", id);
        Long organizationId = projectService.getExistingProject(id).getOrganizationId();
        Page<ActivityLogDTO> page = activityLogService.findByProject(id, organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
