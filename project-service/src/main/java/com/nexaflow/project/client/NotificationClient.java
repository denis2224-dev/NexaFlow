package com.nexaflow.project.client;

import com.nexaflow.project.client.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/internal/notifications")
public interface NotificationClient {

    @PostMapping("")
    void createNotification(@RequestBody CreateNotificationRequest request);
}
