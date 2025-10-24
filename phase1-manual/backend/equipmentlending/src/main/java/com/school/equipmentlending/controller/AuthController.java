package com.school.equipmentlending.controller;

import com.school.equipmentlending.dto.LoginRequest;
import com.school.equipmentlending.dto.LoginResponse;
import com.school.equipmentlending.dto.SignupRequest;
import com.school.equipmentlending.dto.UserDTO;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.repository.UserRepository;
import com.school.equipmentlending.security.JwtUtils;
import com.school.equipmentlending.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // dev only; replace with global CORS for production
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(authentication);

        // Get username from authentication
        String username = authentication.getName();

        // Fetch user entity (to get id, etc.)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserDTO userDto = new UserDTO(user.getId(), user.getUsername(), roles);
        LoginResponse response = new LoginResponse(jwt, userDto);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    // GET /api/auth/me -> returns current user (requires Authorization header)
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        UserDTO dto = new UserDTO(user.getId(), user.getUsername(), roles);
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/signup")
    public User registerUser(@RequestBody SignupRequest request) {
        return userService.registerUser(request);
    }
}
