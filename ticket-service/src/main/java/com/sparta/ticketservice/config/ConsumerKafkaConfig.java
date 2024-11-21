package com.sparta.ticketservice.config;

import com.sparta.ticketservice.event.StockDecrEvent;
import com.sparta.ticketservice.event.StockIncrEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class ConsumerKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // StockDecrEvent 전용 ConsumerFactory와 KafkaListenerContainerFactory
    @Bean
    public ConsumerFactory<String, StockDecrEvent> stockDecrConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "ticket-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<StockDecrEvent> jsonDeserializer = new JsonDeserializer<>(StockDecrEvent.class);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockDecrEvent> stockDecrKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StockDecrEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stockDecrConsumerFactory());
        return factory;
    }

    // StockIncrEvent 전용 ConsumerFactory와 KafkaListenerContainerFactory
    @Bean
    public ConsumerFactory<String, StockIncrEvent> stockIncrConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "ticket-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<StockIncrEvent> jsonDeserializer = new JsonDeserializer<>(StockIncrEvent.class);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockIncrEvent> stockIncrKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StockIncrEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stockIncrConsumerFactory());
        return factory;
    }
}
