package com.electriccharge.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:${user.home}/electriccharge/uploads/bornes}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Créer le répertoire s'il n'existe pas
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }

    // Mapper l'URL /uploads/bornes/** vers le répertoire de fichiers
    registry.addResourceHandler("/uploads/bornes/**")
        .addResourceLocations("file:" + uploadDir + "/");

    // Certaines routes de l'API sont préfixées par /api. On ajoute donc aussi le mapping
    // /api/uploads/bornes/** pour que les URLs générées avec ce préfixe fonctionnent.
    registry.addResourceHandler("/api/uploads/bornes/**")
        .addResourceLocations("file:" + uploadDir + "/");
    }
}
