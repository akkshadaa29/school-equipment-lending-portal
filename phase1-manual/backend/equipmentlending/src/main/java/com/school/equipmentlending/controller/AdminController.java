package com.school.equipmentlending.controller;

import com.school.equipmentlending.model.Role;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.repository.RoleRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public AdminController(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    /**
     * Promote a user to ROLE_ADMIN.
     * Requires the caller to be an admin (SecurityConfig protects /api/admin/**).
     */
    @PostMapping("/promote/{username}")
    public String promoteToAdmin(@PathVariable String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepo.save(r);
                });

        var roles = user.getRoles();
        if (roles == null) roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);
        userRepo.save(user);

        return "User " + username + " promoted to ROLE_ADMIN";
    }
}
