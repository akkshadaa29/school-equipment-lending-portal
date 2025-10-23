package com.school.equipmentlending.mapper;

import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.dto.EquipmentRequest;
import com.school.equipmentlending.model.Equipment;

public class EquipmentMapper {

    public static EquipmentDTO toDTO(Equipment equipment) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setCategory(equipment.getCategory());
        dto.setCondition(equipment.getCondition());
        dto.setQuantity(equipment.getQuantity());
        dto.setAvailable(equipment.isAvailable());
        dto.setCreatedAt(equipment.getCreatedAt());
        // availableUnits will be set by the service after computing reservations
        return dto;
    }

    public static Equipment fromRequest(EquipmentRequest req) {
        Equipment e = new Equipment();
        e.setName(req.getName());
        e.setCategory(req.getCategory());
        e.setCondition(req.getCondition());
        if (req.getQuantity() != null) e.setQuantity(req.getQuantity());
        if (req.getAvailable() != null) e.setAvailable(req.getAvailable());
        return e;
    }

    public static void applyUpdate(Equipment equipment, EquipmentRequest req) {
        if (req.getName() != null) equipment.setName(req.getName());
        if (req.getCategory() != null) equipment.setCategory(req.getCategory());
        if (req.getCondition() != null) equipment.setCondition(req.getCondition());
        if (req.getQuantity() != null) equipment.setQuantity(req.getQuantity());
        if (req.getAvailable() != null) equipment.setAvailable(req.getAvailable());
    }
}
