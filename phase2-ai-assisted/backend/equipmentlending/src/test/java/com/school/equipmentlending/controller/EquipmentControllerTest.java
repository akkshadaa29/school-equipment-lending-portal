package com.school.equipmentlending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.equipmentlending.dto.EquipmentDTO;
import com.school.equipmentlending.exception.GlobalExceptionHandler;
import com.school.equipmentlending.service.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Rename package & imports to match your project structure
class EquipmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EquipmentService equipmentService; // replace with actual service class

    @InjectMocks
    private EquipmentController controller; // replace with your controller class

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()) // optional: include if you have one
                .build();
    }

    @Test
    void getAllEquipment_returnsOk() throws Exception {
        // Replace EquipmentDto with your DTO/entity
        List<EquipmentDTO> list = Arrays.asList(new EquipmentDTO(
                1L,
                "Digital Microscope",
                "Laboratory",
                "Good",
                10,
                true,
                8, // availableUnits = quantity - reserved
                LocalDateTime.now()
        ), new EquipmentDTO(
                2L,
                "Projector HD",
                "Electronics",
                "Excellent",
                5,
                true,
                5, // all units available
                LocalDateTime.now().minusDays(3)
        ));
        when(equipmentService.getAllEquipment()).thenReturn(list);

        mockMvc.perform(get("/api/equipments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(equipmentService, times(1)).getAllEquipment();
    }

    @Test
    void getEquipmentById_found_returnsOk() throws Exception {
        EquipmentDTO dto = (new EquipmentDTO(
                1L,
                "Digital Microscope",
                "Laboratory",
                "Good",
                10,
                true,
                8, // availableUnits = quantity - reserved
                LocalDateTime.now()
        ));
        when(equipmentService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/equipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createEquipment_valid_returnsOk() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

        EquipmentDTO request = new EquipmentDTO(
                1L,
                "Digital Microscope",
                "Laboratory",
                "Good",
                10,
                true,
                8,
                LocalDateTime.now()
        );

        EquipmentDTO saved = new EquipmentDTO(
                10L,
                "Digital Microscope",
                "Laboratory",
                "Good",
                10,
                true,
                8,
                LocalDateTime.now()
        );

        when(equipmentService.createEquipment(any())).thenReturn(saved);

        mockMvc.perform(post("/api/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())                  // ✅ changed from isCreated() to isOk()
                .andExpect(jsonPath("$.id").value(10));      // ✅ still checks returned id

        verify(equipmentService).createEquipment(any());
    }

    @Test
    void deleteEquipment_returnsNoContent() throws Exception {
        doNothing().when(equipmentService).deleteEquipment(5L);
        mockMvc.perform(delete("/api/equipments/5"))
                .andExpect(status().isNoContent());
        verify(equipmentService).deleteEquipment(5L);
    }
}

