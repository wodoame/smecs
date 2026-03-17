package com.smecs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
// async configuration is controlled via AsyncConfig
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableCaching
public class SmeCSApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmeCSApplication.class, args);
    }
}
