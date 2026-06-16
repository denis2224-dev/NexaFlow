package com.nexaflow.notification.web.rest;

import com.nexaflow.notification.service.NotificationService;
import com.nexaflow.notification.service.dto.CreateNotificationRequest;
import com.nexaflow.notification.service.dto.NotificationDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/internal/notifications")
public class InternalNotificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(InternalNotificationResource.class);
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Value("${application.internal-api-token:}")
    private String internalApiToken;

    private final NotificationService notificationService;

    public InternalNotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> createNotification(
        @RequestHeader(value = INTERNAL_TOKEN_HEADER, required = false) String internalToken,
        @Valid @RequestBody CreateNotificationRequest request
    ) throws URISyntaxException {
        LOG.debug("REST request to create internal Notification : {}", request);
        if (internalApiToken.isBlank() || !Objects.equals(internalApiToken, internalToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal token");
        }
        NotificationDTO notificationDTO = notificationService.createInternalNotification(request);
        return ResponseEntity.created(new URI("/api/notifications/" + notificationDTO.getId())).body(notificationDTO);
    }
}
