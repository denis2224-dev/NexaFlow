package com.nexaflow.project.web.rest;

import com.nexaflow.project.service.DashboardService;
import com.nexaflow.project.service.TaskService;
import com.nexaflow.project.service.dto.DashboardSummaryDTO;
import com.nexaflow.project.service.dto.TaskDTO;
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

@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);
    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    private final DashboardService dashboardService;
    private final TaskService taskService;

    public DashboardResource(DashboardService dashboardService, TaskService taskService) {
        this.dashboardService = dashboardService;
        this.taskService = taskService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@RequestHeader(ORGANIZATION_HEADER) Long organizationId) {
        LOG.debug("REST request to get dashboard summary for organization : {}", organizationId);
        return ResponseEntity.ok(dashboardService.getSummary(organizationId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskDTO>> getMyTasks(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<TaskDTO> page = taskService.findMyTasks(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/tasks-due-today")
    public ResponseEntity<List<TaskDTO>> getTasksDueToday(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<TaskDTO> page = taskService.findDueToday(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/overdue-tasks")
    public ResponseEntity<List<TaskDTO>> getOverdueTasks(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<TaskDTO> page = taskService.findOverdue(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
