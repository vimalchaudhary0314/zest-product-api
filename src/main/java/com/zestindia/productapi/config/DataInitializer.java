package com.zestindia.productapi.config;

import com.zestindia.productapi.user.AppUser;
import com.zestindia.productapi.user.Role;
import com.zestindia.productapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            if (!userRepository.existsByEmail("admin@example.com")) {
                userRepository.save(AppUser.builder()
                        .name("Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .build());
            }
            if (!userRepository.existsByEmail("user@example.com")) {
                userRepository.save(AppUser.builder()
                        .name("User")
                        .email("user@example.com")
                        .password(passwordEncoder.encode("User@123"))
                        .role(Role.USER)
                        .build());
            }
        };
    }
}
