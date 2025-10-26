package com.school.equipmentlending.controller;

import com.school.equipmentlending.model.Role;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.repository.RoleRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @InjectMocks
    private AdminController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void promoteToAdmin_whenRoleExists_addsRoleAndSavesUser() throws Exception {
        // Arrange
        String username = "alice";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setRoles(null); // simulate no roles initially

        Role adminRole = new Role();
        adminRole.setId(99L);
        adminRole.setName("ROLE_ADMIN");

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        mockMvc.perform(post("/api/admin/promote/{username}", username))
                .andExpect(status().isOk())
                .andExpect(content().string("User " + username + " promoted to ROLE_ADMIN"));

        // Verify userRepo.save called and user's roles now contain admin role
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(captor.capture());
        User saved = captor.getValue();
        Set<Role> roles = saved.getRoles();
        assertThat(roles).isNotNull();
        assertThat(roles).extracting(Role::getName).contains("ROLE_ADMIN");
    }

    @Test
    void promoteToAdmin_whenRoleDoesNotExist_createsRoleAndAddsToUser() throws Exception {
        // Arrange
        String username = "bob";
        User user = new User();
        user.setId(2L);
        user.setUsername(username);
        user.setRoles(new HashSet<>()); // empty set

        // roleRepo returns empty -> controller will create Role and call roleRepo.save(...)
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());

        Role savedRole = new Role();
        savedRole.setId(100L);
        savedRole.setName("ROLE_ADMIN");
        when(roleRepo.save(any(Role.class))).thenReturn(savedRole);

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        mockMvc.perform(post("/api/admin/promote/{username}", username))
                .andExpect(status().isOk())
                .andExpect(content().string("User " + username + " promoted to ROLE_ADMIN"));

        // Verify roleRepo.save was called and userRepo.save was called with role added
        verify(roleRepo, times(1)).save(any(Role.class));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getRoles()).isNotNull();
        assertThat(savedUser.getRoles()).extracting(Role::getName).contains("ROLE_ADMIN");
    }

    @Test
    void promoteToAdmin_userNotFound_throwsRuntimeException() throws Exception {
        // Arrange
        String username = "noone";
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert: standalone MockMvc will propagate the exception -> assert it
        Exception resolved = assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/admin/promote/{username}", username))
                        .andReturn()
        );

        // The ServletException wraps the RuntimeException — assert message contains expected text
        Throwable root = resolved.getCause();
        while (root != null && root.getCause() != null) root = root.getCause();
        assertNotNull(root);
        assertTrue(root.getMessage().contains("User not found: " + username));
    }

}
