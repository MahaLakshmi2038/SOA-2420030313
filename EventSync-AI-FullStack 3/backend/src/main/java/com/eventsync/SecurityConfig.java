package com.eventsync;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthFilter jwt)throws Exception {
 http.csrf(csrf->csrf.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 .authorizeHttpRequests(a->a.requestMatchers("/api/auth/users").hasRole("ADMIN")
 .requestMatchers("/api/auth/password").hasAnyRole("ADMIN","ORGANIZER","TEAM_LEAD","VOLUNTEER")
 .requestMatchers("/api/auth/login","/api/auth/register","/api/health","/actuator/health","/actuator/health/**","/h2-console/**").permitAll()
 .requestMatchers("/api/me/**").hasRole("VOLUNTEER")
 .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/dashboard","/api/events","/api/volunteers","/api/tasks","/api/assignments","/api/notifications").hasAnyRole("ADMIN","ORGANIZER","TEAM_LEAD")
 .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/assignments/*/complete","/api/notifications/*/read").hasAnyRole("ADMIN","ORGANIZER","TEAM_LEAD")
 .requestMatchers("/api/**").hasAnyRole("ADMIN","ORGANIZER")
 .anyRequest().denyAll())
 .headers(h->h.frameOptions(f->f.sameOrigin())).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class); return http.build(); }
}
