package com.nexaflow.userservice.web.rest;

import com.nexaflow.userservice.service.WorkspaceService;
import com.nexaflow.userservice.service.dto.CreateWorkspaceRequest;
import com.nexaflow.userservice.service.dto.WorkspaceDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceResource {

    private final WorkspaceService workspaceService;

    public WorkspaceResource(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceDTO> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) throws URISyntaxException {
        WorkspaceDTO result = workspaceService.createWorkspace(request);
        return ResponseEntity.created(new URI("/api/workspaces/" + result.getOrganizationId())).body(result);
    }
}
