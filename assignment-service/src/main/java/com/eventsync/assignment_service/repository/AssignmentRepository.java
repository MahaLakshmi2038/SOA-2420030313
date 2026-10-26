package com.eventsync.assignment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventsync.assignment_service.entity.Assignment;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {
}