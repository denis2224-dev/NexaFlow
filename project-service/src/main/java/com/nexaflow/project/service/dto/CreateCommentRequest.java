package com.nexaflow.project.service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(@NotBlank String content) {}
