package com.eventsync;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="notifications")
public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false, length=1000) public String message;
 public String type = "INFO";
 public LocalDateTime createdAt = LocalDateTime.now();
 public boolean readFlag = false;
 public Notification() {}
}
