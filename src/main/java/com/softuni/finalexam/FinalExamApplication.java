package com.softuni.finalexam;


import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class FinalExamApplication {

    @PostConstruct
    public void init() {
        // Set default timezone to Europe/Sofia (Bulgaria)
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Sofia"));
    }

    public static void main(String[] args) {

        SpringApplication.run(FinalExamApplication.class, args);
    }

}
