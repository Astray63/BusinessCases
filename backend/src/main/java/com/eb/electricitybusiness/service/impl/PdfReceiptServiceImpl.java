package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.model.Reservation;
import com.eb.electricitybusiness.service.PdfReceiptService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptServiceImpl implements PdfReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(PdfReceiptServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${app.receipts.storage.path:receipts}")
    private String receiptsStoragePath;

    @Override
    public String generateReceipt(Reservation reservation) throws IOException {
        logger.info("Génération du reçu PDF pour la réservation #{}", reservation.getNumeroReservation());

        try {
            // Créer le répertoire de stockage s'il n'existe pas
            Path storagePath = Paths.get(receiptsStoragePath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // Nom du fichier PDF
            String fileName = String.format("recu_reservation_%d_%d.pdf",
                    reservation.getNumeroReservation(),
                    System.currentTimeMillis());
            String fullPath = Paths.get(receiptsStoragePath, fileName).toString();

            // Créer le document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fullPath)));
            document.open();

            // Ajouter le contenu
            addHeader(document);
            addReservationDetails(document, reservation);
            addFooter(document);

            document.close();

            logger.info("Reçu PDF généré avec succès : {}", fullPath);
            return fullPath;

        } catch (DocumentException e) {
            logger.error("Erreur lors de la génération du PDF pour la réservation #{}", 
                reservation.getNumeroReservation(), e);
            throw new IOException("Erreur lors de la génération du reçu PDF", e);
        }
    }

    @Override
    public byte[] getReceiptContent(String receiptPath) throws IOException {
        Path path = Paths.get(receiptPath);
        if (!Files.exists(path)) {
            throw new IOException("Le fichier du reçu n'existe pas : " + receiptPath);
        }
        return Files.readAllBytes(path);
    }

    private void addHeader(Document document) throws DocumentException {
        // Logo / Titre de l'entreprise
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("ElectricCharge", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
        Paragraph subtitle = new Paragraph("Reçu de réservation", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(30);
        document.add(subtitle);

        // Ligne de séparation
        addSeparator(document);
    }

    private void addReservationDetails(Document document, Reservation reservation) throws DocumentException {
        // Informations de la réservation
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

        // Numéro de réservation
        Paragraph reservationNumber = new Paragraph("Réservation n° " + reservation.getNumeroReservation(), sectionFont);
        reservationNumber.setSpacingBefore(20);
        reservationNumber.setSpacingAfter(15);
        document.add(reservationNumber);

        // Table des informations client
        addClientInfo(document, reservation, normalFont);

        // Table des informations de la borne
        addStationInfo(document, reservation, normalFont);

        // Table des détails de la réservation
        addReservationInfo(document, reservation, normalFont);

        // Montant total
        addTotalAmount(document, reservation);
    }

    private void addClientInfo(Document document, Reservation reservation, Font font) throws DocumentException {
        Paragraph clientTitle = new Paragraph("Informations client", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
        clientTitle.setSpacingBefore(10);
        clientTitle.setSpacingAfter(8);
        document.add(clientTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableRow(table, "Nom", 
            reservation.getUtilisateur().getPrenom() + " " + reservation.getUtilisateur().getNom(), font);
        addTableRow(table, "Email", reservation.getUtilisateur().getEmail(), font);
        if (reservation.getUtilisateur().getPseudo() != null) {
            addTableRow(table, "Pseudo", reservation.getUtilisateur().getPseudo(), font);
        }

        document.add(table);
    }

    private void addStationInfo(Document document, Reservation reservation, Font font) throws DocumentException {
        Paragraph stationTitle = new Paragraph("Borne de recharge", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
        stationTitle.setSpacingBefore(10);
        stationTitle.setSpacingAfter(8);
        document.add(stationTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableRow(table, "Nom", reservation.getChargingStation().getNom(), font);
        addTableRow(table, "Localisation", reservation.getChargingStation().getLocalisation(), font);
        addTableRow(table, "Numéro", reservation.getChargingStation().getNumero(), font);
        addTableRow(table, "Puissance", reservation.getChargingStation().getPuissance() + " kW", font);
        if (reservation.getChargingStation().getConnectorType() != null) {
            addTableRow(table, "Type de connecteur", 
                reservation.getChargingStation().getConnectorType(), font);
        }

        document.add(table);
    }

    private void addReservationInfo(Document document, Reservation reservation, Font font) throws DocumentException {
        Paragraph reservationTitle = new Paragraph("Détails de la réservation", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY));
        reservationTitle.setSpacingBefore(10);
        reservationTitle.setSpacingAfter(8);
        document.add(reservationTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableRow(table, "Date de début", 
            reservation.getDateDebut().format(DATE_FORMATTER), font);
        addTableRow(table, "Date de fin", 
            reservation.getDateFin().format(DATE_FORMATTER), font);
        addTableRow(table, "Prix à la minute", 
            formatMontant(reservation.getPrixALaMinute()) + " €", font);
        addTableRow(table, "Statut", 
            getStatutLabel(reservation.getEtat().name()), font);

        document.add(table);
    }

    private void addTotalAmount(Document document, Reservation reservation) throws DocumentException {
        addSeparator(document);

        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(20);
        totalTable.setSpacingAfter(20);

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        
        PdfPCell labelCell = new PdfPCell(new Phrase("MONTANT TOTAL", totalFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(20);
        totalTable.addCell(labelCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(
            formatMontant(reservation.getTotalPrice()) + " €", totalFont));
        amountCell.setBorder(Rectangle.NO_BORDER);
        amountCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        amountCell.setPaddingLeft(20);
        totalTable.addCell(amountCell);

        document.add(totalTable);
    }

    private void addFooter(Document document) throws DocumentException {
        addSeparator(document);

        Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph(
            "Merci d'avoir utilisé nos services ElectricCharge.\n" +
            "Pour toute question, contactez-nous à support@electriccharge.com", 
            footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font font) {
        Font boldFont = new Font(font.getFamily(), font.getSize(), Font.BOLD, font.getColor());
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label + " :", boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);
        labelCell.setPaddingTop(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        valueCell.setPaddingTop(5);
        table.addCell(valueCell);
    }

    private void addSeparator(Document document) throws DocumentException {
        Paragraph separator = new Paragraph();
        separator.setSpacingBefore(10);
        separator.setSpacingAfter(10);
        
        com.itextpdf.text.pdf.draw.LineSeparator line = 
            new com.itextpdf.text.pdf.draw.LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        separator.add(new Chunk(line));
        
        document.add(separator);
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) return "0.00";
        return String.format("%.2f", montant);
    }

    private String getStatutLabel(String etat) {
        switch (etat) {
            case "ACTIVE":
            case "CONFIRMEE":
                return "Confirmée";
            case "EN_ATTENTE":
                return "En attente";
            case "TERMINEE":
                return "Terminée";
            case "ANNULEE":
                return "Annulée";
            case "REFUSEE":
                return "Refusée";
            default:
                return etat;
        }
    }
}
