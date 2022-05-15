package com.marian.owncloudbackend.DTO;

import java.time.LocalDateTime;

import com.marian.owncloudbackend.enums.NotificationState;

public record NotificationDTO(Long id, String description, LocalDateTime dateTime, NotificationState notificationState) {
}
