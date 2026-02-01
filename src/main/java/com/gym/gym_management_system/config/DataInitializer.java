package com.gym.gym_management_system.config;

import com.gym.gym_management_system.entity.User;
import com.gym.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user exists
        if (!userRepository.existsByEmail("admin@gym.com")) {
            User admin = new User();
            admin.setEmail("admin@gym.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setRole("ADMIN");

            userRepository.save(admin);
            log.info("✅ Admin user created successfully!");
            log.info("📧 Email: admin@gym.com");
            log.info("🔑 Password: admin123");
        } else {
            log.info("ℹ️ Admin user already exists. Skipping creation.");
        }
    }
}
