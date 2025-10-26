package com.school.equipmentlending.service;


import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.dto.EquipmentRequest;
import com.school.equipmentlending.exception.ResourceNotFoundException;
import com.school.equipmentlending.model.Equipment;
import com.school.equipmentlending.repository.EquipmentRepository;
import com.school.equipmentlending.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- getAllEquipment ----------

    @Test
    void getAllEquipment_computesAvailability() {
        Equipment e1 = new Equipment();
        e1.setId(1L);
        e1.setName("Camera");
        e1.setQuantity(10);

        Equipment e2 = new Equipment();
        e2.setId(2L);
        e2.setName("Projector");
        e2.setQuantity(2);

        when(equipmentRepository.findAll()).thenReturn(List.of(e1, e2));
        // simulate reserved quantities
        when(loanRepository.sumCurrentlyReserved(eq(1L), any(LocalDateTime.class))).thenReturn(3L);
        when(loanRepository.sumCurrentlyReserved(eq(2L), any(LocalDateTime.class))).thenReturn(2L);

        List<EquipmentDTO> list = equipmentService.getAllEquipment();

        assertThat(list).hasSize(2);
        EquipmentDTO dto1 = list.stream().filter(d -> d.getId().equals(1L)).findFirst().orElse(null);
        EquipmentDTO dto2 = list.stream().filter(d -> d.getId().equals(2L)).findFirst().orElse(null);

        assertThat(dto1).isNotNull();
        assertThat(dto1.getAvailableUnits()).isEqualTo(7); // 10 - 3
        assertThat(dto1.isAvailable()).isTrue();

        assertThat(dto2).isNotNull();
        assertThat(dto2.getAvailableUnits()).isEqualTo(0); // 2 - 2 => 0
        assertThat(dto2.isAvailable()).isFalse();
    }

    // ---------- search ----------

    @Test
    void search_filtersByCategory_andQuery_andAvailability() {
        Equipment eqA = new Equipment();
        eqA.setId(11L);
        eqA.setName("Canon Camera");
        eqA.setCategory("Photo");
        eqA.setQuantity(5);

        Equipment eqB = new Equipment();
        eqB.setId(12L);
        eqB.setName("Epson Projector");
        eqB.setCategory("AV");
        eqB.setQuantity(1);

        when(equipmentRepository.findAll()).thenReturn(List.of(eqA, eqB));
        when(equipmentRepository.findByCategoryIgnoreCase("Photo")).thenReturn(List.of(eqA));
        // Use matchers for all args — wrap the id with eq(...)
        when(loanRepository.sumCurrentlyReserved(eq(eqA.getId()), any(LocalDateTime.class))).thenReturn(0L);
        when(loanRepository.sumCurrentlyReserved(eq(eqB.getId()), any(LocalDateTime.class))).thenReturn(1L);

        // search by category "Photo" -> returns eqA
        List<EquipmentDTO> byCategory = equipmentService.search(null, "Photo", null);
        assertThat(byCategory).hasSize(1);
        assertThat(byCategory.get(0).getId()).isEqualTo(11L);

        // search by availability = true -> only eqA (availableUnits = 5)
        List<EquipmentDTO> available = equipmentService.search(null, null, true);
        assertThat(available).extracting(EquipmentDTO::getId).contains(11L).doesNotContain(12L);

        // search by text query "projector" (case-insensitive) -> returns eqB from findAll path
        List<EquipmentDTO> byQuery = equipmentService.search("projector", null, null);
        assertThat(byQuery).extracting(EquipmentDTO::getId).contains(12L);
    }


    // ---------- getById ----------

    @Test
    void getById_existing_returnsDtoWithAvailability() {
        Equipment e = new Equipment();
        e.setId(21L);
        e.setName("Mic");
        e.setQuantity(4);

        when(equipmentRepository.findById(21L)).thenReturn(Optional.of(e));
        when(loanRepository.sumCurrentlyReserved(eq(21L), any(LocalDateTime.class))).thenReturn(1L);

        EquipmentDTO dto = equipmentService.getById(21L);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(21L);
        assertThat(dto.getAvailableUnits()).isEqualTo(3);
        assertThat(dto.isAvailable()).isTrue();
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> equipmentService.getById(999L));
    }

    // ---------- createEquipment ----------

    @Test
    void createEquipment_savesAndReturnsDto() {
        EquipmentRequest req = new EquipmentRequest();
        req.setName("Speaker");
        req.setCategory("Audio");
        req.setQuantity(6);

        // saved equipment returned by repository (simulate mapper -> save)
        Equipment saved = new Equipment();
        saved.setId(51L);
        saved.setName("Speaker");
        saved.setCategory("Audio");
        saved.setQuantity(6);

        // when saving any Equipment, return our saved instance
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(saved);
        when(loanRepository.sumCurrentlyReserved(eq(51L), any(LocalDateTime.class))).thenReturn(0L);

        EquipmentDTO dto = equipmentService.createEquipment(req);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(51L);
        assertThat(dto.getAvailableUnits()).isEqualTo(6);
        assertThat(dto.isAvailable()).isTrue();

        verify(equipmentRepository).save(any(Equipment.class));
    }

    // ---------- updateEquipment ----------

    @Test
    void updateEquipment_notFound_throwsResourceNotFound() {
        when(equipmentRepository.findById(123L)).thenReturn(Optional.empty());
        EquipmentRequest req = new EquipmentRequest();
        assertThrows(ResourceNotFoundException.class, () -> equipmentService.updateEquipment(123L, req));
    }

    @Test
    void updateEquipment_existing_appliesUpdateAndReturnsDto() {
        Equipment existing = new Equipment();
        existing.setId(60L);
        existing.setName("Old");
        existing.setCategory("Cat");
        existing.setQuantity(2);

        when(equipmentRepository.findById(60L)).thenReturn(Optional.of(existing));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepository.sumCurrentlyReserved(eq(60L), any(LocalDateTime.class))).thenReturn(0L);

        EquipmentRequest req = new EquipmentRequest();
        req.setName("NewName");
        req.setCategory("NewCat");
        req.setQuantity(10);

        EquipmentDTO dto = equipmentService.updateEquipment(60L, req);
        assertThat(dto.getId()).isEqualTo(60L);
        assertThat(dto.getName()).isEqualTo("NewName");
        assertThat(dto.getCategory()).isEqualTo("NewCat");
        assertThat(dto.getAvailableUnits()).isEqualTo(10);

        verify(equipmentRepository).save(existing);
    }

    // ---------- deleteEquipment ----------

    @Test
    void deleteEquipment_notFound_throwsResourceNotFound() {
        when(equipmentRepository.existsById(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> equipmentService.deleteEquipment(999L));
    }

    @Test
    void deleteEquipment_existing_deletesById() {
        when(equipmentRepository.existsById(5L)).thenReturn(true);
        doNothing().when(equipmentRepository).deleteById(5L);

        equipmentService.deleteEquipment(5L);

        verify(equipmentRepository).deleteById(5L);
    }
}

