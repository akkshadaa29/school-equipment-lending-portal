package com.school.equipmentlending.repository;

import com.school.equipmentlending.model.Loan;
import com.school.equipmentlending.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByBorrower_Username(String username);

    List<Loan> findByStatus(LoanStatus status);

    @Query("""
      SELECT COALESCE(SUM(l.quantity), 0)
      FROM Loan l
      WHERE l.equipment.id = :equipmentId
        AND l.status = 'BORROWED'
        AND l.borrowedAt < :endAt
        AND (l.dueAt IS NULL OR l.dueAt > :startAt)
    """)
    Long sumOverlappingReserved(@Param("equipmentId") Long equipmentId,
                                @Param("startAt") LocalDateTime startAt,
                                @Param("endAt") LocalDateTime endAt);

    @Query("""
      SELECT COALESCE(SUM(l.quantity), 0)
      FROM Loan l
      WHERE l.equipment.id = :equipmentId
        AND l.status = 'BORROWED'
        AND l.borrowedAt < :now
        AND (l.dueAt IS NULL OR l.dueAt > :now)
    """)
    Long sumCurrentlyReserved(@Param("equipmentId") Long equipmentId,
                              @Param("now") LocalDateTime now);

    /**
     * Return true if any Loan references the given equipment id.
     * This is used to prevent deleting equipment that has related loans.
     */
    boolean existsByEquipment_Id(Long equipmentId);
}
