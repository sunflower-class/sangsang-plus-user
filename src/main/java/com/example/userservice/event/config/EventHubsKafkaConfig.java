package com.example.userservice.event.config;

import com.example.userservice.event.model.BaseUserEvent;
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

/**
 * Configuration for Azure Event Hubs using Kafka protocol
 * This class configures the Kafka producer to connect to Azure Event Hubs
 */
@Configuration
public class EventHubsKafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(EventHubsKafkaConfig.class);

    @Value("${AZURE_EVENTHUBS_NAMESPACE:${azure.eventhubs.namespace:}}")
    private String eventHubsNamespace;
    
    @Value("${AZURE_EVENTHUBS_CONNECTION_STRING:${azure.eventhubs.connection-string:}}")
    private String eventHubsConnectionString;
    
    @Value("${AZURE_EVENTHUB_NAME:${azure.eventhubs.eventhub-name:user-events}}")
    private String eventHubName;

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
        
        // Azure Event Hubs requires the namespace FQDN as bootstrap server
        String bootstrapServers = eventHubsNamespace + ".servicebus.windows.net:9093";
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Azure Event Hubs Kafka configuration
        configProps.put("security.protocol", "SASL_SSL");
        configProps.put("sasl.mechanism", "PLAIN");
        configProps.put("sasl.jaas.config", 
            "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"$ConnectionString\" " +
            "password=\"" + eventHubsConnectionString + "\";");
        
        // Azure Event Hubs optimized settings
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configProps.put("client.dns.lookup", "use_all_dns_ips");
        configProps.put("acks", "1");
        configProps.put("retries", 3);
        configProps.put("max.in.flight.requests.per.connection", 1);
        configProps.put("enable.idempotence", false);
        configProps.put("compression.type", "none");
        configProps.put("linger.ms", 0);
        configProps.put("request.timeout.ms", 30000);
        configProps.put("delivery.timeout.ms", 120000);
        configProps.put("batch.size", 16384);
        configProps.put("buffer.memory", 33554432);
        
        // Message format compatibility settings for Azure Event Hubs
        configProps.put("api.version.request", false);
        configProps.put("kafka.api.version", "2.0.0");
        
        // Event type mappings for JSON serialization
        configProps.put(JsonSerializer.TYPE_MAPPINGS, 
            "userCreated:com.example.userservice.event.model.UserCreatedEvent," +
            "userDeleted:com.example.userservice.event.model.UserDeletedEvent," +
            "userSuspended:com.example.userservice.event.model.UserSuspendedEvent," +
            "userUpdated:com.example.userservice.event.model.UserUpdatedEvent," +
            "userProfile:com.example.userservice.event.model.UserProfileEvent");
        
        logger.info("Azure Event Hubs Kafka Producer configured - Namespace: {}, Event Hub: {}", 
            eventHubsNamespace, eventHubName);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, BaseUserEvent> kafkaTemplate() {
        KafkaTemplate<String, BaseUserEvent> template = new KafkaTemplate<>(producerFactory());
        // Use the configured event hub name as the topic
        template.setDefaultTopic(eventHubName);
        logger.info("KafkaTemplate configured with default topic: {}", eventHubName);
        return template;
    }
}