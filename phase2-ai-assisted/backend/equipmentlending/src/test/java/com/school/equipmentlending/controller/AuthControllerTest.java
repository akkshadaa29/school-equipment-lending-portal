package com.school.equipmentlending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.equipmentlending.dto.LoginRequest;
import com.school.equipmentlending.dto.SignupRequest;
import com.school.equipmentlending.model.Role;
import com.school.equipmentlending.model.User;
import com.school.equipmentlending.repository.UserRepository;
import com.school.equipmentlending.security.JwtUtils;
import com.school.equipmentlending.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController (MockMvc standalone).
 */
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void authenticateUser_success_returnsTokenAndUserDto() throws Exception {
        // Arrange
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getName()).thenReturn("alice");
        // authorities stub using doReturn to avoid generics inference problems
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();

        when(jwtUtils.generateToken(auth)).thenReturn("fake-jwt-token");

        User user = new User();
        user.setId(5L);
        user.setUsername("alice");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Collections.singleton(role));

        when(userRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(user));

        // Act
        var mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert: parse response and check token exists under a few possible names, and user info is present
        String json = mvcResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        java.util.Map<String,Object> root = mapper.readValue(json, java.util.Map.class);

        // token might be named "token", "accessToken", "jwt", "access_token", etc.
        boolean hasToken = root.containsKey("token")
                || root.containsKey("accessToken")
                || root.containsKey("jwt")
                || root.containsKey("access_token")
                || root.containsKey("idToken");

        assertTrue(hasToken, "Expected response to contain a token under one of [token, accessToken, jwt, access_token, idToken]. Response: " + json);

        // user object may be under "user" or nested differently; try common patterns
        boolean userOk = false;
        if (root.containsKey("user") && root.get("user") instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String,Object> userMap = (java.util.Map<String,Object>) root.get("user");
            userOk = "alice".equals(userMap.get("username")) && (Integer.valueOf(5).equals(userMap.get("id")) || Long.valueOf(5).equals(userMap.get("id")));
        } else {
            // fallback: check JSON contains username and id somewhere
            userOk = json.contains("\"alice\"") && json.contains("\"5\"");
        }
        assertTrue(userOk, "Expected user info (username/id) in response. Response: " + json);

        // verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(auth);
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void me_nullAuthentication_returns401() throws Exception {
        // When authentication is null -> should return 401
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_authenticated_returnsUserDto() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("bob");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();

        User user = new User();
        user.setId(9L);
        user.setUsername("bob");
        Role r = new Role();
        r.setName("ROLE_USER");
        user.setRoles(Collections.singleton(r));

        when(userRepository.findByUsername("bob")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/api/auth/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(userRepository).findByUsername("bob");
    }

    @Test
    void signup_callsUserServiceAndReturnsUser() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("newuser");
        req.setPassword("pwd");

        User created = new User();
        created.setId(77L);
        created.setUsername("newuser");
        Role r = new Role();
        r.setName("ROLE_USER");
        created.setRoles(Collections.singleton(r));

        when(userService.registerUser(any(SignupRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userService).registerUser(any(SignupRequest.class));
    }
}
