package com.marian.owncloudbackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marian.owncloudbackend.dto.NotificationDTO;
import com.marian.owncloudbackend.dto.UserStorageRequest;
import com.marian.owncloudbackend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/admin")
    public ResponseEntity<String> notifyAdmin(@RequestBody UserStorageRequest userStorageRequest) {
        notificationService.notifyAdminForUserRequest(userStorageRequest);

        return ResponseEntity.ok("SENT");
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        List<NotificationDTO> allNotificationsOrdered = notificationService.getAllNotificationsOrdered();
        return ResponseEntity.ok(allNotificationsOrdered);
    }

    @GetMapping("/all/update")
    public ResponseEntity<String> updateNotificationsState() {
        notificationService.updateNotificationsState();
        return ResponseEntity.ok("");
    }

}
