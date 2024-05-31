package com.challenge.ratelimit.e2e.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class E2ETestConfiguration {

    private final RedisServer redisServer;

    public E2ETestConfiguration(@Value("${spring.data.redis.port}") final int port) throws IOException {
        this.redisServer = new RedisServer(port);
    }

    @Bean
    public WebClient webClient(@Value("${server.port}") final int port,
                               @Value("${spring.webflux.base-path}") final String contextPath,
                               final ObjectMapper objectMapper) {
        return WebClient.builder()
                .baseUrl("http://localhost:" + port + contextPath)
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                })
                .build();
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        redisServer.stop();
    }

}
