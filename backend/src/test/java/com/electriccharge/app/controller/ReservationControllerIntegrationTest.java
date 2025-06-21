package com.electriccharge.app.controller;

import com.electriccharge.app.dto.ReservationDto;
import com.electriccharge.app.model.ChargingStation;
import com.electriccharge.app.model.Utilisateur;
import com.electriccharge.app.repository.ChargingStationRepository;
import com.electriccharge.app.repository.ReservationRepository;
import com.electriccharge.app.repository.UtilisateurRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.eb.electricitybusiness.ElectricityBusinessApplication.class)
@AutoConfigureMockMvc
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Utilisateur testUser;
    private ChargingStation testStation;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        chargingStationRepository.deleteAll();
        utilisateurRepository.deleteAll();

        testUser = new Utilisateur();
        testUser.setNom("Test");
        testUser.setPrenom("User");
        testUser.setPseudo("testuser");
        testUser.setEmail("test@user.com");
        testUser.setMotDePasse(passwordEncoder.encode("password"));
        testUser.setRole(Utilisateur.Role.client);
        testUser = utilisateurRepository.save(testUser);

        testStation = new ChargingStation();
        testStation.setNumero("ST001");
        testStation.setNom("Station 1");
        testStation.setLocalisation("Grenoble");
        testStation.setLatitude(45.18);
        testStation.setLongitude(5.72);
        testStation.setPuissance(22);
        testStation.setPrixALaMinute(new BigDecimal("0.45"));
        testStation.setConnectorType("Type 2");
        testStation.setEtat(ChargingStation.Etat.DISPONIBLE);
        testStation.setOwner(testUser);
        testStation.setAddress("1 Rue Test");
        testStation.setHourlyRate(new BigDecimal("15"));
        testStation = chargingStationRepository.save(testStation);
    }

    @Test
    @WithMockUser
    void whenCreateReservation_thenSuccess() throws Exception {
        ReservationDto dto = new ReservationDto();
        dto.setUtilisateurId(testUser.getIdUtilisateur());
        dto.setChargingStationId(testStation.getIdBorne());
        dto.setDateDebut(LocalDateTime.now().plusHours(1));
        dto.setDateFin(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.utilisateurId").value(testUser.getIdUtilisateur()));
    }

    @Test
    @WithMockUser
    void whenGetByUser_thenReturnList() throws Exception {
        // create one reservation first
        ReservationDto dto = new ReservationDto();
        dto.setUtilisateurId(testUser.getIdUtilisateur());
        dto.setChargingStationId(testStation.getIdBorne());
        dto.setDateDebut(LocalDateTime.now().plusHours(1));
        dto.setDateFin(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/reservations/utilisateur/" + testUser.getIdUtilisateur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
} 