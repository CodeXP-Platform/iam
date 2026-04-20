package com.codexp.iam.infrastructure.messaging;

import com.codexp.iam.event.UserDeletedEvent;
import com.codexp.iam.event.UserProfileUpdatedEvent;
import com.codexp.iam.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publicando UserRegisteredEvent para userId={}", event.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IAM_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_CREATED,
                event
        );
    }

    public void publishUserProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("Publicando UserProfileUpdatedEvent para userId={}", event.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IAM_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_UPDATED,
                event
        );
    }

    public void publishUserDeleted(UserDeletedEvent event) {
        log.info("Publicando UserDeletedEvent para userId={}", event.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IAM_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_DELETED,
                event
        );
    }
}