package com.nexaflow.project.service.dto;

import com.nexaflow.project.domain.enumeration.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    String description,
    @NotNull ProjectStatus status
) {}
