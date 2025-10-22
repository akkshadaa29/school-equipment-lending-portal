package com.school.equipmentlending.controller;

import com.school.equipmentlending.dto.LoginRequest;
import com.school.equipmentlending.dto.LoginResponse;
import com.school.equipmentlending.dto.SignupRequest;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.security.JwtUtils;
import com.school.equipmentlending.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 🧩 Endpoint: User signup / registration
     * URL: POST /api/auth/signup
     */
    @PostMapping("/signup")
    public User registerUser(@RequestBody SignupRequest request) {
        return userService.registerUser(request);
    }

    /**
     * 🧩 Endpoint: User login (returns JWT token)
     * URL: POST /api/auth/login
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        // Authenticate credentials using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        // Generate JWT token
        String token = jwtUtils.generateToken(authentication);

        // Return token as JSON
        return new LoginResponse(token);
    }

    /**
     * 🧩 Optional: Check logged-in user details
     * URL: GET /api/auth/me (requires JWT)
     */
    @GetMapping("/me")
    public Object getCurrentUser(Authentication authentication) {
        return authentication != null
                ? authentication.getPrincipal()
                : "No active session";
    }
}
