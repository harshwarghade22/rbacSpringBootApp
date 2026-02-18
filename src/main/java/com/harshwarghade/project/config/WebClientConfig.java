package com.harshwarghade.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {

        // 🔥 Increase HTTP connection pool
        ConnectionProvider provider = ConnectionProvider.builder("bank-api-pool")
                .maxConnections(50) // good for 16 threads
                .pendingAcquireMaxCount(200)
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .keepAlive(true)
                .compress(true);

        // 🔥 CRITICAL: Increase buffer size (default = 256KB ❌)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs()
                                .maxInMemorySize(50 * 1024 * 1024) // 50 MB buffer
                )
                .build();

        return WebClient.builder()
                .baseUrl("http://localhost:8081")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
