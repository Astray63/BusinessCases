package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.BorneDto;
import com.eb.electricitybusiness.service.BorneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@SuppressWarnings("null")
public class BorneControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BorneService borneService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser(roles = "ADMIN")
        public void testCreateBorne() throws Exception {
                // Arrange
                BorneDto inputDto = new BorneDto();
                inputDto.setNom("Test Borne");
                inputDto.setNumero("B001");
                inputDto.setLocalisation("Test Location");
                inputDto.setLatitude(45.0);
                inputDto.setLongitude(5.0);
                inputDto.setPuissance(22);
                inputDto.setEtat("DISPONIBLE");
                inputDto.setOccupee(false);
                inputDto.setPrixALaMinute(new BigDecimal("2.50"));
                inputDto.setOwnerId(1L);
                inputDto.setLieuId(1L);

                BorneDto outputDto = new BorneDto();
                outputDto.setId(1L);
                outputDto.setNom(inputDto.getNom());
                // ... other properties

                when(borneService.create(any(BorneDto.class))).thenReturn(outputDto);

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
                BorneDto borneDto = new BorneDto();
                borneDto.setId(borneId);
                borneDto.setNom("Test Borne");
                borneDto.setIdBorne(borneId);

                when(borneService.getBorneDtoById(borneId)).thenReturn(borneDto);

                // Act & Assert
                mockMvc.perform(get("/bornes/{id}", borneId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.idBorne").value(borneId));
        }

        @Test
        @WithMockUser
        public void testGetAllBornes() throws Exception {
                // Arrange
                List<BorneDto> bornes = Arrays.asList(
                                new BorneDto(),
                                new BorneDto());
                when(borneService.getAllBornesDto()).thenReturn(bornes);

                // Act & Assert
                mockMvc.perform(get("/bornes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = "PROPRIETAIRE")
        void updateBorne_AsOwner_ReturnsUpdated() throws Exception {
                BorneDto dto = new BorneDto();
                dto.setNom("Updated Borne");
                dto.setNumero("B001");
                dto.setLocalisation("Test Location");
                dto.setLatitude(45.0);
                dto.setLongitude(5.0);
                dto.setPuissance(22);
                dto.setEtat("DISPONIBLE");
                dto.setPrixALaMinute(new BigDecimal("2.50"));
                dto.setOwnerId(1L);
                dto.setLieuId(1L);
                dto.setDescription("Test description");
                dto.setInstructionSurPied("Test instructions");

                when(borneService.update(eq(1L), any(BorneDto.class))).thenReturn(dto);

                mockMvc.perform(put("/bornes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @WithMockUser(roles = "PROPRIETAIRE")
        void createBorne_AsOwner_ReturnsCreated() throws Exception {
                BorneDto dto = new BorneDto();
                dto.setNom("New Borne");
                dto.setNumero("B002");
                dto.setLocalisation("Owner Location");
                dto.setLatitude(46.0);
                dto.setLongitude(6.0);
                dto.setPuissance(50);
                dto.setEtat("DISPONIBLE");
                dto.setOccupee(false);
                dto.setPrixALaMinute(new BigDecimal("3.00"));
                dto.setOwnerId(1L);
                dto.setLieuId(1L);
                dto.setDescription("Owner created description");
                dto.setInstructionSurPied("Owner created instructions");

                when(borneService.create(any(BorneDto.class))).thenReturn(dto);

                mockMvc.perform(post("/bornes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated()) // Expect 201 Created
                                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @WithMockUser(roles = "PROPRIETAIRE")
        public void testUpdateBorne() throws Exception {
                // Arrange
                Long borneId = 1L;
                BorneDto inputDto = new BorneDto();
                inputDto.setNom("Updated Borne");
                inputDto.setNumero("B001");
                inputDto.setLocalisation("Test Location");
                inputDto.setLatitude(45.0);
                inputDto.setLongitude(5.0);
                inputDto.setPuissance(22);
                inputDto.setEtat("DISPONIBLE");
                inputDto.setPrixALaMinute(new BigDecimal("2.50"));
                inputDto.setOwnerId(1L);
                inputDto.setLieuId(1L);
                inputDto.setDescription("Test description");
                inputDto.setInstructionSurPied("Test instructions");

                BorneDto outputDto = new BorneDto();
                outputDto.setId(borneId);
                outputDto.setNom(inputDto.getNom());
                outputDto.setNumero(inputDto.getNumero());
                outputDto.setLocalisation(inputDto.getLocalisation());
                outputDto.setLatitude(inputDto.getLatitude());
                outputDto.setLongitude(inputDto.getLongitude());
                outputDto.setPuissance(inputDto.getPuissance());
                outputDto.setEtat(inputDto.getEtat());
                outputDto.setPrixALaMinute(inputDto.getPrixALaMinute());
                outputDto.setOwnerId(inputDto.getOwnerId());

                when(borneService.update(eq(borneId), any(BorneDto.class)))
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

                BorneDto outputDto = new BorneDto();
                outputDto.setId(borneId);
                outputDto.setOccupee(newOccupationStatus);

                when(borneService.toggleOccupation(borneId, newOccupationStatus))
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

                BorneDto outputDto = new BorneDto();
                outputDto.setId(borneId);
                outputDto.setEtat(nouvelEtat);

                when(borneService.changerEtat(borneId, nouvelEtat))
                                .thenReturn(outputDto);

                // Act & Assert
                mockMvc.perform(put("/bornes/{id}/etat", borneId)
                                .param("nouvelEtat", nouvelEtat))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.etat").value(nouvelEtat));
        }
}
