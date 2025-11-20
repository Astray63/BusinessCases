package com.eb.electricitybusiness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElectricityBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricityBusinessApplication.class, args);
    }
}
