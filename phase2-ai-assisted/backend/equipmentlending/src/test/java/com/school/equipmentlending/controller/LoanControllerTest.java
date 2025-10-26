package com.school.equipmentlending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.school.equipmentlending.dto.BorrowRequest;
import com.school.equipmentlending.dto.LoanResponse;
import com.school.equipmentlending.service.LoanService;
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
 * Unit tests for LoanController (MockMvc standalone).
 */
class LoanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController controller;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void borrowNow_success_callsServiceAndReturnsOk() throws Exception {
        BorrowRequest req = new BorrowRequest();
        req.setEquipmentId(5L);
        req.setQuantity(2);
        req.setDays(3);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alice");

        LoanResponse resp = new LoanResponse();
        resp.setId(42L);
        resp.setEquipmentId(5L);
        resp.setBorrowerUsername("alice");
        resp.setBorrowedAt(LocalDateTime.now());
        resp.setDueAt(LocalDateTime.now().plusDays(3));
        resp.setQuantity(2);

        when(loanService.borrowNow(eq("alice"), any(BorrowRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.equipmentId").value(5))
                .andExpect(jsonPath("$.borrowerUsername").value("alice"));

        verify(loanService).borrowNow(eq("alice"), any(BorrowRequest.class));
    }

    @Test
    void returnLoan_callsServiceAndReturnsOk() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("bob");
        // mark non-admin
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(auth).getAuthorities();

        LoanResponse resp = new LoanResponse();
        resp.setId(10L);
        resp.setBorrowerUsername("bob");
        resp.setReturnedAt(LocalDateTime.now());
        resp.setStatus(null); // not asserting status here

        when(loanService.markLoanReturned(eq(10L), eq("bob"), eq(false))).thenReturn(resp);

        mockMvc.perform(post("/api/loans/10/return").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.borrowerUsername").value("bob"));

        verify(loanService).markLoanReturned(10L, "bob", false);
    }

    @Test
    void myLoans_returnsListForUser() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("charlie");

        LoanResponse lr = new LoanResponse();
        lr.setId(7L);
        lr.setBorrowerUsername("charlie");
        lr.setEquipmentId(2L);

        when(loanService.getLoansForUser("charlie")).thenReturn(List.of(lr));

        mockMvc.perform(get("/api/loans/my").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].borrowerUsername").value("charlie"));
    }

    @Test
    void allLoans_nonAdmin_returnsForbidden() throws Exception {
        Authentication auth = mock(Authentication.class);
        // user, not admin
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(auth).getAuthorities();

        mockMvc.perform(get("/api/loans").principal(auth))
                .andExpect(status().isForbidden());

        verifyNoInteractions(loanService);
    }

    @Test
    void allLoans_admin_returnsList() throws Exception {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(auth).getAuthorities();

        LoanResponse lr = new LoanResponse();
        lr.setId(8L);
        lr.setEquipmentId(3L);

        when(loanService.getAllLoans()).thenReturn(List.of(lr));

        mockMvc.perform(get("/api/loans").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(8));

        verify(loanService).getAllLoans();
    }

    @Test
    void activeLoans_admin_returnsList() throws Exception {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(auth).getAuthorities();

        LoanResponse lr = new LoanResponse();
        lr.setId(9L);
        lr.setEquipmentId(4L);

        when(loanService.getActiveLoans()).thenReturn(List.of(lr));

        mockMvc.perform(get("/api/loans/active").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(9));

        verify(loanService).getActiveLoans();
    }
}
