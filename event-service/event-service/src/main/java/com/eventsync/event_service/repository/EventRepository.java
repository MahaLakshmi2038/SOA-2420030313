package com.eventsync.event_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventsync.event_service.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}