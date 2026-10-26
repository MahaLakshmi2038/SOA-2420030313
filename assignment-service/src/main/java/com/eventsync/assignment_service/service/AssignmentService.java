
package com.eventsync.assignment_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.eventsync.assignment_service.entity.Assignment;
import com.eventsync.assignment_service.repository.AssignmentRepository;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final DiscoveryClient discoveryClient;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            DiscoveryClient discoveryClient) {
        this.assignmentRepository = assignmentRepository;
        this.discoveryClient = discoveryClient;
    }

    public Assignment createAssignment(Assignment assignment) {
        if (assignment.getStatus() == null
                || assignment.getStatus().isBlank()) {
            assignment.setStatus("PENDING");
        }

        Assignment savedAssignment =
                assignmentRepository.save(assignment);

        try {
            List<ServiceInstance> instances =
                    discoveryClient.getInstances("notification-service");

            if (instances.isEmpty()) {
                System.err.println(
                        "Warning: Assignment saved, but Notification "
                        + "Service was not found in Eureka.");
                return savedAssignment;
            }

            String message = "You have been assigned to event "
                    + savedAssignment.getEventId()
                    + ". Task: "
                    + savedAssignment.getTaskDescription();

            ServiceInstance notificationService = instances.get(0);

            RestClient.create()
                    .post()
                    .uri(notificationService.getUri()
                            .resolve("/notifications"))
                    .body(new NotificationRequest(
                            savedAssignment.getVolunteerId(),
                            savedAssignment.getEventId(),
                            message,
                            "ASSIGNMENT",
                            "PENDING"))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            System.err.println(
                    "Warning: Assignment was saved, but notification "
                    + "could not be created: " + e.getMessage());
        }

        return savedAssignment;
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public Optional<Assignment> getAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    public Assignment updateAssignment(
            Long id, Assignment updatedAssignment) {
        return assignmentRepository.findById(id)
                .map(assignment -> {
                    assignment.setEventId(updatedAssignment.getEventId());
                    assignment.setVolunteerId(
                            updatedAssignment.getVolunteerId());
                    assignment.setTaskDescription(
                            updatedAssignment.getTaskDescription());
                    assignment.setStatus(updatedAssignment.getStatus());

                    return assignmentRepository.save(assignment);
                })
                .orElse(null);
    }

    public boolean deleteAssignment(Long id) {
        if (assignmentRepository.existsById(id)) {
            assignmentRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private record NotificationRequest(
            Long volunteerId,
            Long eventId,
            String message,
            String type,
            String status) {
    }
}
