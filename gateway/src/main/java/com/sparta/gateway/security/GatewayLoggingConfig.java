package com.sparta.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayLoggingConfig.class);

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            logger.info("Request: {}", exchange.getRequest().getURI());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                logger.info("Final Response to Client: {}", exchange.getResponse().getStatusCode());
                logger.info("Response Headers: {}", exchange.getResponse().getHeaders());

            }));
        };
    }
}
