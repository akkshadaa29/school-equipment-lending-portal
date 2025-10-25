package com.school.equipmentlending.config;

import com.school.equipmentlending.model.Role;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.repository.RoleRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DefaultAdminRunner {

    @Bean
    CommandLineRunner createDefaultAdmin(UserRepository userRepo, RoleRepository roleRepo) {
        return args -> {
            String adminUsername = "admin";
            String adminPassword = "adminpass"; // change for real use

            if (!userRepo.existsByUsername(adminUsername)) {

                Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                        .orElseGet(() -> {
                            Role r = new Role();
                            r.setName("ROLE_ADMIN");
                            return roleRepo.save(r);
                        });

                Role userRole = roleRepo.findByName("ROLE_USER")
                        .orElseGet(() -> {
                            Role r = new Role();
                            r.setName("ROLE_USER");
                            return roleRepo.save(r);
                        });

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                roles.add(userRole);
                admin.setRoles(roles);

                userRepo.save(admin);
                System.out.println("Default admin created -> username: " + adminUsername + ", password: " + adminPassword);
            } else {
                System.out.println("Default admin already exists.");
            }
        };
    }
}
