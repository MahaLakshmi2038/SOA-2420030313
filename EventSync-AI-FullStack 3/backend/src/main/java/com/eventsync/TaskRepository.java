package com.eventsync;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TaskRepository extends JpaRepository<EventTask, Long> { List<EventTask> findByEventId(Long eventId); }
