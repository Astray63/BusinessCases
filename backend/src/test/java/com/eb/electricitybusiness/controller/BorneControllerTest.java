package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ChargingStationDto;
import com.eb.electricitybusiness.service.ChargingStationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.eb.electricitybusiness.ElectricityBusinessApplication.class)
@AutoConfigureMockMvc
public class BorneControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ChargingStationService chargingStationService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser(roles = "ADMIN")
        public void testCreateBorne() throws Exception {
                // Arrange
                ChargingStationDto inputDto = new ChargingStationDto();
                inputDto.setNom("Test Borne"); // This maps to the name column
                inputDto.setNumero("B001");
                inputDto.setLocalisation("Test Location");
                inputDto.setLatitude(45.0);
                inputDto.setLongitude(5.0);
                inputDto.setPuissance(22);
                inputDto.setEtat("DISPONIBLE");
                inputDto.setOccupee(false);
                inputDto.setPrixALaMinute(new BigDecimal("2.50"));
                // inputDto.setConnectorType("2S"); // Toujours Type 2S
                inputDto.setHourlyRate(new BigDecimal("15.00")); // Added required field
                inputDto.setOwnerId(1L);

                ChargingStationDto outputDto = new ChargingStationDto();
                outputDto.setId(1L);
                outputDto.setNom(inputDto.getNom());
                // ... other properties

                when(chargingStationService.create(any(ChargingStationDto.class))).thenReturn(outputDto);

                // Act & Assert
                mockMvc.perform(post("/bornes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inputDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.id").exists())
                                .andExpect(jsonPath("$.data.nom").value(inputDto.getNom()));
        }

        @Test
        @WithMockUser
        public void testGetBorneById() throws Exception {
                // Arrange
                Long borneId = 1L;
                ChargingStationDto dto = new ChargingStationDto();
                dto.setId(borneId);
                dto.setNom("Test Borne");

                when(chargingStationService.getById(borneId)).thenReturn(dto);

                // Act & Assert
                mockMvc.perform(get("/bornes/{id}", borneId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.id").value(borneId));
        }

        @Test
        @WithMockUser
        public void testGetAllBornes() throws Exception {
                // Arrange
                List<ChargingStationDto> bornes = Arrays.asList(
                                new ChargingStationDto(),
                                new ChargingStationDto());
                when(chargingStationService.getAll()).thenReturn(bornes);

                // Act & Assert
                mockMvc.perform(get("/bornes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = "PROPRIETAIRE")
        void updateBorne_AsOwner_ReturnsUpdated() throws Exception {
                ChargingStationDto dto = new ChargingStationDto();
                dto.setNom("Updated Borne");
                dto.setNumero("B001");
                dto.setLocalisation("Test Location");
                dto.setLatitude(45.0);
                dto.setLongitude(5.0);
                dto.setPuissance(22);
                dto.setEtat("DISPONIBLE");
                dto.setPrixALaMinute(new BigDecimal("2.50"));
                dto.setHourlyRate(new BigDecimal("15.00"));
                dto.setOwnerId(1L);
                dto.setDescription("Test description");
                dto.setInstructionSurPied("Test instructions");

                when(chargingStationService.update(eq(1L), any(ChargingStationDto.class))).thenReturn(dto);

                mockMvc.perform(put("/bornes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @WithMockUser(roles = "PROPRIETAIRE")
        void createBorne_AsOwner_ReturnsCreated() throws Exception {
                ChargingStationDto dto = new ChargingStationDto();
                dto.setNom("New Borne");
                dto.setNumero("B002");
                dto.setLocalisation("Owner Location");
                dto.setLatitude(46.0);
                dto.setLongitude(6.0);
                dto.setPuissance(50);
                dto.setEtat("DISPONIBLE");
                dto.setOccupee(false);
                dto.setPrixALaMinute(new BigDecimal("3.00"));
                dto.setHourlyRate(new BigDecimal("20.00"));
                dto.setOwnerId(1L);
                dto.setDescription("Owner created description");
                dto.setInstructionSurPied("Owner created instructions");

                when(chargingStationService.create(any(ChargingStationDto.class))).thenReturn(dto);

                mockMvc.perform(post("/bornes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated()) // Expect 201 Created
                                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @WithMockUser(roles = "PROPRIETAIRE") // Changed from ADMIN to PROPRIETAIRE
        public void testUpdateBorne() throws Exception {
                // Arrange
                Long borneId = 1L;
                ChargingStationDto inputDto = new ChargingStationDto();
                inputDto.setNom("Updated Borne");
                inputDto.setNumero("B001");
                inputDto.setLocalisation("Test Location");
                inputDto.setLatitude(45.0);
                inputDto.setLongitude(5.0);
                inputDto.setPuissance(22);
                inputDto.setEtat("DISPONIBLE");
                inputDto.setPrixALaMinute(new BigDecimal("2.50"));
                // inputDto.setConnectorType("2S"); // Removed
                inputDto.setHourlyRate(new BigDecimal("15.00"));
                inputDto.setOwnerId(1L);
                inputDto.setDescription("Test description");
                inputDto.setInstructionSurPied("Test instructions");
                inputDto.setNumero("B001");
                inputDto.setLocalisation("Test Location");
                inputDto.setLatitude(45.0);
                inputDto.setLongitude(5.0);
                inputDto.setPuissance(22);
                inputDto.setEtat("DISPONIBLE");
                inputDto.setPrixALaMinute(new BigDecimal("2.50"));
                // inputDto.setConnectorType("2S"); // Removed
                inputDto.setHourlyRate(new BigDecimal("15.00"));
                inputDto.setOwnerId(1L);
                inputDto.setDescription("Test description"); // Ajout du champ manquant
                inputDto.setInstructionSurPied("Test instructions"); // Ajout du champ manquant

                ChargingStationDto outputDto = new ChargingStationDto();
                outputDto.setId(borneId);
                outputDto.setNom(inputDto.getNom());
                outputDto.setNumero(inputDto.getNumero());
                outputDto.setLocalisation(inputDto.getLocalisation());
                outputDto.setLatitude(inputDto.getLatitude());
                outputDto.setLongitude(inputDto.getLongitude());
                outputDto.setPuissance(inputDto.getPuissance());
                outputDto.setEtat(inputDto.getEtat());
                outputDto.setPrixALaMinute(inputDto.getPrixALaMinute());
                // outputDto.setConnectorType(inputDto.getConnectorType()); // Removed
                outputDto.setHourlyRate(inputDto.getHourlyRate());
                outputDto.setOwnerId(inputDto.getOwnerId());

                when(chargingStationService.update(eq(borneId), any(ChargingStationDto.class)))
                                .thenReturn(outputDto);

                // Act & Assert
                mockMvc.perform(put("/bornes/{id}", borneId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inputDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.nom").value(inputDto.getNom()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        public void testToggleOccupation() throws Exception {
                // Arrange
                Long borneId = 1L;
                boolean newOccupationStatus = true;

                ChargingStationDto outputDto = new ChargingStationDto();
                outputDto.setId(borneId);
                outputDto.setOccupee(newOccupationStatus);

                when(chargingStationService.toggleOccupation(borneId, newOccupationStatus))
                                .thenReturn(outputDto);

                // Act & Assert
                mockMvc.perform(put("/bornes/{id}/occupation", borneId)
                                .param("occupee", String.valueOf(newOccupationStatus)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.occupee").value(newOccupationStatus));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        public void testChangerEtat() throws Exception {
                // Arrange
                Long borneId = 1L;
                String nouvelEtat = "HORS_SERVICE";

                ChargingStationDto outputDto = new ChargingStationDto();
                outputDto.setId(borneId);
                outputDto.setEtat(nouvelEtat);

                when(chargingStationService.changerEtat(borneId, nouvelEtat))
                                .thenReturn(outputDto);

                // Act & Assert
                mockMvc.perform(put("/bornes/{id}/etat", borneId)
                                .param("nouvelEtat", nouvelEtat))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.etat").value(nouvelEtat));
        }
}
