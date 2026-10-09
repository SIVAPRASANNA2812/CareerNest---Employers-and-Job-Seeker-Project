package com.guvi.careernestbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CareernestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareernestBackendApplication.class, args);
    }

}
