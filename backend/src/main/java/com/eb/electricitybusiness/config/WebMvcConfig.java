package com.eb.electricitybusiness.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration Spring MVC pour servir les fichiers statiques uploadés
 *
 * Permet l'accès aux photos des bornes via HTTP
 * Avec context-path /api, les URLs seront:
 * http://localhost:8080/api/uploads/bornes/borne-123/photo.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:${user.home}/electriccharge/uploads/bornes}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Servir les fichiers uploadés comme ressources statiques
        // URL pattern: /uploads/bornes/** (sera préfixé par /api à cause du
        // context-path)
        // Fichier physique: file:/home/user/electriccharge/uploads/bornes/

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";

        registry.addResourceHandler("/uploads/bornes/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(3600); // Cache 1 heure

    }
}
