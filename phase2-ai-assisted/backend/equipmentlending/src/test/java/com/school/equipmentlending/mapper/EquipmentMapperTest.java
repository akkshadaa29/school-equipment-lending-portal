package com.school.equipmentlending.mapper;

import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.dto.EquipmentRequest;
import com.school.equipmentlending.model.Equipment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EquipmentMapperTest {

    @Test
    void toDTO_copiesFieldsCorrectly() {
        Equipment e = new Equipment();
        e.setId(101L);
        e.setName("Zoom Camera");
        e.setCategory("Photo");
        e.setCondition("NEW");
        e.setQuantity(7);
        e.setAvailable(true);
        LocalDateTime now = LocalDateTime.of(2023, 1, 1, 10, 0);
        e.setCreatedAt(now);

        EquipmentDTO dto = EquipmentMapper.toDTO(e);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(101L);
        assertThat(dto.getName()).isEqualTo("Zoom Camera");
        assertThat(dto.getCategory()).isEqualTo("Photo");
        assertThat(dto.getCondition()).isEqualTo("NEW");
        assertThat(dto.getQuantity()).isEqualTo(7);
        assertThat(dto.isAvailable()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        // availableUnits is not set by mapper (service sets it) — ensure default is 0
        assertThat(dto.getAvailableUnits()).isEqualTo(0);
    }

    @Test
    void fromRequest_createsEquipmentWithRequestValues() {
        EquipmentRequest req = new EquipmentRequest();
        req.setName("Speaker");
        req.setCategory("Audio");
        req.setCondition("GOOD");
        req.setQuantity(4);
        req.setAvailable(Boolean.FALSE);

        Equipment e = EquipmentMapper.fromRequest(req);

        assertThat(e).isNotNull();
        assertThat(e.getName()).isEqualTo("Speaker");
        assertThat(e.getCategory()).isEqualTo("Audio");
        assertThat(e.getCondition()).isEqualTo("GOOD");
        assertThat(e.getQuantity()).isEqualTo(4);
        assertThat(e.isAvailable()).isFalse();
    }

    @Test
    void applyUpdate_updatesOnlyNonNullFields() {
        Equipment existing = new Equipment();
        existing.setId(55L);
        existing.setName("OldName");
        existing.setCategory("OldCat");
    }
}