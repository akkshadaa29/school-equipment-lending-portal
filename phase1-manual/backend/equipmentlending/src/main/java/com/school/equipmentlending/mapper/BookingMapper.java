package com.school.equipmentlending.mapper;

import com.school.equipmentlending.dto.BookingRequestDTO;
import com.school.equipmentlending.model.BookingRequest;

public class BookingMapper {
    public static BookingRequestDTO toDTO(BookingRequest br) {
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setId(br.getId());
        if (br.getEquipment() != null) {
            dto.setEquipmentId(br.getEquipment().getId());
            dto.setEquipmentName(br.getEquipment().getName());
        }
        if (br.getRequester() != null) {
            dto.setRequesterUsername(br.getRequester().getUsername());
        }
        dto.setStartAt(br.getStartAt());
        dto.setEndAt(br.getEndAt());
        dto.setQuantityRequested(br.getQuantityRequested());
        dto.setStatus(br.getStatus());
        dto.setAdminNote(br.getAdminNote());
        dto.setCreatedAt(br.getCreatedAt());
        dto.setUpdatedAt(br.getUpdatedAt());
        return dto;
    }
}
