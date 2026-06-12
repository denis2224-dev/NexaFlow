package com.nexaflow.project.service.dto;

import com.nexaflow.project.domain.enumeration.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
    @NotBlank @Size(min = 2, max = 150) String title,
    String description,
    @NotNull TaskPriority priority
) {}
