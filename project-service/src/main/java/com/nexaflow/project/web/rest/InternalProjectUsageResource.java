package com.nexaflow.project.web.rest;

import com.nexaflow.project.repository.ProjectRepository;
import com.nexaflow.project.repository.TaskRepository;
import com.nexaflow.project.service.dto.ProjectUsageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/projects")
public class InternalProjectUsageResource {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public InternalProjectUsageResource(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/usage")
    public ResponseEntity<ProjectUsageDTO> getUsage(@RequestParam Long organizationId) {
        return ResponseEntity.ok(
            new ProjectUsageDTO(
                organizationId,
                projectRepository.countByOrganizationId(organizationId),
                taskRepository.countByOrganizationId(organizationId)
            )
        );
    }
}
