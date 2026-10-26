
package com.eventsync.notification_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eventsync.notification_service.entity.Notification;
import com.eventsync.notification_service.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification) {
        if (notification.getStatus() == null ||
                notification.getStatus().isBlank()) {
            notification.setStatus("UNREAD");
        }

        if (notification.getType() == null ||
                notification.getType().isBlank()) {
            notification.setType("GENERAL");
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> getNotificationsByVolunteer(Long volunteerId) {
        return notificationRepository.findByVolunteerId(volunteerId);
    }

    public List<Notification> getNotificationsByStatus(String status) {
        return notificationRepository.findByStatus(status);
    }

    public Notification updateNotificationStatus(Long id, String status) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setStatus(status);
                    return notificationRepository.save(notification);
                })
                .orElse(null);
    }

    public boolean deleteNotification(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }

        return false;
    }
}
