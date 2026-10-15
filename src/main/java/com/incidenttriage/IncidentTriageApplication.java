package com.incidenttriage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IncidentTriageApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentTriageApplication.class, args);
    }
}
