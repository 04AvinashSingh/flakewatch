package com.flakewatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FlakewatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlakewatchApplication.class, args);
    }

}
