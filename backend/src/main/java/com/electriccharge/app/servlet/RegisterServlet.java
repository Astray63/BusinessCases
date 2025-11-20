package com.electriccharge.app.servlet;

import com.electriccharge.app.service.UtilisateurService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet Java SE pur pour gérer l'inscription des utilisateurs
 * Utilise uniquement les API Java standard (HttpServlet)
 * Compatible avec le titre RNCP CDA
 */
public class RegisterServlet extends HttpServlet {

    private UtilisateurService utilisateurService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();

        // Récupérer le contexte Spring pour accéder aux services
        WebApplicationContext context = WebApplicationContextUtils
            .getRequiredWebApplicationContext(getServletContext());

        // Injecter les dépendances manuellement
        this.utilisateurService = context.getBean(UtilisateurService.class);
        this.objectMapper = new ObjectMapper();
        // Configurer Jackson pour supporter les dates Java 8 (LocalDate, LocalDateTime)
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuration des headers CORS
        configurerCors(response);

        // Configuration du type de contenu
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Lire le corps de la requête
            String requestBody = lireCorpsRequete(request);

            // Parser le JSON en Map
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);

            // Extraire les données de l'utilisateur et le mot de passe
            Map<String, Object> utilisateurData = (Map<String, Object>) requestData.get("utilisateur");
            String motDePasse = (String) requestData.get("motDePasse");

            // Valider les données
            if (utilisateurData == null || motDePasse == null || motDePasse.trim().isEmpty()) {
                envoyerErreur(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Les données utilisateur et le mot de passe sont requis");
                return;
            }

            // Créer l'objet utilisateur (conversion manuelle)
            com.electriccharge.app.dto.UtilisateurDto utilisateurDto =
                objectMapper.convertValue(utilisateurData, com.electriccharge.app.dto.UtilisateurDto.class);

            // Appeler le service pour créer l'utilisateur
            com.electriccharge.app.dto.UtilisateurDto nouveauUtilisateur =
                utilisateurService.creerUtilisateur(utilisateurDto, motDePasse);

            // Préparer la réponse de succès
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("result", "SUCCESS");
            responseData.put("message", "Inscription réussie. Un code de vérification a été envoyé à votre adresse email.");
            responseData.put("data", nouveauUtilisateur);

            // Envoyer la réponse
            response.setStatus(HttpServletResponse.SC_CREATED);
            envoyerReponse(response, responseData);

        } catch (IllegalArgumentException e) {
            envoyerErreur(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            envoyerErreur(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Une erreur est survenue lors de l'inscription: " + e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Gérer les requêtes preflight CORS
        configurerCors(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Lire le corps de la requête HTTP
     */
    private String lireCorpsRequete(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    /**
     * Configurer les headers CORS
     */
    private void configurerCors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * Envoyer une réponse JSON
     */
    private void envoyerReponse(HttpServletResponse response, Map<String, Object> data)
            throws IOException {
        PrintWriter out = response.getWriter();
        String jsonResponse = objectMapper.writeValueAsString(data);
        out.print(jsonResponse);
        out.flush();
    }

    /**
     * Envoyer une erreur JSON
     */
    private void envoyerErreur(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("result", "ERROR");
        errorData.put("message", message);
        errorData.put("data", null);

        envoyerReponse(response, errorData);
    }
}
