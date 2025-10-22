package com.school.equipmentlending.controller;

import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.dto.EquipmentRequest;
import com.school.equipmentlending.exception.ResourceNotFoundException;
import com.school.equipmentlending.mapper.EquipmentMapper;
import com.school.equipmentlending.model.Equipment;
import com.school.equipmentlending.repository.EquipmentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentController.class);
    private final EquipmentRepository equipmentRepository;

    public EquipmentController(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @GetMapping
    public List<EquipmentDTO> getAllEquipments() {
        logger.info("Fetching all equipments");
        return equipmentRepository.findAll().stream().map(EquipmentMapper::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/available")
    public List<EquipmentDTO> getAvailableEquipments() {
        logger.info("Fetching available equipments");
        return equipmentRepository.findByAvailableTrue().stream().map(EquipmentMapper::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EquipmentDTO getEquipmentById(@PathVariable Long id) {
        logger.info("Fetching equipment with ID: {}", id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id " + id));
        return EquipmentMapper.toDTO(equipment);
    }

    @PostMapping
    public EquipmentDTO createEquipment(@Valid @RequestBody EquipmentRequest req) {
        logger.info("Creating new equipment: {}", req.getName());
        Equipment toSave = EquipmentMapper.fromRequest(req);
        Equipment saved = equipmentRepository.save(toSave);
        return EquipmentMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public EquipmentDTO updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentRequest req) {
        logger.info("Updating equipment id: {}", id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id " + id));
        EquipmentMapper.applyUpdate(equipment, req);
        Equipment saved = equipmentRepository.save(equipment);
        return EquipmentMapper.toDTO(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        logger.info("Deleting equipment with ID: {}", id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id " + id));
        equipmentRepository.delete(equipment);
        return ResponseEntity.noContent().build();
    }
}
