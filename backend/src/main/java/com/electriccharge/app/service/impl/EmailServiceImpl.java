package com.electriccharge.app.service.impl;

import com.electriccharge.app.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate;

    public EmailServiceImpl() {
        // Configuration simple du RestTemplate
        this.restTemplate = new RestTemplate();
        
        // Log de test au démarrage
        logger.info("✅ EmailService initialisé avec RestTemplate");
    }

    @Override
    public void sendVerificationEmail(String to, String name, String verificationCode) {
        String subject = "Validation de votre compte";
        String htmlContent = buildVerificationEmailHtml(name, verificationCode);
        
        try {
            logger.info("📧 Tentative d'envoi d'email de vérification à: {}", to);
            sendEmail(to, name, subject, htmlContent);
            logger.info("✅ Email de vérification envoyé avec succès à: {}", to);
        } catch (Exception e) {
            logger.error("❌ ERREUR lors de l'envoi de l'email de vérification à {}", to);
            logger.error("❌ Message d'erreur: {}", e.getMessage());
            logger.error("❌ Type d'exception: {}", e.getClass().getName());
            if (e.getCause() != null) {
                logger.error("❌ Cause: {}", e.getCause().getMessage());
            }
            logger.error("❌ Stack trace:", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Bienvenue sur notre plateforme !";
        String htmlContent = buildWelcomeEmailHtml(name);
        
        try {
            sendEmail(to, name, subject, htmlContent);
            logger.info("Email de bienvenue envoyé à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue à {}: {}", to, e.getMessage());
        }
    }

    private void sendEmail(String to, String name, String subject, String htmlContent) {
        try {
            logger.info("=== DÉBUT ENVOI EMAIL ===");
            logger.info("Destinataire: {}", to);
            logger.info("Sujet: {}", subject);
            logger.info("API Key configurée: {}", brevoApiKey != null && !brevoApiKey.isEmpty());
            logger.info("Email expéditeur: {}", senderEmail);
            logger.info("Nom expéditeur: {}", senderName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.set("accept", "application/json");

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);

            Map<String, Object> recipient = new HashMap<>();
            recipient.put("email", to);
            recipient.put("name", name);

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("sender", sender);
            emailData.put("to", List.of(recipient));
            emailData.put("subject", subject);
            emailData.put("htmlContent", htmlContent);

            // Afficher le JSON envoyé
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonPayload = mapper.writeValueAsString(emailData);
                logger.debug("Payload JSON: {}", jsonPayload);
            } catch (Exception e) {
                logger.warn("Impossible d'afficher le JSON payload");
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            logger.info("Envoi de la requête à: {}", BREVO_API_URL);
            
            ResponseEntity<String> response = restTemplate.exchange(
                BREVO_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );

            logger.info("✅ Réponse reçue - Status: {}", response.getStatusCode());
            logger.info("✅ Body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Email envoyé avec succès à: {}", to);
            } else {
                logger.error("❌ Échec de l'envoi. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Échec de l'envoi de l'email: " + response.getStatusCode());
            }
            
            logger.info("=== FIN ENVOI EMAIL ===");
            
        } catch (RestClientException e) {
            logger.error("❌ Erreur REST lors de l'envoi d'email à {}: {}", to, e.getMessage());
            logger.error("❌ Détails de l'erreur:", e);
            throw new RuntimeException("Erreur REST lors de l'envoi de l'email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Erreur générale lors de l'envoi d'email à {}: {}", to, e.getMessage());
            logger.error("❌ Stack trace complète:", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email: " + e.getMessage(), e);
        }
    }

    private String buildVerificationEmailHtml(String name, String verificationCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 50px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .content {
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .code {
                        background-color: #f8f9fa;
                        border: 2px dashed #667eea;
                        border-radius: 8px;
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 5px;
                        padding: 20px;
                        margin: 30px 0;
                        color: #667eea;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #6c757d;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Validation de votre compte</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Un code de validation a été envoyé à votre adresse email.</p>
                        <p>Voici votre code de vérification à 6 chiffres :</p>
                        <div class="code">%s</div>
                        <p>Ce code est valable pendant 15 minutes.</p>
                        <p>Si vous n'avez pas demandé cette vérification, vous pouvez ignorer cet email.</p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                        <p>&copy; 2025 Electric Charge Platform. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, verificationCode);
    }

    private String buildWelcomeEmailHtml(String name) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 50px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #6c757d;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Bienvenue !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre compte a été validé avec succès ! Bienvenue sur notre plateforme de recharge pour véhicules électriques.</p>
                        <p>Vous pouvez maintenant :</p>
                        <ul>
                            <li>Réserver des bornes de recharge</li>
                            <li>Gérer vos véhicules électriques</li>
                            <li>Suivre vos réservations</li>
                            <li>Laisser des avis sur les bornes</li>
                        </ul>
                        <p>Nous sommes ravis de vous compter parmi nous !</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 Electric Charge Platform. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """, name);
    }
}
