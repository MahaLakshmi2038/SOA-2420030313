package com.eventsync;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="assignments")
public class Assignment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long eventId;
 public Long taskId;
 public Long volunteerId;
 public String status = "ASSIGNED";
 public LocalDateTime assignedAt = LocalDateTime.now();
 public Assignment() {}
}
