package com.example.ch4paymentssystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Ch4PaymentsSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ch4PaymentsSystemApplication.class, args);
    }

}
