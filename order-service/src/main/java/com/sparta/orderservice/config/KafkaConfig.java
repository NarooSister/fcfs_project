package com.sparta.orderservice.config;

import com.sparta.orderservice.event.StockDecrEvent;
import com.sparta.orderservice.event.StockIncrEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // 공통 설정을 위한 메서드
    private Map<String, Object> producerConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }

    // StockDecrEvent 전용 KafkaTemplate 빈
    @Bean
    public ProducerFactory<String, StockDecrEvent> stockDecrProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate() {
        return new KafkaTemplate<>(stockDecrProducerFactory());
    }

    // StockIncrEvent 전용 KafkaTemplate 빈
    @Bean
    public ProducerFactory<String, StockIncrEvent> stockIncrProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate() {
        return new KafkaTemplate<>(stockIncrProducerFactory());
    }
}