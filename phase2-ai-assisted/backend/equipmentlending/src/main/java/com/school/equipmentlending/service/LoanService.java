package com.school.equipmentlending.service;

import com.school.equipmentlending.dto.BorrowRequest;
import com.school.equipmentlending.dto.LoanResponse;
import com.school.equipmentlending.exception.BadRequestException;
import com.school.equipmentlending.exception.ResourceNotFoundException;
import com.school.equipmentlending.model.*;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.repository.LoanRepository;
import com.school.equipmentlending.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepo;
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;

    public LoanService(LoanRepository loanRepo,
                       EquipmentRepository equipmentRepo,
                       UserRepository userRepo) {
        this.loanRepo = loanRepo;
        this.equipmentRepo = equipmentRepo;
        this.userRepo = userRepo;
    }

    /**
     * Immediate borrow (creates a BORROWED loan for now -> dueAt as provided or null)
     * Ensures enough units available at the requested interval (now -> dueAt).
     * quantityRequested must be >=1 and <= equipment.totalQuantity.
     */
    @Transactional
    public LoanResponse borrowNow(String username, BorrowRequest req) {
        logger.info("User {} requests immediate borrow of equipmentId={} qty={} days={}",
                username, req.getEquipmentId(), req.getDays(), req.getDays());

        Equipment equipment = equipmentRepo.findById(req.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id " + req.getEquipmentId()));

        int qtyRequested = (req.getQuantity() == null || req.getQuantity() < 1) ? 1 : req.getQuantity();

        if (qtyRequested > equipment.getQuantity()) {
            throw new BadRequestException("Requested quantity exceeds total inventory (" + equipment.getQuantity() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueAt = null;
        if (req.getDays() != null && req.getDays() > 0) {
            dueAt = now.plusDays(req.getDays());
        }

        // Check overlapping reserved quantity in interval [now, dueAt)
        Long reserved = loanRepo.sumOverlappingReserved(equipment.getId(), now, dueAt == null ? now.plusYears(100) : dueAt);
        long reservedQty = (reserved == null ? 0L : reserved.longValue());
        long availableUnits = equipment.getQuantity() - reservedQty;

        logger.debug("BorrowNow check: equipmentId={} total={} reserved={} available={}",
                equipment.getId(), equipment.getQuantity(), reservedQty, availableUnits);

        if (availableUnits < qtyRequested) {
            throw new BadRequestException("Not enough units available now. Available: " + availableUnits + ", requested: " + qtyRequested);
        }

        // create loan
        Loan loan = new Loan();
        loan.setEquipment(equipment);
        loan.setBorrower(userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
        loan.setBorrowedAt(now);
        loan.setDueAt(dueAt);
        loan.setQuantity(qtyRequested);
        loan.setStatus(LoanStatus.BORROWED);
        Loan saved = loanRepo.save(loan);
        logger.info("Loan created id={} for user={} equipmentId={} qty={}", saved.getId(), username, equipment.getId(), qtyRequested);

        return toResponse(saved);
    }

    /**
     * Mark existing loan as returned. Only borrower or admin allowed.
     */
    @Transactional
    public LoanResponse markLoanReturned(Long loanId, String username, boolean isAdmin) {
        logger.info("User {} marking loan {} returned (isAdmin={})", username, loanId, isAdmin);

        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id " + loanId));

        boolean isBorrower = loan.getBorrower() != null && username.equals(loan.getBorrower().getUsername());
        if (!isBorrower && !isAdmin) {
            throw new BadRequestException("Only borrower or admin can mark this loan returned");
        }

        if (loan.getReturnedAt() != null) {
            throw new BadRequestException("Loan already returned");
        }

        LocalDateTime now = LocalDateTime.now();
        loan.setReturnedAt(now);

        if (loan.getDueAt() != null && now.isAfter(loan.getDueAt())) {
            loan.setStatus(LoanStatus.OVERDUE);
        } else {
            loan.setStatus(LoanStatus.RETURNED);
        }

        loanRepo.save(loan);
        logger.info("Loan {} marked returned by {}", loanId, username);

        // Update equipment.available flag to reflect current reservations
        Equipment equipment = loan.getEquipment();
        Long reservedNow = loanRepo.sumCurrentlyReserved(equipment.getId(), now);
        int reservedQty = reservedNow == null ? 0 : reservedNow.intValue();
        boolean avail = (equipment.getQuantity() - reservedQty) > 0;
        equipment.setAvailable(avail);
        equipmentRepo.save(equipment);

        return toResponse(loan);
    }

    public List<LoanResponse> getLoansForUser(String username) {
        return loanRepo.findByBorrower_Username(username).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LoanResponse> getActiveLoans() {
        return loanRepo.findByStatus(LoanStatus.BORROWED).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private LoanResponse toResponse(Loan loan) {
        LoanResponse r = new LoanResponse();
        r.setId(loan.getId());
        if (loan.getEquipment() != null) {
            r.setEquipmentId(loan.getEquipment().getId());
            r.setEquipmentName(loan.getEquipment().getName());
        }
        if (loan.getBorrower() != null) {
            r.setBorrowerId(loan.getBorrower().getId());
            r.setBorrowerUsername(loan.getBorrower().getUsername());
        }
        r.setBorrowedAt(loan.getBorrowedAt());
        r.setDueAt(loan.getDueAt());
        r.setReturnedAt(loan.getReturnedAt());
        r.setStatus(loan.getStatus());
        r.setQuantity(loan.getQuantity());
        return r;
    }
}