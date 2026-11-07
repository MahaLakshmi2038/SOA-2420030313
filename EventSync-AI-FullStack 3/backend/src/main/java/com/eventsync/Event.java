package com.eventsync;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="events")
public class Event {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 public String location;
 public LocalDate date;
 @Column(length=1000) public String description;
 public String status = "PLANNED";
 public Event() {}
}
