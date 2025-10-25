package com.cakify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CakifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CakifyApplication.class, args);
    }

}
