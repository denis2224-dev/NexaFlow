package com.nexaflow.notification.service.mapper;

import com.nexaflow.notification.domain.Notification;
import com.nexaflow.notification.service.dto.NotificationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Notification} and its DTO {@link NotificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {}
