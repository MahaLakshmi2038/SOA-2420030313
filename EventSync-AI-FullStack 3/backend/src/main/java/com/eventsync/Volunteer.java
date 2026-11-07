package com.eventsync;
import jakarta.persistence.*;
@Entity @Table(name="volunteers")
public class Volunteer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false) public String email;
 @Column(length=1000) public String skills = "";
 public String experience = "BEGINNER";
 public boolean available = true;
 public int maxWorkload = 3;
 public Volunteer() {}
}
