package com.eventsync;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
 List<Assignment> findByStatus(String status);
 List<Assignment> findByVolunteerIdAndStatus(Long volunteerId, String status);
 List<Assignment> findByTaskIdAndStatus(Long taskId, String status);
 List<Assignment> findByEventId(Long eventId);
}
