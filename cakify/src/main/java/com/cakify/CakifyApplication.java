package com.cakify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CakifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CakifyApplication.class, args);
    }

    @Bean
    public org.springframework.mail.javamail.JavaMailSender javaMailSender() {
        return new org.springframework.mail.javamail.JavaMailSenderImpl();
    }

}
