
package com.eventsync.notification_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventsync.notification_service.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByVolunteerId(Long volunteerId);

    List<Notification> findByStatus(String status);
}
