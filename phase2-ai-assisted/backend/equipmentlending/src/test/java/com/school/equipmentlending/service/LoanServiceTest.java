package com.school.equipmentlending.service;

import com.school.equipmentlending.dto.BorrowRequest;
import com.school.equipmentlending.dto.LoanResponse;
import com.school.equipmentlending.exception.BadRequestException;
import com.school.equipmentlending.exception.ResourceNotFoundException;
import com.school.equipmentlending.model.*;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.repository.LoanRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock private LoanRepository loanRepo;
    @Mock private EquipmentRepository equipmentRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- borrowNow tests ----------

    @Test
    void borrowNow_equipmentNotFound_throwsResourceNotFound() {
        BorrowRequest req = new BorrowRequest();
        req.setEquipmentId(99L);
        when(equipmentRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.borrowNow("alice", req));
    }

    @Test
    void borrowNow_requestedGreaterThanTotal_throwsBadRequest() {
        Equipment e = new Equipment(); e.setId(1L); e.setQuantity(2);
        when(equipmentRepo.findById(1L)).thenReturn(Optional.of(e));

        BorrowRequest req = new BorrowRequest();
        req.setEquipmentId(1L);
        req.setQuantity(5); // > 2

        BadRequestException ex = assertThrows(BadRequestException.class, () -> loanService.borrowNow("bob", req));
        assertTrue(ex.getMessage().contains("Requested quantity exceeds total inventory"));
    }

    @Test
    void borrowNow_notEnoughAvailableNow_throwsBadRequest() {
        Equipment e = new Equipment(); e.setId(2L); e.setQuantity(5);
        when(equipmentRepo.findById(2L)).thenReturn(Optional.of(e));

        BorrowRequest req = new BorrowRequest();
        req.setEquipmentId(2L);
        req.setQuantity(3);
        req.setDays(2);

        // reserved = 4 => availableUnits = 1 < requested 3
        when(loanRepo.sumOverlappingReserved(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(4L);

        when(userRepo.findByUsername("carol")).thenReturn(Optional.of(new User()));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> loanService.borrowNow("carol", req));
        assertTrue(ex.getMessage().contains("Not enough units available now"));
    }

    @Test
    void borrowNow_success_createsLoan_andReturnsResponse() {
        Equipment e = new Equipment(); e.setId(3L); e.setQuantity(10); e.setName("Cam");
        when(equipmentRepo.findById(3L)).thenReturn(Optional.of(e));

        User u = new User(); u.setId(7L); u.setUsername("dude");
        when(userRepo.findByUsername("dude")).thenReturn(Optional.of(u));

        BorrowRequest req = new BorrowRequest();
        req.setEquipmentId(3L);
        req.setQuantity(2);
        req.setDays(5);

        // no reserved units
        when(loanRepo.sumOverlappingReserved(eq(3L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);

        when(loanRepo.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            l.setId(500L);
            return l;
        });

        LoanResponse resp = loanService.borrowNow("dude", req);
        assertNotNull(resp);
        assertEquals(500L, resp.getId());
        assertEquals(3L, resp.getEquipmentId());
        assertEquals("dude", resp.getBorrowerUsername());
        assertEquals(2, resp.getQuantity());

        verify(loanRepo).save(any(Loan.class));
    }

    // ---------- markLoanReturned tests ----------

    @Test
    void markLoanReturned_notFound_throwsResourceNotFound() {
        when(loanRepo.findById(42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> loanService.markLoanReturned(42L, "alice", false));
    }

    @Test
    void markLoanReturned_notBorrowerOrAdmin_throwsBadRequest() {
        Loan loan = new Loan();
        loan.setId(43L);
        User borrower = new User(); borrower.setUsername("owner");
        loan.setBorrower(borrower);
        loan.setReturnedAt(null);
        when(loanRepo.findById(43L)).thenReturn(Optional.of(loan));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> loanService.markLoanReturned(43L, "intruder", false));
        assertTrue(ex.getMessage().contains("Only borrower or admin"));
    }

    @Test
    void markLoanReturned_alreadyReturned_throwsBadRequest() {
        Loan loan = new Loan();
        loan.setId(44L);
        User borrower = new User(); borrower.setUsername("owner");
        loan.setBorrower(borrower);
        loan.setReturnedAt(LocalDateTime.now().minusDays(1));
        when(loanRepo.findById(44L)).thenReturn(Optional.of(loan));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> loanService.markLoanReturned(44L, "owner", false));
        assertTrue(ex.getMessage().contains("Loan already returned"));
    }

    @Test
    void markLoanReturned_success_byBorrower_updatesLoanAndEquipment() {
        Equipment eq = new Equipment(); eq.setId(8L); eq.setQuantity(5); eq.setAvailable(true);
        Loan loan = new Loan();
        loan.setId(45L);
        loan.setEquipment(eq);
        User borrower = new User(); borrower.setId(9L); borrower.setUsername("sam");
        loan.setBorrower(borrower);
        loan.setDueAt(LocalDateTime.now().plusDays(1));
        loan.setReturnedAt(null);
        loan.setQuantity(1);
        loan.setStatus(LoanStatus.BORROWED);

        when(loanRepo.findById(45L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepo.sumCurrentlyReserved(eq(8L), any(LocalDateTime.class))).thenReturn(0L);
        when(equipmentRepo.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse resp = loanService.markLoanReturned(45L, "sam", false);
        assertNotNull(resp);
        assertEquals(45L, resp.getId());
        assertNotNull(resp.getReturnedAt());
        assertTrue(loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.OVERDUE);

        verify(loanRepo).save(loan);
        verify(equipmentRepo).save(eq);
    }

    // ---------- simple passthrough methods ----------

    @Test
    void getLoansForUser_returnsMappedList() {
        Loan loan = new Loan(); loan.setId(60L);
        User u = new User(); u.setUsername("tom");
        loan.setBorrower(u);
        when(loanRepo.findByBorrower_Username("tom")).thenReturn(List.of(loan));

        List<LoanResponse> list = loanService.getLoansForUser("tom");
        assertThat(list).hasSize(1);
        assertEquals(60L, list.get(0).getId());
    }

    @Test
    void getAllLoans_and_getActiveLoans_mapResults() {
        Loan a = new Loan(); a.setId(70L);
        when(loanRepo.findAll()).thenReturn(List.of(a));
        when(loanRepo.findByStatus(LoanStatus.BORROWED)).thenReturn(List.of(a));

        List<LoanResponse> all = loanService.getAllLoans();
        List<LoanResponse> active = loanService.getActiveLoans();

        assertThat(all).hasSize(1);
        assertThat(active).hasSize(1);
        assertEquals(70L, all.get(0).getId());
        assertEquals(70L, active.get(0).getId());

        verify(loanRepo).findAll();
        verify(loanRepo).findByStatus(LoanStatus.BORROWED);
    }
}
