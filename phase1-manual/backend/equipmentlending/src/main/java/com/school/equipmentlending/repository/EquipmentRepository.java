package com.school.equipmentlending.repository;

import com.school.equipmentlending.model.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // case-insensitive category
    List<Equipment> findByCategoryIgnoreCase(String category);

    // convenience: all available (stored flag)
    List<Equipment> findByAvailableTrue();

    // availability general
    List<Equipment> findByAvailable(boolean available);

    // partial name search
    List<Equipment> findByNameContainingIgnoreCase(String namePart);

    // combined helpers
    List<Equipment> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(String namePart, String category);
    List<Equipment> findByNameContainingIgnoreCaseAndAvailableTrue(String namePart);
    List<Equipment> findByNameContainingIgnoreCaseAndCategoryIgnoreCaseAndAvailableTrue(String namePart, String category);

    Optional<Equipment> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdForUpdate(@Param("id") Long id);
}
