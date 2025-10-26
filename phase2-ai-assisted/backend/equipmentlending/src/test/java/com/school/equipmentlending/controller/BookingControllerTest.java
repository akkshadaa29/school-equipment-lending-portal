package com.school.equipmentlending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.school.equipmentlending.dto.BookingDecisionDTO;
import com.school.equipmentlending.dto.BookingRequestDTO;
import com.school.equipmentlending.dto.CreateBookingRequestDTO;
import com.school.equipmentlending.model.*;
import com.school.equipmentlending.repository.BookingRequestRepository;
import com.school.equipmentlending.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the original BookingController implementation which:
 * - delegates to BookingService for create/approve/reject
 * - reads from BookingRequestRepository for myBookings and pending
 * - checks ROLE_ADMIN for admin endpoints
 */
class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingRequestRepository bookingRepo;

    @InjectMocks
    private BookingController controller;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---------------- createBooking ----------------

    @Test
    void createBooking_success_returnsOk() throws Exception {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(1);

        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setId(123L);
        dto.setEquipmentId(1L);
        dto.setRequesterUsername("alice");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alice");

        when(bookingService.createBooking(eq("alice"), any(CreateBookingRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.requesterUsername").value("alice"));

        verify(bookingService).createBooking(eq("alice"), any(CreateBookingRequestDTO.class));
    }

    @Test
    void createBooking_serviceThrowsIllegalArgument_returnsBadRequestWithErrorBody() throws Exception {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(1);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("bob");

        when(bookingService.createBooking(eq("bob"), any())).thenThrow(new IllegalArgumentException("invalid"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists()); // controller returns Map.of("error", ex.getMessage()) in original
    }

    @Test
    void createBooking_runtimeExceptionContainingExceeds_throwsBadRequest() throws Exception {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(1000);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("charlie");

        when(bookingService.createBooking(eq("charlie"), any()))
                .thenThrow(new RuntimeException("Requested quantity exceeds total inventory (1000 > 10)"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------------- myBookings ----------------

    @Test
    void myBookings_returnsListMappedFromRepo() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alice");

        BookingRequest br = new BookingRequest();
        br.setId(11L);
        Equipment e = new Equipment();
        e.setId(5L);
        e.setName("Camera");
        br.setEquipment(e);
        User u = new User();
        u.setUsername("alice");
        br.setRequester(u);
        br.setStartAt(LocalDateTime.now().plusDays(1));
        br.setEndAt(LocalDateTime.now().plusDays(2));
        br.setQuantityRequested(1);
        br.setStatus(BookingStatus.PENDING);

        when(bookingRepo.findByRequester_Username("alice")).thenReturn(List.of(br));

        mockMvc.perform(get("/api/bookings/my").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].equipmentId").value(5))
                .andExpect(jsonPath("$[0].requesterUsername").value("alice"));
    }

    // ---------------- pendingBookings (admin only) ----------------

    @Test
    void pendingBookings_nonAdmin_returnsForbidden() throws Exception {
        Authentication auth = mock(Authentication.class);
        // use doReturn to avoid generics inference issues
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(auth).getAuthorities();

        mockMvc.perform(get("/api/bookings/pending").principal(auth))
                .andExpect(status().isForbidden());
    }

    @Test
    void pendingBookings_admin_returnsList() throws Exception {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(auth).getAuthorities();

        BookingRequest br = new BookingRequest();
        br.setId(21L);
        Equipment e = new Equipment();
        e.setId(7L);
        e.setName("Projector");
        br.setEquipment(e);
        User u = new User();
        u.setUsername("dave");
        br.setRequester(u);
        br.setStatus(BookingStatus.PENDING);

        when(bookingRepo.findByStatus(BookingStatus.PENDING)).thenReturn(List.of(br));

        mockMvc.perform(get("/api/bookings/pending").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(21))
                .andExpect(jsonPath("$[0].equipmentId").value(7));
    }

    // ---------------- approve/reject (admin only) ----------------

    @Test
    void approveBooking_nonAdmin_returnsForbidden() throws Exception {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(auth).getAuthorities();

        mockMvc.perform(post("/api/bookings/10/approve").principal(auth))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveBooking_admin_callsServiceAndReturnsDto() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("adminUser");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(auth).getAuthorities();

        BookingRequestDTO resp = new BookingRequestDTO();
        resp.setId(10L);
        resp.setRequesterUsername("alice");
        when(bookingService.approveBooking(eq(10L), eq("adminUser"), isNull())).thenReturn(resp);

        mockMvc.perform(post("/api/bookings/10/approve").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.requesterUsername").value("alice"));

        verify(bookingService).approveBooking(10L, "adminUser", null);
    }

    @Test
    void rejectBooking_admin_withDecisionNote_passesNoteToService() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("adminUser");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(auth).getAuthorities();

        BookingDecisionDTO decision = new BookingDecisionDTO();
        decision.setAdminNote("Not enough stock");

        BookingRequestDTO resp = new BookingRequestDTO();
        resp.setId(99L);
        when(bookingService.rejectBooking(eq(99L), eq("adminUser"), eq("Not enough stock"))).thenReturn(resp);

        mockMvc.perform(post("/api/bookings/99/reject")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(decision)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(bookingService).rejectBooking(99L, "adminUser", "Not enough stock");
    }
}
