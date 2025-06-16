package com.eb.electricitybusiness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.eb.electricitybusiness", "com.electriccharge.app"})
@EntityScan(basePackages = {"com.eb.electricitybusiness", "com.electriccharge.app"})
@EnableJpaRepositories(basePackages = {"com.eb.electricitybusiness", "com.electriccharge.app"})
public class ElectricityBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricityBusinessApplication.class, args);
    }
}
