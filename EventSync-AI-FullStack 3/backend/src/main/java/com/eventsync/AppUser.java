package com.eventsync;
import jakarta.persistence.*;
@Entity @Table(name="app_users", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false, unique=true) public String email;
 @Column(nullable=false) public String passwordHash;
 @Column(nullable=false) public String role="VOLUNTEER";
 public Long volunteerId;
 @Column(nullable=false) public int tokenVersion = 0;
 public AppUser() {}
}
