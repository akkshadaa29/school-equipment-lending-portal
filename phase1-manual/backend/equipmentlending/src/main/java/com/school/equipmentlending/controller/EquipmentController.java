package com.school.equipmentlending.controller;

import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.dto.EquipmentRequest;
import com.school.equipmentlending.mapper.EquipmentMapper;
import com.school.equipmentlending.model.Equipment;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.service.EquipmentService;
import com.school.equipmentlending.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@CrossOrigin(origins = "http://localhost:5173")
public class EquipmentController {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentController.class);

    private final EquipmentService equipmentService;
    private final EquipmentRepository equipmentRepository;

    public EquipmentController(EquipmentService equipmentService,
                               EquipmentRepository equipmentRepository) {
        this.equipmentService = equipmentService;
        this.equipmentRepository = equipmentRepository;
    }

    /** Dashboard: list all with availability */
    @GetMapping
    public ResponseEntity<List<EquipmentDTO>> getAllEquipments() {
        logger.info("Fetching all equipments (dashboard)");
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    /**
     * Search/filter endpoint:
     * GET /api/equipments/search?category=Sports&available=true
     */
    @GetMapping("/search")
    public ResponseEntity<List<EquipmentDTO>> searchEquipments(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available) {
        logger.info("Searching equipments: category='{}', available={}", category, available);
        return ResponseEntity.ok(equipmentService.search(q,category, available));
    }

    /** convenience: list available */
    @GetMapping("/available")
    public ResponseEntity<List<EquipmentDTO>> getAvailableEquipments() {
        logger.info("Fetching available equipments (convenience)");
        return ResponseEntity.ok(equipmentService.getAvailableEquipment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDTO> getEquipmentById(@PathVariable Long id) {
        logger.info("Fetching equipment with ID: {}", id);
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentRequest req) {
        logger.info("Creating new equipment: {}", req.getName());
        EquipmentDTO dto = equipmentService.createEquipment(req);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentRequest req) {
        logger.info("Updating equipment id: {}", id);
        EquipmentDTO dto = equipmentService.updateEquipment(id, req);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        logger.info("Deleting equipment with ID: {}", id);
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
