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
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(locations = "classpath:application-test.yml")
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
    private Utilisateur testUser; // Renamed from testAdmin to testUser

    @BeforeEach
    void setup() {
        // Clean up existing test data
        chargingStationRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Create test user (formerly admin)
        testUser = new Utilisateur();
        testUser.setNom("Test");
        testUser.setPrenom("User");
        testUser.setPseudo("testuser");
        testUser.setEmail("user@test.com");
        testUser.setMotDePasse(passwordEncoder.encode("password"));
        testUser.setRole(Utilisateur.Role.client); // Changed role to client
        testUser = utilisateurRepository.save(testUser);

        // Create test charging station
        testBorne = new ChargingStation();
        testBorne.setNom("Test Borne"); // This already maps to the name column
        testBorne.setNumero("B001");
        testBorne.setLocalisation("Test Location");
        testBorne.setLatitude(45.0);
        testBorne.setLongitude(5.0);
        testBorne.setPuissance(22);
        testBorne.setEtat(ChargingStation.Etat.DISPONIBLE);
        testBorne.setOccupee(false);
        testBorne.setPrixALaMinute(new BigDecimal("2.50"));
        testBorne.setDescription("Test charging station");
        testBorne.setOwner(testUser); // Changed from testAdmin to testUser
        testBorne = chargingStationRepository.save(testBorne);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "PROPRIETAIRE")
    void createBorne_ValidData_ReturnsCreated() throws Exception {
        ChargingStationDto dto = new ChargingStationDto();
        dto.setNom("New Borne");
        dto.setLocalisation("New Location");
        dto.setLatitude(48.8566);
        dto.setLongitude(2.3522);
        dto.setPuissance(22);
        dto.setPrixALaMinute(new BigDecimal("0.25")); // Ensure BigDecimal for price
        dto.setOwnerId(testUser.getIdUtilisateur());
        dto.setNumero("B-NEW-001");
        dto.setEtat("DISPONIBLE");

        mockMvc.perform(post("/bornes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nom").value(dto.getNom()));
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
    @WithMockUser(username = "testuser")
    void getAllBornes_ReturnsList() throws Exception {
        // Create a test charging station
        ChargingStation testStation = new ChargingStation();
        testStation.setNom("Test Station");
        testStation.setNumero("TEST123");
        testStation.setLocalisation("Test Location");
        testStation.setLatitude(45.0);
        testStation.setLongitude(5.0);
        testStation.setPuissance(50);
        testStation.setEtat(ChargingStation.Etat.DISPONIBLE);
        testStation.setOccupee(false);
        testStation.setPrixALaMinute(new BigDecimal("2.50"));
        testStation.setOwner(testUser);
        testStation = chargingStationRepository.save(testStation);

        mockMvc.perform(get("/bornes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nom").value(testBorne.getNom()))
                .andExpect(jsonPath("$.data[1].nom").value(testStation.getNom()));
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
        testStation.setEtat(ChargingStation.Etat.DISPONIBLE);
        testStation.setOccupee(false);
        testStation.setPrixALaMinute(new BigDecimal("2.50"));
        testStation.setOwner(testUser);
        testStation = chargingStationRepository.save(testStation);

        mockMvc.perform(put("/bornes/" + testStation.getIdBorne() + "/etat")
                .param("nouvelEtat", "EN_PANNE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.etat").value("EN_PANNE"));
    }

    @Test
    void whenGetBornesProches_thenReturnNearbyStations() throws Exception {
        mockMvc.perform(get("/bornes/proches")
                .param("latitude", "45.0")
                .param("longitude", "5.0")
                .param("distance", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "testproprietaire", roles = "PROPRIETAIRE")
    void updateBorne_AsOwner_ReturnsUpdated() throws Exception {
        // Arrange
        ChargingStationDto dto = new ChargingStationDto();
        dto.setNom("Updated Borne Name");
        dto.setNumero("B001-UPDATED");
        dto.setLocalisation("Updated Location");
        dto.setLatitude(45.1);
        dto.setLongitude(5.1);
        dto.setPuissance(22);
        dto.setEtat("DISPONIBLE");
        dto.setOccupee(false);
        dto.setPrixALaMinute(new BigDecimal("3.00"));
        dto.setInstructionSurPied("Instructions");
        // dto.setConnectorType("2S");
        dto.setDescription("Description");
        dto.setDescription("Updated description");
        dto.setOwnerId(testUser.getIdUtilisateur());

        // Act & Assert
        mockMvc.perform(put("/bornes/" + testBorne.getIdBorne())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nom").value("Updated Borne Name"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "PROPRIETAIRE")
    void deleteBorne_AsOwner_ReturnsSuccess() throws Exception {
        // Ensure testUser is the owner (already set in setUp but good to be explicit if
        // needed)
        testBorne.setOwner(testUser);
        chargingStationRepository.save(testBorne);

        mockMvc.perform(delete("/bornes/" + testBorne.getIdBorne()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));

        // Verify it's gone
        assertFalse(chargingStationRepository.existsById(testBorne.getIdBorne()));
    }

    @Test
    @WithMockUser
    void whenSearchBornes_thenReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/bornes/search")
                .param("prixMax", "200.00")
                .param("puissanceMin", "20")
                .param("etat", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testBorne.getIdBorne()));
    }
}
