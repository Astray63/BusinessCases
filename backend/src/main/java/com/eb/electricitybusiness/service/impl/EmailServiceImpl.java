package com.eb.electricitybusiness.service.impl;

import com.eb.electricitybusiness.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
        String subject = "Code de validation - Electric Charge Platform";
        String htmlContent = buildVerificationEmailHtml(name, verificationCode);

        try {
            logger.info("📧 Tentative d'envoi d'email de vérification à: {}", to);
            sendEmail(to, name, subject, htmlContent);
            logger.info("✅ Email de vérification envoyé avec succès à: {}", to);
        } catch (Exception e) {
            logger.error("❌ ERREUR lors de l'envoi de l'email de vérification à {}", to);
            logger.error("❌ Message d'erreur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
    }

    private void sendEmail(String to, String name, String subject, String htmlContent) {
        try {
            logger.info("=== DÉBUT ENVOI EMAIL ===");
            logger.info("Destinataire: {}", to);

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

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    BREVO_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Email envoyé avec succès");
            } else {
                logger.error("❌ Échec de l'envoi. Status: {}", response.getStatusCode());
                throw new RuntimeException("Échec de l'envoi de l'email: " + response.getStatusCode());
            }

            logger.info("=== FIN ENVOI EMAIL ===");

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi d'email à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    private String buildVerificationEmailHtml(String name, String verificationCode) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Validation de votre compte</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f3f4f6;
                            margin: 0;
                            padding: 0;
                            line-height: 1.6;
                            color: #1f2937;
                        }
                        .container {
                            max-width: 600px;
                            margin: 40px auto;
                            background-color: #ffffff;
                            border-radius: 16px;
                            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
                            overflow: hidden;
                        }
                        .header {
                            background-color: #2563eb; /* Blue-600 */
                            padding: 32px 24px;
                            text-align: center;
                        }
                        .header h1 {
                            color: #ffffff;
                            margin: 0;
                            font-size: 24px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px 32px;
                            text-align: center;
                        }
                        .greeting {
                            font-size: 18px;
                            margin-bottom: 24px;
                            color: #111827;
                        }
                        .message {
                            color: #4b5563;
                            margin-bottom: 32px;
                        }
                        .code-container {
                            background-color: #eff6ff; /* Blue-50 */
                            border: 2px dashed #2563eb;
                            border-radius: 12px;
                            padding: 24px;
                            margin: 32px 0;
                            display: inline-block;
                        }
                        .code {
                            font-family: 'Courier New', Courier, monospace;
                            font-size: 36px;
                            font-weight: 700;
                            color: #2563eb;
                            letter-spacing: 8px;
                        }
                        .expiry {
                            font-size: 14px;
                            color: #6b7280;
                            margin-top: 16px;
                        }
                        .footer {
                            background-color: #f9fafb;
                            padding: 24px;
                            text-align: center;
                            font-size: 12px;
                            color: #9ca3af;
                            border-top: 1px solid #e5e7eb;
                        }
                        .footer p {
                            margin: 4px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Validation de votre compte</h1>
                        </div>
                        <div class="content">
                            <p class="greeting">Bonjour <strong>%s</strong>,</p>
                            <p class="message">
                                Merci de vous être inscrit sur <strong>Electricity Business</strong>.<br>
                                Pour finaliser votre inscription, veuillez utiliser le code de validation ci-dessous.
                            </p>

                            <div class="code-container">
                                <div class="code">%s</div>
                            </div>

                            <p class="expiry">Ce code est valable pendant 15 minutes.</p>
                            <p class="message" style="font-size: 14px; margin-top: 32px;">
                                Si vous n'avez pas créé de compte, vous pouvez ignorer cet email en toute sécurité.
                            </p>
                        </div>
                        <div class="footer">
                            <p>Cet email a été envoyé automatiquement.</p>
                            <p>&copy; 2025 Electricity Business. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, name, verificationCode);
    }
}
