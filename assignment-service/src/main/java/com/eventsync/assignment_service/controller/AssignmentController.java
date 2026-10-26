
package com.eventsync.assignment_service.controller;

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

import com.eventsync.assignment_service.entity.Assignment;
import com.eventsync.assignment_service.service.AssignmentService;

@RestController
@RequestMapping("/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @RequestBody Assignment assignment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.createAssignment(assignment));
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        return ResponseEntity.ok(
                assignmentService.getAllAssignments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(
            @PathVariable Long id) {
        Optional<Assignment> assignment =
                assignmentService.getAssignmentById(id);

        return assignment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment updatedAssignment) {
        Assignment assignment =
                assignmentService.updateAssignment(id, updatedAssignment);

        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id) {
        if (assignmentService.deleteAssignment(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
