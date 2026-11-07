package com.eventsync;
import org.springframework.http.HttpStatus; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController @RequestMapping("/api/auth") @CrossOrigin(origins={"http://localhost:5173","http://127.0.0.1:5173"})
public class AuthController {
 private final AppUserRepository users; private final VolunteerRepository volunteers; private final PasswordEncoder encoder; private final TokenService tokens;
 public AuthController(AppUserRepository users,VolunteerRepository volunteers,PasswordEncoder encoder,TokenService tokens){this.users=users;this.volunteers=volunteers;this.encoder=encoder;this.tokens=tokens;}
 record RegisterRequest(String name,String email,String password,String skills,String experience){} record LoginRequest(String email,String password){} record PasswordChangeRequest(String currentPassword,String newPassword){}
 @PostMapping("/register") @Transactional public Map<String,Object> register(@RequestBody RegisterRequest r){
 if(r.name()==null||r.name().isBlank()||r.email()==null||!r.email().matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$")||r.email().length()>254||r.password()==null||r.password().length()<8) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a name, valid email, and password with at least 8 characters.");
 if(users.existsByEmailIgnoreCase(r.email().trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists.");
 Volunteer v=new Volunteer(); v.name=r.name().trim(); v.email=r.email().trim().toLowerCase(); v.skills=r.skills()==null?"":r.skills(); v.experience=r.experience()==null?"BEGINNER":r.experience(); v.available=true; v.maxWorkload=3; v=volunteers.save(v);
 AppUser u=new AppUser(); u.name=v.name; u.email=v.email; u.passwordHash=encoder.encode(r.password()); u.role="VOLUNTEER"; u.volunteerId=v.id; users.save(u); return session(u); }
 @PostMapping("/login") public Map<String,Object> login(@RequestBody LoginRequest r){ AppUser u=users.findByEmailIgnoreCase(r.email()==null?"":r.email().trim()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password.")); if(r.password()==null||!encoder.matches(r.password(),u.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password."); return session(u); }
 @PostMapping("/password") @Transactional public Map<String,String> changePassword(Authentication auth,@RequestBody PasswordChangeRequest r){
  AppUser u=current(auth);
  if(r.currentPassword()==null || !encoder.matches(r.currentPassword(),u.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Current password is incorrect.");
  if(r.newPassword()==null || r.newPassword().length()<14 || r.newPassword().length()>128) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New password must be between 14 and 128 characters.");
  if(encoder.matches(r.newPassword(),u.passwordHash)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a password different from the current password.");
  u.passwordHash=encoder.encode(r.newPassword()); u.tokenVersion++; users.save(u); return Map.of("message","Password updated. Existing sessions have been invalidated; sign in again.");
 }
 @GetMapping("/me") public Map<String,Object> me(Authentication auth){AppUser u=current(auth); return publicUser(u);}
 @GetMapping("/users") public List<Map<String,Object>> listUsers(){return users.findAll().stream().map(this::publicUser).toList();}
 @PostMapping("/users") public Map<String,Object> createUser(@RequestBody Map<String,String> r){String name=r.get("name"),email=r.get("email"),password=r.get("password"),role=r.get("role"); if(name==null||name.isBlank()||email==null||!email.matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$")||email.length()>254||password==null||password.length()<8)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter name, valid email, and password of at least 8 characters."); if(!Set.of("ADMIN","ORGANIZER","TEAM_LEAD").contains(role))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Role must be ADMIN, ORGANIZER, or TEAM_LEAD."); if(users.existsByEmailIgnoreCase(email))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists."); AppUser u=new AppUser();u.name=name.trim();u.email=email.trim().toLowerCase();u.passwordHash=encoder.encode(password);u.role=role;users.save(u);return publicUser(u);}
 private AppUser current(Authentication a){return users.findByEmailIgnoreCase(a.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 private Map<String,Object> session(AppUser u){Map<String,Object> m=publicUser(u);m.put("token",tokens.issue(u));return m;} private Map<String,Object> publicUser(AppUser u){Map<String,Object> m=new LinkedHashMap<>();m.put("id",u.id);m.put("name",u.name);m.put("email",u.email);m.put("role",u.role);m.put("volunteerId",u.volunteerId);return m;}
}
