package com.eb.electricitybusiness.controller;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.eb.electricitybusiness.dto.ReservationDto;
import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.repository.BorneRepository;
import com.eb.electricitybusiness.repository.ReservationRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@SuppressWarnings("null")
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BorneRepository borneRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private com.eb.electricitybusiness.repository.LieuRepository lieuRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Utilisateur testUser;
    private Borne testStation;
    private com.eb.electricitybusiness.model.Lieu testLieu;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        borneRepository.deleteAll();
        utilisateurRepository.deleteAll();
        lieuRepository.deleteAll();

        testUser = new Utilisateur();
        testUser.setNom("Test");
        testUser.setPrenom("User");
        testUser.setPseudo("testuser");
        testUser.setEmail("test@user.com");
        testUser.setMotDePasse(passwordEncoder.encode("password"));
        testUser.setRole(Utilisateur.Role.client);
        testUser = utilisateurRepository.save(testUser);

        testLieu = new com.eb.electricitybusiness.model.Lieu();
        testLieu.setAdresse("123 Test St");
        testLieu.setVille("Test City");
        testLieu.setCodePostal("12345");
        testLieu.setNom("Test Lieu");
        testLieu = lieuRepository.save(testLieu);

        testStation = new Borne();
        testStation.setNumero("ST001");
        testStation.setNom("Station 1");
        testStation.setLocalisation("Grenoble");
        testStation.setLatitude(45.18);
        testStation.setLongitude(5.72);
        testStation.setPuissance(22);
        testStation.setPrixALaMinute(new BigDecimal("0.45"));
        testStation.setEtat(Borne.Etat.DISPONIBLE);
        testStation.setOwner(testUser);
        testStation.setLieu(testLieu);
        testStation = borneRepository.save(testStation);
    }

    @Test
    @WithMockUser
    void whenCreateReservation_thenSuccess() throws Exception {
        ReservationDto dto = new ReservationDto();
        dto.setUtilisateurId(testUser.getIdUtilisateur());
        dto.setBorneId(testStation.getIdBorne());
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
        dto.setBorneId(testStation.getIdBorne());
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
