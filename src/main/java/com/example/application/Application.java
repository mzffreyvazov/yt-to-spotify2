package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class Application {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone(); 
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
