package com.school.equipmentlending.service;

import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.dto.EquipmentRequest;
import com.school.equipmentlending.exception.ResourceNotFoundException;
import com.school.equipmentlending.mapper.EquipmentMapper;
import com.school.equipmentlending.model.Equipment;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {
    private static final Logger logger = LoggerFactory.getLogger(EquipmentService.class);

    private final EquipmentRepository equipmentRepository;
    private final LoanRepository loanRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, LoanRepository loanRepository) {
        this.equipmentRepository = equipmentRepository;
        this.loanRepository = loanRepository;
    }

    /** Return all equipment DTOs with computed availableUnits. */
    public List<EquipmentDTO> getAllEquipment() {
        LocalDateTime now = LocalDateTime.now();
        return equipmentRepository.findAll().stream()
                .map(e -> toDtoWithAvailability(e, now))
                .collect(Collectors.toList());
    }

    /**
     * Search equipment by optional category and optional availability flag.
     * - category: exact match (ignore case)
     * - available: if true, only return items with availableUnits > 0; if false, only those with availableUnits == 0
     */
    public List<EquipmentDTO> search(String q, String category, Boolean available) {
        LocalDateTime now = LocalDateTime.now();

        // Fetch initial list based on category
        List<Equipment> list;
        if (category != null && !category.isBlank()) {
            list = equipmentRepository.findByCategoryIgnoreCase(category);
        } else {
            list = equipmentRepository.findAll();
        }

        // Convert and filter
        return list.stream()
                .map(e -> toDtoWithAvailability(e, now))
                .filter(dto -> {

                    if (available != null) {
                        if (available && dto.getAvailableUnits() <= 0) return false;
                        if (!available && dto.getAvailableUnits() > 0) return false;
                    }

                    if (q != null && !q.isBlank()) {
                        String query = q.toLowerCase();
                        return dto.getName().toLowerCase().contains(query)
                                || (dto.getCategory() != null && dto.getCategory().toLowerCase().contains(query))
                                || (dto.getName() != null && dto.getName().toLowerCase().contains(query));
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }


    public List<EquipmentDTO> getAvailableEquipment() {
        return search(null, null,true);
    }

    public EquipmentDTO getById(Long id) {
        Equipment e = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id " + id));
        return toDtoWithAvailability(e, LocalDateTime.now());
    }

    public EquipmentDTO createEquipment(EquipmentRequest req) {
        Equipment e = EquipmentMapper.fromRequest(req);
        Equipment saved = equipmentRepository.save(e);
        return toDtoWithAvailability(saved, LocalDateTime.now());
    }

    public EquipmentDTO updateEquipment(Long id, EquipmentRequest req) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id " + id));
        EquipmentMapper.applyUpdate(equipment, req);
        Equipment saved = equipmentRepository.save(equipment);
        return toDtoWithAvailability(saved, LocalDateTime.now());
    }

    /**
     * Delete equipment only if it exists and no loans reference it.
     * If loans exist, throws IllegalStateException which is mapped to HTTP 409 by the global handler.
     */
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment not found with id " + id);
        }

        if (loanRepository.existsByEquipment_Id(id)) {
            throw new IllegalStateException(
                    "Cannot delete this equipment because one or more loans reference it. " +
                            "Please remove those loans first or mark the equipment unavailable."
            );
        }

        equipmentRepository.deleteById(id);
    }

    private EquipmentDTO toDtoWithAvailability(Equipment e, LocalDateTime when) {
        EquipmentDTO dto = EquipmentMapper.toDTO(e);
        Long reserved = loanRepository.sumCurrentlyReserved(e.getId(), when);
        int reservedQty = reserved == null ? 0 : reserved.intValue();
        int availableUnits = e.getQuantity() - reservedQty;
        if (availableUnits < 0) availableUnits = 0;
        dto.setAvailableUnits(availableUnits);
        dto.setAvailable(availableUnits > 0);
        return dto;
    }
}
