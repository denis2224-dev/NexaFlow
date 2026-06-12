package com.nexaflow.project.service.dto;

import com.nexaflow.project.domain.enumeration.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(@NotNull TaskStatus status) {}
