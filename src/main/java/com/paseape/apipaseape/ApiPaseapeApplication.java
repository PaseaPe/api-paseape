package com.paseape.apipaseape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiPaseapeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiPaseapeApplication.class, args);
    }

}
