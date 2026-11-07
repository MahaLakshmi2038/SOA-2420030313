package com.eventsync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("prod")
public class ProductionAdminBootstrap {
    @Bean
    CommandLineRunner seedInitialAdmin(AppUserRepository users, PasswordEncoder encoder,
            @Value("${EVENTSYNC_ADMIN_EMAIL}") String email,
            @Value("${EVENTSYNC_ADMIN_PASSWORD}") String password,
            @Value("${EVENTSYNC_ADMIN_NAME:EventSync Administrator}") String name) {
        return args -> {
            if (email == null || email.isBlank() || !email.contains("@")) {
                throw new IllegalStateException("EVENTSYNC_ADMIN_EMAIL must be configured for the first production startup.");
            }
            if (password == null || password.length() < 14) {
                throw new IllegalStateException("EVENTSYNC_ADMIN_PASSWORD must be at least 14 characters.");
            }
            if (!users.existsByEmailIgnoreCase(email.trim())) {
                AppUser user = new AppUser();
                user.name = name;
                user.email = email.trim().toLowerCase();
                user.passwordHash = encoder.encode(password);
                user.role = "ADMIN";
                users.save(user);
            }
        };
    }
}
