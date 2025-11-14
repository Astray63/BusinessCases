package com.eb.electricitybusiness.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        try {
            // Charger le fichier .env depuis la racine du projet (parent du backend)
            Dotenv dotenv = Dotenv.configure()
                    .directory("../")
                    .ignoreIfMissing()
                    .load();
            
            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvMap.put(entry.getKey(), entry.getValue());
                System.out.println("✅ Loaded .env variable: " + entry.getKey() + " = " + entry.getValue());
            });
            
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", dotenvMap));
            
            System.out.println("✅ .env file loaded successfully with " + dotenvMap.size() + " variables");
        } catch (Exception e) {
            System.out.println("⚠️ Could not load .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
