package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.dto.ChargingStationDto;
import com.eb.electricitybusiness.model.ChargingStation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.ChargingStationRepository;
import com.eb.electricitybusiness.repository.UtilisateurRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.eb.electricitybusiness.ElectricityBusinessApplication.class)
@AutoConfigureMockMvc
class BorneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ChargingStation testBorne;
    private Utilisateur testAdmin;

    @BeforeEach
    void setup() {
        // Clean up existing test data
        chargingStationRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Create test admin user
        testAdmin = new Utilisateur();
        testAdmin.setNom("Admin Test");
        testAdmin.setPrenom("Test");
        testAdmin.setPseudo("admintest");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setMotDePasse(passwordEncoder.encode("password"));
        testAdmin.setRole(Utilisateur.Role.admin);
        testAdmin = utilisateurRepository.save(testAdmin);

        // Create test charging station
        testBorne = new ChargingStation();
        testBorne.setNom("Test Borne");  // This already maps to the name column
        testBorne.setNumero("B001");
        testBorne.setLocalisation("Test Location");
        testBorne.setLatitude(45.0);
        testBorne.setLongitude(5.0);
        testBorne.setPuissance(22);
        testBorne.setEtat(ChargingStation.Etat.DISPONIBLE);
        testBorne.setOccupee(false);
        testBorne.setPrixALaMinute(new BigDecimal("2.50"));
        testBorne.setConnectorType("2S");
        testBorne.setDescription("Test charging station");
        testBorne.setOwner(testAdmin);
        testBorne.setAddress("123 Test Street");
        testBorne.setHourlyRate(new BigDecimal("15.00"));
        testBorne.setPowerOutput(22.0);
        testBorne = chargingStationRepository.save(testBorne);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateBorne_thenReturnCreatedBorne() throws Exception {
        // Arrange
        ChargingStationDto dto = new ChargingStationDto();
        dto.setNom("New Test Borne");
        dto.setNumero("B002");
        dto.setLocalisation("New Test Location");
        dto.setLatitude(46.0);
        dto.setLongitude(6.0);
        dto.setPuissance(50);
        dto.setEtat("DISPONIBLE");
        dto.setOccupee(false);
        dto.setPrixALaMinute(new BigDecimal("3.50"));
        dto.setConnectorType("2S");
        dto.setDescription("New test charging station");
        dto.setAddress("456 New Test Street");
        dto.setHourlyRate(new BigDecimal("20.00"));  // Adding hourly rate
        dto.setOwnerId(testAdmin.getIdUtilisateur());

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/bornes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nom").value(dto.getNom()))
                .andReturn();

        // Additional assertions if needed
    }

    @Test
    @WithMockUser
    void whenGetAllBornes_thenReturnBornesList() throws Exception {
        mockMvc.perform(get("/bornes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    void whenGetBorneById_withValidId_thenReturnBorne() throws Exception {
        mockMvc.perform(get("/bornes/" + testBorne.getIdBorne()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nom").value(testBorne.getNom()));
    }

    @Test
    @WithMockUser
    void whenGetBorneById_withInvalidId_thenReturn404() throws Exception {
        mockMvc.perform(get("/bornes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenToggleOccupation_thenUpdateOccupationStatus() throws Exception {
        mockMvc.perform(put("/bornes/" + testBorne.getIdBorne() + "/occupation")
                .param("occupee", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.occupee").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenChangerEtat_thenUpdateEtat() throws Exception {
        // Create a test charging station
        ChargingStation testStation = new ChargingStation();
        testStation.setNom("Test Station");
        testStation.setNumero("TEST123");
        testStation.setLocalisation("Test Location");
        testStation.setLatitude(45.0);
        testStation.setLongitude(5.0);
        testStation.setPuissance(50);
        testStation.setConnectorType("2S");
        testStation.setEtat(ChargingStation.Etat.DISPONIBLE);
        testStation.setOccupee(false);
        testStation.setPrixALaMinute(new BigDecimal("2.50"));
        testStation.setAddress("123 Test St");
        testStation.setOwner(testAdmin);
        testStation = chargingStationRepository.save(testStation);

        mockMvc.perform(put("/bornes/" + testStation.getIdBorne() + "/etat")
                .param("nouvelEtat", "EN_PANNE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.etat").value("EN_PANNE"));
    }

    @Test
    void whenGetBornesProches_thenReturnNearbyStations() throws Exception {
        mockMvc.perform(get("/bornes/public/proches")
                .param("latitude", "45.0")
                .param("longitude", "5.0")
                .param("distance", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
