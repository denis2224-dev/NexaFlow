package com.nexaflow.project.service.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTaskRequest(@NotBlank String assignedUserLogin) {}
