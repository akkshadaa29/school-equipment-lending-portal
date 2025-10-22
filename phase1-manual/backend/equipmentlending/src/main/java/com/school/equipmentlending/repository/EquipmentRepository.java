package com.school.equipmentlending.repository;

import com.school.equipmentlending.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByAvailableTrue();
    List<Equipment> findByCategory(String category);
}
