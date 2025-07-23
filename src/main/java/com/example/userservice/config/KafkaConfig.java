package com.example.userservice.config;

import com.example.userservice.dto.event.BaseUserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;
    
    @Value("${azure.eventhubs.connection-string:}")
    private String eventHubsConnectionString;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ProducerFactory<String, BaseUserEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Azure Event Hubs specific configuration
        configProps.put("security.protocol", "SASL_SSL");
        configProps.put("sasl.mechanism", "PLAIN");
        configProps.put("sasl.jaas.config", 
            "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"$ConnectionString\" " +
            "password=\"" + eventHubsConnectionString + "\";");
        configProps.put("client.dns.lookup", "use_all_dns_ips");
        configProps.put("acks", "all");
        configProps.put("retries", 2147483647);
        configProps.put("max.in.flight.requests.per.connection", 5);
        configProps.put("enable.idempotence", true);
        
        configProps.put(JsonSerializer.TYPE_MAPPINGS, 
            "userDeleted:com.example.userservice.dto.event.UserDeletedEvent," +
            "userSuspended:com.example.userservice.dto.event.UserSuspendedEvent," +
            "userUpdated:com.example.userservice.dto.event.UserUpdatedEvent");
        
        logger.info("Kafka Producer configured with bootstrap servers: {}", bootstrapServers);
        logger.info("Azure Event Hubs configuration applied");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, BaseUserEvent> kafkaTemplate() {
        KafkaTemplate<String, BaseUserEvent> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic("user-events");
        return template;
    }
}