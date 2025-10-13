package com.darum.auth.config;

import com.darum.auth.model.User;
import com.darum.auth.repositories.UserRepository;
import com.darum.shared.dto.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.management.relation.Role;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Value("${seed.superadmin}")
    private boolean seedSuperAdmin;

    @Value("${superadmin.email}")
    private String superAdminEmail;

    @Value("${superadmin.password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        if (!seedSuperAdmin) return;

        userRepository.findByEmail(superAdminEmail).ifPresentOrElse(
                existing -> System.out.println("✅ SuperAdmin already exists: " + superAdminEmail),
                () -> {
                    User superAdmin = new User();
                    superAdmin.setFirstName("System");
                    superAdmin.setLastName("Administrator");
                    superAdmin.setEmail(superAdminEmail);
                    superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
                    superAdmin.setRoles(List.of(Roles.SUPERADMIN.name(), Roles.ADMIN.name())); // 👈 give both roles

                    userRepository.save(superAdmin);
                    System.out.println("🚀 SuperAdmin seeded successfully: " + superAdminEmail);
                }
        );


    }

}