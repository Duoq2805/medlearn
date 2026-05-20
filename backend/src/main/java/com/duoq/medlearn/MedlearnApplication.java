package com.duoq.medlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MedlearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedlearnApplication.class, args);
    }

}
