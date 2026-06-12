package com.nexaflow.project.service.dto;

import com.nexaflow.project.domain.enumeration.TaskPriority;
import com.nexaflow.project.domain.enumeration.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateTaskRequest(
    @NotNull Long projectId,
    @NotBlank @Size(min = 2, max = 150) String title,
    String description,
    @NotNull TaskPriority priority,
    @NotNull TaskStatus status,
    String assignedUserLogin,
    LocalDate dueDate
) {}
