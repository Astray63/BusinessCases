package com.eb.electricitybusiness.config;

import com.eb.electricitybusiness.servlet.RegisterServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour enregistrer les servlets Java SE
 * Permet d'utiliser des servlets classiques dans Spring Boot
 */
@Configuration
public class ServletConfig {

    /**
     * Enregistre le servlet d'inscription Java SE pur
     * Mappe le servlet à l'URL /auth/register
     */
    @Bean
    public ServletRegistrationBean<RegisterServlet> registerServlet() {
        ServletRegistrationBean<RegisterServlet> bean =
            new ServletRegistrationBean<>(new RegisterServlet(), "/auth/register");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
