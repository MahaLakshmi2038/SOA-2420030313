
package com.eventsync.volunteer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventsync.volunteer_service.entity.Volunteer;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
}
