package com.nexaflow.project.service;

import com.nexaflow.project.domain.enumeration.ProjectStatus;
import com.nexaflow.project.domain.enumeration.TaskStatus;
import com.nexaflow.project.repository.ProjectRepository;
import com.nexaflow.project.repository.TaskRepository;
import com.nexaflow.project.security.OrganizationAccessService;
import com.nexaflow.project.service.dto.DashboardSummaryDTO;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final OrganizationAccessService organizationAccessService;

    public DashboardService(
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        OrganizationAccessService organizationAccessService
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.organizationAccessService = organizationAccessService;
    }

    public DashboardSummaryDTO getSummary(Long organizationId) {
        organizationAccessService.assertMember(organizationId);

        return new DashboardSummaryDTO(
            projectRepository.countByOrganizationId(organizationId),
            projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.ACTIVE),
            projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.COMPLETED),
            taskRepository.countByOrganizationId(organizationId),
            taskRepository.countByOrganizationIdAndStatus(organizationId, TaskStatus.DONE),
            taskRepository.countByOrganizationIdAndDueDateBeforeAndStatusNot(organizationId, LocalDate.now(), TaskStatus.DONE)
        );
    }
}
