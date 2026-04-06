package com.finance.dashboard.config;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("System Admin", "admin@finance.com", Role.ADMIN, "Admin@123");
        seed("Default Analyst", "analyst@finance.com", Role.ANALYST, "Analyst@123");
    }

    private void seed(String fullName, String email, Role role, String rawPassword) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if ("CHANGEME".equals(user.getPassword()) || user.getPassword() == null) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
                log.info("Password hash seeded for {}", email);
            } else {
                log.info("User {} already exists and has a password set, skipping", email);
            }
        }, () -> {
            User user = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("Created seed user {} with role {}", email, role);
        });
    }
}
