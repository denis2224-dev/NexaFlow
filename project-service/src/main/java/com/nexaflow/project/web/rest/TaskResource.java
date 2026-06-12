package com.nexaflow.project.web.rest;

import com.nexaflow.project.service.ActivityLogService;
import com.nexaflow.project.service.CommentService;
import com.nexaflow.project.service.TaskService;
import com.nexaflow.project.service.dto.ActivityLogDTO;
import com.nexaflow.project.service.dto.AssignTaskRequest;
import com.nexaflow.project.service.dto.ChangeTaskStatusRequest;
import com.nexaflow.project.service.dto.CommentDTO;
import com.nexaflow.project.service.dto.CreateCommentRequest;
import com.nexaflow.project.service.dto.CreateTaskRequest;
import com.nexaflow.project.service.dto.TaskDTO;
import com.nexaflow.project.service.dto.UpdateTaskRequest;
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
@RequestMapping("/api/tasks")
public class TaskResource {

    private static final Logger LOG = LoggerFactory.getLogger(TaskResource.class);
    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    private final TaskService taskService;
    private final CommentService commentService;
    private final ActivityLogService activityLogService;

    public TaskResource(TaskService taskService, CommentService commentService, ActivityLogService activityLogService) {
        this.taskService = taskService;
        this.commentService = commentService;
        this.activityLogService = activityLogService;
    }

    @PostMapping("")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) throws URISyntaxException {
        LOG.debug("REST request to create Task : {}", request);
        TaskDTO result = taskService.create(request);
        return ResponseEntity.created(new URI("/api/tasks/" + result.getId())).body(result);
    }

    @GetMapping("")
    public ResponseEntity<List<TaskDTO>> getAllTasks(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Tasks for organization : {}", organizationId);
        Page<TaskDTO> page = taskService.findAllByOrganization(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Task : {}", id);
        return ResponseUtil.wrapOrNotFound(taskService.findOne(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable("id") Long id, @Valid @RequestBody UpdateTaskRequest request) {
        LOG.debug("REST request to update Task : {}, {}", id, request);
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDTO> changeTaskStatus(@PathVariable("id") Long id, @Valid @RequestBody ChangeTaskStatusRequest request) {
        LOG.debug("REST request to change Task status : {}, {}", id, request);
        return ResponseEntity.ok(taskService.changeStatus(id, request));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TaskDTO> assignTask(@PathVariable("id") Long id, @Valid @RequestBody AssignTaskRequest request) {
        LOG.debug("REST request to assign Task : {}, {}", id, request);
        return ResponseEntity.ok(taskService.assign(id, request));
    }

    @PatchMapping("/{id}/unassign")
    public ResponseEntity<TaskDTO> unassignTask(@PathVariable("id") Long id) {
        LOG.debug("REST request to unassign Task : {}", id);
        return ResponseEntity.ok(taskService.unassign(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Task : {}", id);
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable("id") Long id, @Valid @RequestBody CreateCommentRequest request)
        throws URISyntaxException {
        LOG.debug("REST request to add Comment to Task : {}, {}", id, request);
        CommentDTO result = commentService.addToTask(id, request);
        return ResponseEntity.created(new URI("/api/comments/" + result.getId())).body(result);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getTaskComments(
        @PathVariable("id") Long id,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Comments for Task : {}", id);
        Page<CommentDTO> page = commentService.findByTask(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}/activity-logs")
    public ResponseEntity<List<ActivityLogDTO>> getTaskActivityLogs(
        @PathVariable("id") Long id,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ActivityLogs for Task : {}", id);
        Long organizationId = taskService.getExistingTask(id).getOrganizationId();
        Page<ActivityLogDTO> page = activityLogService.findByTask(id, organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
