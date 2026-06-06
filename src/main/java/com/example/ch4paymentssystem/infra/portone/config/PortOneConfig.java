package com.example.ch4paymentssystem.infra.portone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class PortOneConfig {

    private final PortOneProperties properties;

    @Bean
    public RestClient portOneRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "PortOne " + properties.getApiSecret())
                .build();
    }
}
