package com.eventsync.event_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eventsync.event_service.entity.Event;
import com.eventsync.event_service.repository.EventRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event updateEvent(Long id, Event updatedEvent) {

        return eventRepository.findById(id)
                .map(event -> {
                    event.setName(updatedEvent.getName());
                    event.setDescription(updatedEvent.getDescription());
                    event.setLocation(updatedEvent.getLocation());
                    event.setDate(updatedEvent.getDate());
                    event.setTime(updatedEvent.getTime());
                    event.setRequiredVolunteers(updatedEvent.getRequiredVolunteers());
                    event.setStatus(updatedEvent.getStatus());

                    return eventRepository.save(event);
                })
                .orElse(null);
    }

    public boolean deleteEvent(Long id) {

        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }

        return false;
    }
}