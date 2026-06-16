package com.nexaflow.project.client;

import com.nexaflow.project.client.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service", path = "/api/internal/notifications")
public interface NotificationClient {

    @PostMapping("")
    void createNotification(@RequestHeader("X-Internal-Token") String internalToken, @RequestBody CreateNotificationRequest request);
}
