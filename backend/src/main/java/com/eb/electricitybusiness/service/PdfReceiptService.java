package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Reservation;

import java.io.IOException;

public interface PdfReceiptService {
    /**
     * Génère un reçu PDF pour une réservation acceptée
     * @param reservation La réservation pour laquelle générer le reçu
     * @return Le chemin du fichier PDF généré
     * @throws IOException Si une erreur survient lors de la génération
     */
    String generateReceipt(Reservation reservation) throws IOException;
    
    /**
     * Récupère le contenu binaire d'un reçu PDF
     * @param receiptPath Le chemin du fichier PDF
     * @return Le contenu du PDF en bytes
     * @throws IOException Si le fichier n'existe pas ou ne peut être lu
     */
    byte[] getReceiptContent(String receiptPath) throws IOException;
}
