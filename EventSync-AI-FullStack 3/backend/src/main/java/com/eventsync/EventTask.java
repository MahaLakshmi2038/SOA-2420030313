package com.eventsync;
import jakarta.persistence.*;
@Entity @Table(name="tasks")
public class EventTask {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String title;
 public Long eventId;
 @Column(length=1000) public String description;
 @Column(length=1000) public String requiredSkills = "";
 public int workload = 1;
 public String status = "OPEN";
 public EventTask() {}
}
