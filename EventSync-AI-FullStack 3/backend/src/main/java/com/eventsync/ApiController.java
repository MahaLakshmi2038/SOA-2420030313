package com.eventsync;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ApiController {
    private final EventRepository events;
    private final VolunteerRepository volunteers;
    private final TaskRepository tasks;
    private final AssignmentRepository assignments;
    private final NotificationRepository notifications;

    public ApiController(EventRepository events, VolunteerRepository volunteers, TaskRepository tasks,
                         AssignmentRepository assignments, NotificationRepository notifications) {
        this.events = events; this.volunteers = volunteers; this.tasks = tasks;
        this.assignments = assignments; this.notifications = notifications;
    }

    @GetMapping("/health")
    public Map<String,Object> health() { return Map.of("status","UP","application","EventSync AI API"); }

    @GetMapping("/events") public List<Event> allEvents() { return events.findAll(); }
    @PostMapping("/events") public Event createEvent(@RequestBody Event e) {
        if (e.name == null || e.name.isBlank()) throw bad("Event name is required");
        e.id = null; if (e.status == null) e.status = "PLANNED";
        return events.save(e);
    }
    @PutMapping("/events/{id}") public Event updateEvent(@PathVariable Long id, @RequestBody Event body) {
        Event e = events.findById(id).orElseThrow(() -> missing("Event"));
        e.name = body.name; e.location = body.location; e.date = body.date;
        e.description = body.description; e.status = body.status == null ? e.status : body.status;
        return events.save(e);
    }
    @DeleteMapping("/events/{id}") public Map<String,String> deleteEvent(@PathVariable Long id) {
        if (!events.existsById(id)) throw missing("Event");
        events.deleteById(id); return Map.of("message","Event deleted");
    }

    @GetMapping("/volunteers") public List<Volunteer> allVolunteers() { return volunteers.findAll(); }
    @PostMapping("/volunteers") public Volunteer createVolunteer(@RequestBody Volunteer v) {
        if (v.name == null || v.name.isBlank()) throw bad("Volunteer name is required");
        if (v.email == null || !v.email.contains("@")) throw bad("A valid email is required");
        v.id = null; return volunteers.save(v);
    }
    @PutMapping("/volunteers/{id}") public Volunteer updateVolunteer(@PathVariable Long id, @RequestBody Volunteer body) {
        Volunteer v = volunteers.findById(id).orElseThrow(() -> missing("Volunteer"));
        v.name=body.name; v.email=body.email; v.skills=body.skills; v.experience=body.experience;
        v.maxWorkload=Math.max(1, body.maxWorkload);
        boolean wasAvailable = v.available;
        v.available=body.available;
        Volunteer saved=volunteers.save(v);
        if (wasAvailable && !saved.available) reassignVolunteer(saved.id);
        return saved;
    }
    @PostMapping("/volunteers/{id}/availability")
    public Volunteer setAvailability(@PathVariable Long id, @RequestBody Map<String,Boolean> body) {
        Volunteer v=volunteers.findById(id).orElseThrow(() -> missing("Volunteer"));
        boolean wasAvailable=v.available;
        v.available=Boolean.TRUE.equals(body.get("available"));
        Volunteer saved=volunteers.save(v);
        if (wasAvailable && !saved.available) reassignVolunteer(saved.id);
        return saved;
    }
    @DeleteMapping("/volunteers/{id}") public Map<String,String> deleteVolunteer(@PathVariable Long id) {
        if (!volunteers.existsById(id)) throw missing("Volunteer");
        if (!assignments.findByVolunteerIdAndStatus(id,"ASSIGNED").isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reassign this volunteer's active tasks before deleting");
        volunteers.deleteById(id); return Map.of("message","Volunteer deleted");
    }

    @GetMapping("/tasks") public List<EventTask> allTasks() { return tasks.findAll(); }
    @PostMapping("/tasks") public EventTask createTask(@RequestBody EventTask t) {
        if (t.title == null || t.title.isBlank()) throw bad("Task title is required");
        if (t.eventId != null && !events.existsById(t.eventId)) throw bad("Selected event does not exist");
        t.id=null; t.workload=Math.max(1, Math.min(10,t.workload)); t.status="OPEN";
        return tasks.save(t);
    }
    @PutMapping("/tasks/{id}") public EventTask updateTask(@PathVariable Long id, @RequestBody EventTask body) {
        EventTask t=tasks.findById(id).orElseThrow(() -> missing("Task"));
        t.title=body.title; t.eventId=body.eventId; t.description=body.description;
        t.requiredSkills=body.requiredSkills; t.workload=Math.max(1,Math.min(10,body.workload));
        t.status=body.status == null ? t.status : body.status;
        return tasks.save(t);
    }
    @DeleteMapping("/tasks/{id}") public Map<String,String> deleteTask(@PathVariable Long id) {
        if (!tasks.existsById(id)) throw missing("Task");
        for (Assignment a : assignments.findByTaskIdAndStatus(id,"ASSIGNED")) { a.status="CANCELLED"; assignments.save(a); }
        tasks.deleteById(id); return Map.of("message","Task deleted");
    }

    @GetMapping("/assignments") public List<Map<String,Object>> allAssignments() {
        return assignments.findAll().stream().map(this::assignmentView).collect(Collectors.toList());
    }

    @PostMapping("/agent/auto-assign")
    @Transactional
    public Map<String,Object> autoAssign(@RequestParam(required=false) Long eventId) {
        List<EventTask> taskList = tasks.findAll().stream()
            .filter(t -> eventId == null || Objects.equals(t.eventId,eventId))
            .filter(t -> "OPEN".equalsIgnoreCase(t.status))
            .sorted(Comparator.comparingInt((EventTask t) -> t.workload).reversed()).toList();
        int created=0;
        List<String> skipped=new ArrayList<>();
        for (EventTask task : taskList) {
            List<Assignment> existing=assignments.findByTaskIdAndStatus(task.id,"ASSIGNED");
            if (!existing.isEmpty()) continue;
            Volunteer best=chooseVolunteer(task, null);
            if (best == null) { skipped.add(task.title); continue; }
            createAssignment(task,best);
            created++;
        }
        return Map.of("assignedCount",created,"skippedTasks",skipped,
                      "message", created + " task(s) assigned; " + skipped.size() + " task(s) need attention.");
    }

    @PostMapping("/assignments/{id}/complete")
    public Map<String,Object> completeAssignment(@PathVariable Long id) {
        Assignment a=assignments.findById(id).orElseThrow(() -> missing("Assignment"));
        a.status="COMPLETED"; assignments.save(a);
        EventTask t=tasks.findById(a.taskId).orElse(null);
        if (t != null) { t.status="COMPLETED"; tasks.save(t); }
        notify("Task completed: " + (t == null ? a.taskId : t.title), "SUCCESS");
        return assignmentView(a);
    }

    @GetMapping("/notifications") public List<Notification> allNotifications() {
        List<Notification> list=notifications.findAll();
        list.sort(Comparator.comparing((Notification n) -> n.createdAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }
    @PostMapping("/notifications/{id}/read") public Notification markRead(@PathVariable Long id) {
        Notification n=notifications.findById(id).orElseThrow(() -> missing("Notification"));
        n.readFlag=true; return notifications.save(n);
    }

    @GetMapping("/dashboard") public Map<String,Object> dashboard() {
        long active=assignments.findByStatus("ASSIGNED").size();
        long open=tasks.findAll().stream().filter(t -> "OPEN".equalsIgnoreCase(t.status)
            && assignments.findByTaskIdAndStatus(t.id,"ASSIGNED").isEmpty()).count();
        long available=volunteers.findAll().stream().filter(v -> v.available).count();
        long unread=notifications.findAll().stream().filter(n -> !n.readFlag).count();
        Map<String,Integer> workload=new LinkedHashMap<>();
        for (Volunteer v: volunteers.findAll()) workload.put(v.name,
            assignments.findByVolunteerIdAndStatus(v.id,"ASSIGNED").stream().mapToInt(a -> {
                EventTask t=tasks.findById(a.taskId).orElse(null); return t == null ? 1 : t.workload;
            }).sum());
        return Map.of("events",events.count(),"volunteers",volunteers.count(),"availableVolunteers",available,
            "tasks",tasks.count(),"activeAssignments",active,"unassignedTasks",open,"unreadNotifications",unread,
            "workload",workload);
    }

    @PostMapping("/demo/reset")
    @Transactional
    public Map<String,String> resetDemo() {
        assignments.deleteAll(); notifications.deleteAll(); tasks.deleteAll(); volunteers.deleteAll(); events.deleteAll();
        Event e=new Event(); e.name="Community Clean-up Day"; e.location="KLH Bowrampet Campus";
        e.description="A campus and community clean-up event."; e.status="PLANNED"; events.save(e);
        Volunteer v1=vol("Ananya Rao","ananya@example.com","first aid, coordination, communication","INTERMEDIATE",true,3);
        Volunteer v2=vol("Nageshwari Bingi","nageshwari@example.com","registration, communication, data entry","ADVANCED",true,4);
        Volunteer v3=vol("Sri Maha Lakshmi","maha@example.com","logistics, setup, coordination","INTERMEDIATE",true,3);
        Volunteer v4=vol("Kiran Kumar","kiran@example.com","first aid, logistics","BEGINNER",true,2);
        volunteers.saveAll(List.of(v1,v2,v3,v4));
        EventTask t1=task(e.id,"Volunteer Registration","registration, communication",2);
        EventTask t2=task(e.id,"Safety Desk","first aid",2);
        EventTask t3=task(e.id,"Setup and Logistics","logistics, setup",3);
        EventTask t4=task(e.id,"Participant Coordination","coordination",1);
        tasks.saveAll(List.of(t1,t2,t3,t4));
        notify("Welcome to EventSync AI. Demo data is ready.", "INFO");
        return Map.of("message","Demo data reset successfully");
    }

    private Volunteer vol(String name,String email,String skills,String exp,boolean available,int max) {
        Volunteer v=new Volunteer(); v.name=name; v.email=email; v.skills=skills; v.experience=exp; v.available=available; v.maxWorkload=max; return v;
    }
    private EventTask task(Long eventId,String title,String skills,int workload) {
        EventTask t=new EventTask(); t.eventId=eventId; t.title=title; t.requiredSkills=skills; t.workload=workload; t.description=""; return t;
    }
    private Volunteer chooseVolunteer(EventTask task, Long excludedId) {
        List<Volunteer> pool=volunteers.findAll().stream().filter(v -> v.available)
            .filter(v -> excludedId == null || !Objects.equals(v.id,excludedId)).toList();
        return pool.stream().filter(v -> {
            int load=currentLoad(v.id);
            return load + task.workload <= Math.max(v.maxWorkload, task.workload);
        }).max(Comparator.comparingInt((Volunteer v) -> matchScore(v,task))
            .thenComparingInt(v -> -currentLoad(v.id))).orElseGet(() ->
                pool.stream().max(Comparator.comparingInt((Volunteer v) -> matchScore(v,task))
                    .thenComparingInt(v -> -currentLoad(v.id))).orElse(null));
    }
    private int matchScore(Volunteer v, EventTask t) {
        Set<String> volunteerSkills=split(v.skills), required=split(t.requiredSkills);
        int matches=0; for (String s: required) if (volunteerSkills.contains(s)) matches++;
        int experience=switch(String.valueOf(v.experience).toUpperCase()) {
            case "ADVANCED" -> 3; case "INTERMEDIATE" -> 2; default -> 1;
        };
        return matches*100 + experience*5 - currentLoad(v.id)*10;
    }
    private Set<String> split(String s) {
        if (s==null || s.isBlank()) return Set.of();
        return Arrays.stream(s.toLowerCase().split("[,;]")).map(String::trim).filter(x -> !x.isBlank()).collect(Collectors.toSet());
    }
    private int currentLoad(Long volunteerId) {
        return assignments.findByVolunteerIdAndStatus(volunteerId,"ASSIGNED").stream().mapToInt(a ->
            tasks.findById(a.taskId).map(t -> t.workload).orElse(1)).sum();
    }
    private void createAssignment(EventTask task, Volunteer v) {
        Assignment a=new Assignment(); a.eventId=task.eventId; a.taskId=task.id; a.volunteerId=v.id;
        a.status="ASSIGNED"; assignments.save(a); task.status="ASSIGNED"; tasks.save(task);
        notify("Assigned \""+task.title+"\" to "+v.name+".", "ASSIGNMENT");
    }
    private void reassignVolunteer(Long volunteerId) {
        List<Assignment> active=assignments.findByVolunteerIdAndStatus(volunteerId,"ASSIGNED");
        Volunteer old=volunteers.findById(volunteerId).orElse(null);
        for (Assignment a: active) {
            EventTask task=tasks.findById(a.taskId).orElse(null);
            a.status="REASSIGNED"; assignments.save(a);
            if (task == null) continue;
            Volunteer replacement=chooseVolunteer(task,volunteerId);
            if (replacement != null) {
                createAssignment(task,replacement);
                notify("Reassigned \""+task.title+"\" from "+(old == null ? "unavailable volunteer" : old.name)
                    +" to "+replacement.name+".", "REASSIGNMENT");
            } else {
                task.status="OPEN"; tasks.save(task);
                notify("No suitable replacement found for \""+task.title+"\". Coordinator action required.", "WARNING");
            }
        }
    }
    private Map<String,Object> assignmentView(Assignment a) {
        Map<String,Object> m=new LinkedHashMap<>(); m.put("id",a.id); m.put("eventId",a.eventId); m.put("taskId",a.taskId);
        m.put("volunteerId",a.volunteerId); m.put("status",a.status); m.put("assignedAt",a.assignedAt);
        m.put("task",tasks.findById(a.taskId).map(t -> t.title).orElse("Deleted task"));
        m.put("volunteer",volunteers.findById(a.volunteerId).map(v -> v.name).orElse("Deleted volunteer"));
        return m;
    }
    private void notify(String message,String type) {
        Notification n=new Notification(); n.message=message; n.type=type; notifications.save(n);
    }
    private ResponseStatusException bad(String msg) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,msg); }
    private ResponseStatusException missing(String what) { return new ResponseStatusException(HttpStatus.NOT_FOUND,what+" not found"); }
}
