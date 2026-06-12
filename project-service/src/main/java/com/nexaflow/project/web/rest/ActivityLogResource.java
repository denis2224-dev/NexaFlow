package com.nexaflow.project.web.rest;

import com.nexaflow.project.service.ActivityLogService;
import com.nexaflow.project.service.dto.ActivityLogDTO;
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
@RequestMapping("/api/activity-logs")
public class ActivityLogResource {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityLogResource.class);
    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    private final ActivityLogService activityLogService;

    public ActivityLogResource(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("")
    public ResponseEntity<List<ActivityLogDTO>> getAllActivityLogs(
        @RequestHeader(ORGANIZATION_HEADER) Long organizationId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ActivityLogs for organization : {}", organizationId);
        Page<ActivityLogDTO> page = activityLogService.findAllByOrganization(organizationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityLogDTO> getActivityLog(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ActivityLog : {}", id);
        return ResponseUtil.wrapOrNotFound(activityLogService.findOne(id));
    }
}
