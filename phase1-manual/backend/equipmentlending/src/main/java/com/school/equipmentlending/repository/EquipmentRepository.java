package com.school.equipmentlending.repository;

import com.school.equipmentlending.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
