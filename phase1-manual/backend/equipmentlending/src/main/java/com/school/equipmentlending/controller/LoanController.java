package com.school.equipmentlending.controller;

import com.school.equipmentlending.dto.BorrowRequest;
import com.school.equipmentlending.dto.LoanResponse;
import com.school.equipmentlending.service.LoanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Immediate borrow (authenticated users)
     */
    @PostMapping("/borrow")
    public ResponseEntity<LoanResponse> borrowNow(@Valid @RequestBody BorrowRequest req, Authentication authentication) {
        String username = authentication.getName();
        LoanResponse res = loanService.borrowNow(username, req);
        return ResponseEntity.ok(res);
    }

    /**
     * Mark loan returned (borrower or admin)
     */
    @PostMapping("/{loanId}/return")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long loanId, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        LoanResponse res = loanService.markLoanReturned(loanId, username, isAdmin);
        return ResponseEntity.ok(res);
    }

    /**
     * Get loans for current user
     */
    @GetMapping("/my")
    public ResponseEntity<List<LoanResponse>> myLoans(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(loanService.getLoansForUser(username));
    }

    /**
     * Admin: list all loans
     */
    @GetMapping
    public ResponseEntity<List<LoanResponse>> allLoans(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    /**
     * Admin: list active loans
     */
    @GetMapping("/active")
    public ResponseEntity<List<LoanResponse>> activeLoans(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(loanService.getActiveLoans());
    }
}
