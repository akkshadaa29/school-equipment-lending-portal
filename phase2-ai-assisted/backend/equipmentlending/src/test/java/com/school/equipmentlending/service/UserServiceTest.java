package com.school.equipmentlending.service;

import com.school.equipmentlending.dto.SignupRequest;
import com.school.equipmentlending.model.Role;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.repository.RoleRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @InjectMocks private UserService userService;

    @Captor ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- registerUser ----------

    @Test
    void registerUser_whenUsernameExists_throwsRuntimeException() {
        SignupRequest req = new SignupRequest();
        req.setUsername("alice");
        req.setPassword("pass");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.registerUser(req));

        assertEquals("Username already taken!", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withExistingRole_reusesRoleAndSavesUser() {
        SignupRequest req = new SignupRequest();
        req.setUsername("bob");
        req.setPassword("secret");

        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setName("ROLE_USER");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(existingRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        User result = userService.registerUser(req);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("bob", result.getUsername());
        assertThat(result.getRoles()).contains(existingRole);

        // ensure password encoded
        assertNotEquals("secret", result.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("secret", result.getPassword()));

        verify(roleRepository, never()).save(any(Role.class)); // reused existing role
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_whenRoleNotExists_createsAndSavesNewRole() {
        SignupRequest req = new SignupRequest();
        req.setUsername("newuser");
        req.setPassword("pass123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        Role createdRole = new Role();
        createdRole.setId(2L);
        createdRole.setName("ROLE_USER");

        when(roleRepository.save(any(Role.class))).thenReturn(createdRole);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });

        User saved = userService.registerUser(req);

        assertNotNull(saved);
        assertEquals(20L, saved.getId());
        assertEquals("newuser", saved.getUsername());
        assertThat(saved.getRoles()).extracting(Role::getName).contains("ROLE_USER");

        verify(roleRepository).save(any(Role.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_encodesPasswordBeforeSaving() {
        SignupRequest req = new SignupRequest();
        req.setUsername("encuser");
        req.setPassword("plainpass");

        Role role = new Role(); role.setName("ROLE_USER");

        when(userRepository.existsByUsername("encuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(req);

        assertNotEquals("plainpass", saved.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("plainpass", saved.getPassword()));
        assertThat(saved.getRoles()).extracting(Role::getName).contains("ROLE_USER");
    }
}
