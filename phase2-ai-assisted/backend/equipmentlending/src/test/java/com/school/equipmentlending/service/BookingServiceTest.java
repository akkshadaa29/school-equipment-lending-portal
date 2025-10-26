package com.school.equipmentlending.service;

import com.school.equipmentlending.dto.BookingRequestDTO;
import com.school.equipmentlending.dto.CreateBookingRequestDTO;
import com.school.equipmentlending.model.*;
import com.school.equipmentlending.repository.BookingRequestRepository;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.repository.LoanRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRequestRepository bookingRepo;

    @Mock
    private EquipmentRepository equipmentRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private LoanRepository loanRepo;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- createBooking tests ----------

    @Test
    void createBooking_missingTimes_throwsBadRequest() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        // startAt / endAt left null

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking("alice", req));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Start and end time are required"));
    }

    @Test
    void createBooking_endNotAfterStart_throwsBadRequest() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        LocalDateTime now = LocalDateTime.now();
        req.setStartAt(now.plusDays(1));
        req.setEndAt(now.plusDays(1)); // not after
        req.setQuantityRequested(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking("alice", req));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("End time must be after start time"));
    }

    @Test
    void createBooking_equipmentNotFound_throwsNotFound() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(99L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(1);

        when(equipmentRepo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking("alice", req));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Equipment not found"));
    }

    @Test
    void createBooking_quantityInvalid_throwsBadRequest() {
        // quantity < 1
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(0);

        Equipment eq = new Equipment();
        eq.setId(1L);
        eq.setQuantity(5);
        when(equipmentRepo.findById(1L)).thenReturn(Optional.of(eq));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking("alice", req));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Quantity must be at least 1"));
    }

    @Test
    void createBooking_requestedExceedsInventory_throwsBadRequest() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(10);

        Equipment eq = new Equipment();
        eq.setId(1L);
        eq.setQuantity(3);
        when(equipmentRepo.findById(1L)).thenReturn(Optional.of(eq));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking("alice", req));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("exceeds total inventory"));
    }

    @Test
    void createBooking_userNotFound_throwsNotFound() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(1L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(1);

        Equipment eq = new Equipment();
        eq.setId(1L);
        eq.setQuantity(5);
        when(equipmentRepo.findById(1L)).thenReturn(Optional.of(eq));
        when(userRepo.findByUsername("bob")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking("bob", req));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("User not found"));
    }

    @Test
    void createBooking_success_savesAndReturnsDto() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setEquipmentId(2L);
        req.setStartAt(LocalDateTime.now().plusDays(1));
        req.setEndAt(LocalDateTime.now().plusDays(2));
        req.setQuantityRequested(2);

        Equipment eq = new Equipment();
        eq.setId(2L);
        eq.setQuantity(5);
        when(equipmentRepo.findById(2L)).thenReturn(Optional.of(eq));

        User user = new User();
        user.setId(11L);
        user.setUsername("carol");
        when(userRepo.findByUsername("carol")).thenReturn(Optional.of(user));

        // bookingRepo.save should return booking with id
        when(bookingRepo.save(any(BookingRequest.class))).thenAnswer(invocation -> {
            BookingRequest b = invocation.getArgument(0);
            b.setId(555L);
            return b;
        });

        BookingRequestDTO dto = bookingService.createBooking("carol", req);
        assertNotNull(dto);
        assertEquals(555L, dto.getId());
        assertEquals(2L, dto.getEquipmentId());
        assertEquals("carol", dto.getRequesterUsername());

        verify(bookingRepo, times(1)).save(any(BookingRequest.class));
    }

    // ---------- approveBooking tests ----------

    @Test
    void approveBooking_notFound_throwsNotFound() {
        when(bookingRepo.findById(9L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.approveBooking(9L, "admin", null));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void approveBooking_notPending_throwsBadRequest() {
        BookingRequest b = new BookingRequest();
        b.setId(10L);
        b.setStatus(BookingStatus.APPROVED);
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(b));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.approveBooking(10L, "admin", null));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Only PENDING bookings"));
    }

    @Test
    void approveBooking_equipmentNotFound_throwsNotFound() {
        BookingRequest b = new BookingRequest();
        b.setId(20L);
        b.setStatus(BookingStatus.PENDING);
        Equipment eqRef = new Equipment();
        eqRef.setId(50L);
        b.setEquipment(eqRef);
        when(bookingRepo.findById(20L)).thenReturn(Optional.of(b));
        when(equipmentRepo.findByIdForUpdate(50L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.approveBooking(20L, "admin", null));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Equipment not found"));
    }

    @Test
    void approveBooking_notEnoughUnits_throwsBadRequest() {
        BookingRequest b = new BookingRequest();
        b.setId(30L);
        b.setStatus(BookingStatus.PENDING);
        b.setStartAt(LocalDateTime.now().plusDays(1));
        b.setEndAt(LocalDateTime.now().plusDays(2));
        b.setQuantityRequested(5);
        Equipment eqRef = new Equipment();
        eqRef.setId(60L);
        b.setEquipment(eqRef);

        when(bookingRepo.findById(30L)).thenReturn(Optional.of(b));
        Equipment eq = new Equipment();
        eq.setId(60L);
        eq.setQuantity(3);
        when(equipmentRepo.findByIdForUpdate(60L)).thenReturn(Optional.of(eq));

        // no existing reserved -> sumOverlappingReserved returns 0 -> availableUnits = 3 < requested 5
        when(loanRepo.sumOverlappingReserved(60L, b.getStartAt(), b.getEndAt())).thenReturn(0L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.approveBooking(30L, "admin", null));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Not enough units available"));
    }

    @Test
    void approveBooking_lockFailure_translatesToConflict() {
        BookingRequest b = new BookingRequest();
        b.setId(31L);
        b.setStatus(BookingStatus.PENDING);
        b.setStartAt(LocalDateTime.now().plusDays(1));
        b.setEndAt(LocalDateTime.now().plusDays(2));
        b.setQuantityRequested(1);
        Equipment eqRef = new Equipment();
        eqRef.setId(61L);
        b.setEquipment(eqRef);

        when(bookingRepo.findById(31L)).thenReturn(Optional.of(b));
        // simulate pessimistic lock exception from equipmentRepo
        when(equipmentRepo.findByIdForUpdate(61L)).thenThrow(new PessimisticLockingFailureException("lock failed"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.approveBooking(31L, "admin", null));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Could not acquire lock"));
    }

    @Test
    void approveBooking_success_createsLoanAndApprovesBooking() {
        BookingRequest b = new BookingRequest();
        b.setId(40L);
        b.setStatus(BookingStatus.PENDING);
        LocalDateTime s = LocalDateTime.now().plusDays(1);
        LocalDateTime e = LocalDateTime.now().plusDays(3);
        b.setStartAt(s);
        b.setEndAt(e);
        b.setQuantityRequested(2);

        Equipment eqRef = new Equipment();
        eqRef.setId(70L);
        b.setEquipment(eqRef);

        User requester = new User();
        requester.setId(5L);
        requester.setUsername("dude");
        b.setRequester(requester);

        when(bookingRepo.findById(40L)).thenReturn(Optional.of(b));

        Equipment eq = new Equipment();
        eq.setId(70L);
        eq.setQuantity(10);
        when(equipmentRepo.findByIdForUpdate(70L)).thenReturn(Optional.of(eq));

        // reserved 3 => availableUnits = 7 >= requested 2
        when(loanRepo.sumOverlappingReserved(70L, s, e)).thenReturn(3L);

        // mock loanRepo.save
        when(loanRepo.save(any())).thenAnswer(inv -> {
            Loan saved = inv.getArgument(0);
            saved.setId(888L);
            return saved;
        });

        // mock bookingRepo.save to capture status update
        when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingRequestDTO result = bookingService.approveBooking(40L, "adminX", "ok");

        assertNotNull(result);
        assertEquals(40L, result.getId());
        assertEquals("ok", result.getAdminNote());
        // booking in memory should have updated status
        assertEquals(BookingStatus.APPROVED, b.getStatus());

        verify(loanRepo).save(any(Loan.class));
        verify(bookingRepo, atLeastOnce()).save(any(BookingRequest.class));
    }

    // ---------- rejectBooking tests ----------

    @Test
    void rejectBooking_notFound_throwsNotFound() {
        when(bookingRepo.findById(200L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.rejectBooking(200L, "admin", null));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void rejectBooking_notPending_throwsBadRequest() {
        BookingRequest b = new BookingRequest();
        b.setId(201L);
        b.setStatus(BookingStatus.APPROVED);
        when(bookingRepo.findById(201L)).thenReturn(Optional.of(b));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.rejectBooking(201L, "admin", null));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void rejectBooking_success_updatesStatusAndSaves() {
        BookingRequest b = new BookingRequest();
        b.setId(202L);
        b.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(202L)).thenReturn(Optional.of(b));
        when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingRequestDTO dto = bookingService.rejectBooking(202L, "adminY", "nope");
        assertNotNull(dto);
        assertEquals(202L, dto.getId());
        assertEquals(BookingStatus.REJECTED, b.getStatus());
        assertEquals("nope", b.getAdminNote());
        verify(bookingRepo).save(b);
    }
}

