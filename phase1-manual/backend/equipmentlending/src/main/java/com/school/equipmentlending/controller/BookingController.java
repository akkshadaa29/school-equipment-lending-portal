package com.school.equipmentlending.controller;

import com.school.equipmentlending.dto.BookingDecisionDTO;
import com.school.equipmentlending.dto.BookingRequestDTO;
import com.school.equipmentlending.dto.CreateBookingRequestDTO;
import com.school.equipmentlending.mapper.BookingMapper;
import com.school.equipmentlending.model.BookingRequest;
import com.school.equipmentlending.repository.BookingRequestRepository;
import com.school.equipmentlending.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final BookingRequestRepository bookingRepo;

    public BookingController(BookingService bookingService, BookingRequestRepository bookingRepo) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequestDTO req,
                                           Authentication authentication) {
        String username = authentication.getName();
        try {
            BookingRequestDTO dto = bookingService.createBooking(username, req);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            // for validation-type exceptions from service
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (RuntimeException ex) {
            // for inventory errors or any domain rule violations
            if (ex.getMessage() != null && ex.getMessage().contains("exceeds")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }
            logger.error("Error creating booking", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingRequestDTO>> myBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingRequest> list = bookingRepo.findByRequester_Username(username);
        List<BookingRequestDTO> dtos = list.stream().map(BookingMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /* ---------------- Admin endpoints (approve/reject/list pending) ---------------- */

    @GetMapping("/pending")
    public ResponseEntity<List<BookingRequestDTO>> pendingBookings(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }
        List<BookingRequest> list = bookingRepo.findByStatus(com.school.equipmentlending.model.BookingStatus.PENDING);
        return ResponseEntity.ok(list.stream().map(BookingMapper::toDTO).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BookingRequestDTO> approveBooking(@PathVariable Long id,
                                                            @RequestBody(required = false) BookingDecisionDTO decision,
                                                            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }
        String admin = authentication.getName();
        BookingRequestDTO dto = bookingService.approveBooking(id, admin, decision == null ? null : decision.getAdminNote());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BookingRequestDTO> rejectBooking(@PathVariable Long id,
                                                           @RequestBody(required = false) BookingDecisionDTO decision,
                                                           Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }
        String admin = authentication.getName();
        BookingRequestDTO dto = bookingService.rejectBooking(id, admin, decision == null ? null : decision.getAdminNote());
        return ResponseEntity.ok(dto);
    }
}
