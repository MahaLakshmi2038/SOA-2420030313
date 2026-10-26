
package com.eventsync.notification_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventsync.notification_service.entity.Notification;
import com.eventsync.notification_service.service.NotificationService;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(notification));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(
                notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long id) {
        Optional<Notification> notification =
                notificationService.getNotificationById(id);

        return notification.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<List<Notification>> getNotificationsByVolunteer(
            @PathVariable Long volunteerId) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByVolunteer(volunteerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Notification> updateNotificationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Notification notification =
                notificationService.updateNotificationStatus(id, status);

        if (notification == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id) {
        if (notificationService.deleteNotification(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
