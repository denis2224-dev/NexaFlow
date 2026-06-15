package com.nexaflow.notification.web.rest;

import com.nexaflow.notification.service.NotificationService;
import com.nexaflow.notification.service.dto.CreateNotificationRequest;
import com.nexaflow.notification.service.dto.NotificationDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/notifications")
public class InternalNotificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(InternalNotificationResource.class);

    private final NotificationService notificationService;

    public InternalNotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("")
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody CreateNotificationRequest request)
        throws URISyntaxException {
        LOG.debug("REST request to create internal Notification : {}", request);
        NotificationDTO notificationDTO = notificationService.create(request);
        return ResponseEntity.created(new URI("/api/notifications/" + notificationDTO.getId())).body(notificationDTO);
    }
}
