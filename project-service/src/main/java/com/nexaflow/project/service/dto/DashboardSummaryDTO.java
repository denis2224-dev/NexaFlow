package com.nexaflow.project.service.dto;

public record DashboardSummaryDTO(
    long totalProjects,
    long activeProjects,
    long completedProjects,
    long totalTasks,
    long completedTasks,
    long overdueTasks
) {}
