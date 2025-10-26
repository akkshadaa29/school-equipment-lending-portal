package com.school.equipmentlending.mapper;

import com.school.equipmentlending.dto.BookingRequestDTO;
import com.school.equipmentlending.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toDTO_copiesAllFields_whenEquipmentAndRequesterPresent() {
        // Arrange
        Equipment equipment = new Equipment();
        equipment.setId(11L);
        equipment.setName("Canon Camera");

        User requester = new User();
        requester.setUsername("alice");

        BookingRequest booking = new BookingRequest();
        booking.setId(101L);
        booking.setEquipment(equipment);
        booking.setRequester(requester);
        booking.setStartAt(LocalDateTime.of(2025, 1, 10, 10, 0));
        booking.setEndAt(LocalDateTime.of(2025, 1, 12, 10, 0));
        booking.setQuantityRequested(2);
        booking.setStatus(BookingStatus.PENDING);
        booking.setAdminNote("awaiting approval");
        booking.setCreatedAt(LocalDateTime.of(2025, 1, 9, 9, 0));
        booking.setUpdatedAt(LocalDateTime.of(2025, 1, 9, 10, 0));

        // Act
        BookingRequestDTO dto = BookingMapper.toDTO(booking);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(101L);
        assertThat(dto.getEquipmentId()).isEqualTo(11L);
        assertThat(dto.getEquipmentName()).isEqualTo("Canon Camera");
        assertThat(dto.getRequesterUsername()).isEqualTo("alice");
        assertThat(dto.getStartAt()).isEqualTo(LocalDateTime.of(2025, 1, 10, 10, 0));
        assertThat(dto.getEndAt()).isEqualTo(LocalDateTime.of(2025, 1, 12, 10, 0));
        assertThat(dto.getQuantityRequested()).isEqualTo(2);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(dto.getAdminNote()).isEqualTo("awaiting approval");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 9, 9, 0));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 9, 10, 0));
    }

    @Test
    void toDTO_handlesNullEquipmentAndRequesterGracefully() {
        // Arrange
        BookingRequest booking = new BookingRequest();
        booking.setId(202L);
        booking.setEquipment(null);
        booking.setRequester(null);
        booking.setStartAt(LocalDateTime.of(2025, 2, 1, 12, 0));
        booking.setEndAt(LocalDateTime.of(2025, 2, 2, 12, 0));
        booking.setQuantityRequested(3);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setAdminNote("no stock");
        booking.setCreatedAt(LocalDateTime.of(2025, 2, 1, 11, 0));
        booking.setUpdatedAt(LocalDateTime.of(2025, 2, 1, 12, 0));

        // Act
        BookingRequestDTO dto = BookingMapper.toDTO(booking);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getEquipmentId()).isNull();
        assertThat(dto.getEquipmentName()).isNull();
        assertThat(dto.getRequesterUsername()).isNull();
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(dto.getAdminNote()).isEqualTo("no stock");
    }
}

