package com.duoq.medlearn;

import org.springframework.boot.SpringApplication;

public class TestMedlearnApplication {

    public static void main(String[] args) {
        SpringApplication.from(MedlearnApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
