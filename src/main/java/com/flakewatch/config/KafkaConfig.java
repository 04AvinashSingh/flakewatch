package com.flakewatch.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    // Define the main topic with multiple partitions to support horizontal scaling
    @Bean
    public NewTopic testResultsTopic() {
        return TopicBuilder.name("test-results")
                .partitions(5) // Scale up to 5 consumer replicas in the consumer group
                .replicas(1)
                .build();
    }

    // Define the Dead Letter Topic
    @Bean
    public NewTopic testResultsDlt() {
        return TopicBuilder.name("test-results-dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // Publish to topic-name-dlt on failure
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (r, e) -> new org.apache.kafka.common.TopicPartition(r.topic() + "-dlt", r.partition()));
        
        // Retry 3 times with 2 seconds interval
        return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3));
    }
}
