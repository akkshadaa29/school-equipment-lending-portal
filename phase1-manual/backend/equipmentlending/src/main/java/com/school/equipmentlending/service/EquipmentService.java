package com.school.equipmentlending.service;

import com.school.equipmentlending.controller.EquipmentController;
import com.school.equipmentlending.model.Equipment;
import com.school.equipmentlending.repository.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    private static final Logger logger = LoggerFactory.getLogger(EquipmentService.class);
    private final EquipmentRepository repo;

    public EquipmentService(EquipmentRepository repo) {
        this.repo = repo;
    }

    public List<Equipment> getAllEquipments() {
        return repo.findAll();
    }

    public Equipment getEquipmentById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
    }

    public Equipment addEquipment(Equipment equipment) {
        return repo.save(equipment);
    }

    public Equipment updateEquipment(Long id, Equipment updatedEquipment) {
        return repo.findById(id)
                .map(equipment -> {
                    equipment.setName(updatedEquipment.getName());
                    equipment.setCategory(updatedEquipment.getCategory());
                    equipment.setAvailable(updatedEquipment.isAvailable());
                    return repo.save(equipment);
                })
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
    }

    public void deleteEquipment(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new RuntimeException("Equipment not found with id: " + id);
        }
    }
}
