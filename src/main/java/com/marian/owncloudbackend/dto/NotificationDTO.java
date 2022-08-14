package com.marian.owncloudbackend.dto;

import java.time.LocalDateTime;

import com.marian.owncloudbackend.enums.NotificationState;

public record NotificationDTO(Long id, String description, LocalDateTime dateTime, NotificationState notificationState) {
}
