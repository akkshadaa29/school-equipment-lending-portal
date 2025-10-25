package com.school.equipmentlending.service;

import com.school.equipmentlending.dto.BookingRequestDTO;
import com.school.equipmentlending.dto.CreateBookingRequestDTO;
import com.school.equipmentlending.exception.BadRequestException;
import com.school.equipmentlending.exception.ResourceNotFoundException;
import com.school.equipmentlending.mapper.BookingMapper;
import com.school.equipmentlending.model.*;
import com.school.equipmentlending.repository.BookingRequestRepository;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.repository.LoanRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRequestRepository bookingRepo;
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final LoanRepository loanRepo;

    public BookingService(BookingRequestRepository bookingRepo,
                          EquipmentRepository equipmentRepo,
                          UserRepository userRepo,
                          LoanRepository loanRepo) {
        this.bookingRepo = bookingRepo;
        this.equipmentRepo = equipmentRepo;
        this.userRepo = userRepo;
        this.loanRepo = loanRepo;
    }

    /**
     * Create a booking request (PENDING)
     */
    @Transactional
    public BookingRequestDTO createBooking(String username, CreateBookingRequestDTO req) {
        logger.info("User {} creating booking for equipment {} from {} to {} (qty={})",
                username, req.getEquipmentId(), req.getStartAt(), req.getEndAt(), req.getQuantityRequested());

        // ✅ validation
        if (req.getStartAt() == null || req.getEndAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end time are required");
        }

        if (!req.getEndAt().isAfter(req.getStartAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        Equipment equipment = equipmentRepo.findById(req.getEquipmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Equipment not found with id " + req.getEquipmentId()));

        if (req.getQuantityRequested() == null || req.getQuantityRequested() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        // ✅ only fail when requested > total available inventory
        int available = equipment.getQuantity();
        if (req.getQuantityRequested() > available) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Requested quantity (" + req.getQuantityRequested() + ") exceeds total inventory (" + available + ")");
        }

        User requester = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + username));

        BookingRequest booking = new BookingRequest();
        booking.setEquipment(equipment);
        booking.setRequester(requester);
        booking.setStartAt(req.getStartAt());
        booking.setEndAt(req.getEndAt());
        booking.setQuantityRequested(req.getQuantityRequested());
        booking.setStatus(BookingStatus.PENDING);

        BookingRequest saved = bookingRepo.save(booking);
        logger.info("Booking created id={} by {} for equipmentId={}", saved.getId(), username, equipment.getId());
        return BookingMapper.toDTO(saved);
    }

    /**
     * Approve booking (admin)
     */
    @Transactional
    public BookingRequestDTO approveBooking(Long bookingId, String adminUsername, String adminNote) {
        logger.info("Admin {} approving booking {}", adminUsername, bookingId);

        BookingRequest booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found with id " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING bookings can be approved");
        }

        LocalDateTime start = booking.getStartAt();
        LocalDateTime end = booking.getEndAt();
        int requested = booking.getQuantityRequested();

        try {
            Equipment equipment = equipmentRepo.findByIdForUpdate(booking.getEquipment().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Equipment not found with id " + booking.getEquipment().getId()));

            Long reserved = loanRepo.sumOverlappingReserved(equipment.getId(), start, end);
            long reservedQty = (reserved == null ? 0L : reserved);
            long availableUnits = equipment.getQuantity() - reservedQty;

            logger.debug("Equipment id={} total={}, reserved={}, available={}, requested={}",
                    equipment.getId(), equipment.getQuantity(), reservedQty, availableUnits, requested);

            if (availableUnits < requested) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not enough units available. Available: " + availableUnits + ", requested: " + requested);
            }

            Loan loan = new Loan();
            loan.setEquipment(equipment);
            loan.setBorrower(booking.getRequester());
            loan.setBorrowedAt(start);
            loan.setDueAt(end);
            loan.setQuantity(requested);
            loan.setStatus(LoanStatus.BORROWED);
            loanRepo.save(loan);

            booking.setStatus(BookingStatus.APPROVED);
            booking.setAdminNote(adminNote == null ? "Approved by " + adminUsername : adminNote);
            bookingRepo.save(booking);

            return BookingMapper.toDTO(booking);

        } catch (PessimisticLockingFailureException ex) {
            logger.warn("Lock failed approving booking {}", bookingId, ex);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not acquire lock, please try again");
        }
    }

    /**
     * Reject booking (admin)
     */
    @Transactional
    public BookingRequestDTO rejectBooking(Long bookingId, String adminUsername, String adminNote) {
        BookingRequest booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found with id " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setAdminNote(adminNote == null ? "Rejected by " + adminUsername : adminNote);
        bookingRepo.save(booking);
        return BookingMapper.toDTO(booking);
    }
}
