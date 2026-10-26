
package com.eventsync.volunteer_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventsync.volunteer_service.entity.Volunteer;
import com.eventsync.volunteer_service.service.VolunteerService;

@RestController
@RequestMapping("/volunteers")
@CrossOrigin(origins = "*")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping
    public ResponseEntity<Volunteer> createVolunteer(
            @RequestBody Volunteer volunteer) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(volunteerService.createVolunteer(volunteer));
    }

    @GetMapping
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        return ResponseEntity.ok(volunteerService.getAllVolunteers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Volunteer> getVolunteerById(
            @PathVariable Long id) {
        Optional<Volunteer> volunteer =
                volunteerService.getVolunteerById(id);

        return volunteer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Volunteer> updateVolunteer(
            @PathVariable Long id,
            @RequestBody Volunteer updatedVolunteer) {
        Volunteer volunteer =
                volunteerService.updateVolunteer(id, updatedVolunteer);

        if (volunteer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(volunteer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable Long id) {
        if (volunteerService.deleteVolunteer(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
