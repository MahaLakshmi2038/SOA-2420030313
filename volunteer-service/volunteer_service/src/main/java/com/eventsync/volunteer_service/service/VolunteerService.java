
package com.eventsync.volunteer_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eventsync.volunteer_service.entity.Volunteer;
import com.eventsync.volunteer_service.repository.VolunteerRepository;

@Service
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    public Volunteer createVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }

    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }

    public Optional<Volunteer> getVolunteerById(Long id) {
        return volunteerRepository.findById(id);
    }

    public Volunteer updateVolunteer(Long id, Volunteer updatedVolunteer) {
        return volunteerRepository.findById(id)
                .map(volunteer -> {
                    volunteer.setName(updatedVolunteer.getName());
                    volunteer.setEmail(updatedVolunteer.getEmail());
                    volunteer.setSkills(updatedVolunteer.getSkills());
                    volunteer.setAvailable(updatedVolunteer.isAvailable());
                    volunteer.setExperienceYears(
                            updatedVolunteer.getExperienceYears());
                    return volunteerRepository.save(volunteer);
                })
                .orElse(null);
    }

    public boolean deleteVolunteer(Long id) {
        if (volunteerRepository.existsById(id)) {
            volunteerRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
