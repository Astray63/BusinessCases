package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Borne;
import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.model.Utilisateur;
import com.eb.electricitybusiness.service.impl.PdfReceiptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PdfReceiptServiceTest {

    @InjectMocks
    private PdfReceiptServiceImpl pdfReceiptService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pdfReceiptService, "receiptsStoragePath", tempDir.toString());
    }

    @Test
    void generateReceipt_Success() throws IOException {
        Reservation reservation = new Reservation();
        reservation.setNumeroReservation(100L);
        reservation.setDateDebut(LocalDateTime.now());
        reservation.setDateFin(LocalDateTime.now().plusHours(1));
        reservation.setPrixALaMinute(new BigDecimal("0.50"));
        reservation.setTotalPrice(new BigDecimal("30.00"));
        reservation.setEtat(Reservation.EtatReservation.TERMINEE);

        Utilisateur u = new Utilisateur();
        u.setNom("Doe");
        u.setPrenom("John");
        u.setEmail("john@test.com");
        u.setPseudo("johndoe");
        reservation.setUtilisateur(u);

        Borne b = new Borne();
        b.setNom("Borne 1");
        b.setNumero("B-001");
        b.setLocalisation("Adress");
        b.setPuissance(22);
        reservation.setBorne(b);

        String path = pdfReceiptService.generateReceipt(reservation);

        assertNotNull(path);
        assertTrue(Files.exists(Path.of(path)));
    }

    @Test
    void getReceiptContent_Exists_ReturnsBytes() throws IOException {
        Path file = tempDir.resolve("test.pdf");
        Files.write(file, "content".getBytes());

        byte[] content = pdfReceiptService.getReceiptContent(file.toString());
        assertArrayEquals("content".getBytes(), content);
    }

    @Test
    void getReceiptContent_NotExists_ThrowsIOException() {
        Path file = tempDir.resolve("nonexistent.pdf");
        assertThrows(IOException.class, () -> pdfReceiptService.getReceiptContent(file.toString()));
    }
}
