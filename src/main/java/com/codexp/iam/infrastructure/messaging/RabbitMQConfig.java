package com.codexp.iam.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String IAM_EXCHANGE = "iam.exchange";

    public static final String ROUTING_KEY_CREATED  = "iam.user.created";
    public static final String ROUTING_KEY_UPDATED  = "iam.user.updated.profile";
    public static final String ROUTING_KEY_DELETED  = "iam.user.deleted";

    public static final String QUEUE_CREATED = "iam.user.created.queue";
    public static final String QUEUE_UPDATED = "iam.user.updated.queue";
    public static final String QUEUE_DELETED = "iam.user.deleted.queue";

    @Bean
    public TopicExchange iamExchange() {
        return new TopicExchange(IAM_EXCHANGE, true, false);
    }

    @Bean public Queue queueCreated() { return new Queue(QUEUE_CREATED, true); }
    @Bean public Queue queueUpdated() { return new Queue(QUEUE_UPDATED, true); }
    @Bean public Queue queueDeleted() { return new Queue(QUEUE_DELETED, true); }

    @Bean
    public Binding bindingCreated() {
        return BindingBuilder.bind(queueCreated()).to(iamExchange()).with(ROUTING_KEY_CREATED);
    }
    @Bean
    public Binding bindingUpdated() {
        return BindingBuilder.bind(queueUpdated()).to(iamExchange()).with(ROUTING_KEY_UPDATED);
    }
    @Bean
    public Binding bindingDeleted() {
        return BindingBuilder.bind(queueDeleted()).to(iamExchange()).with(ROUTING_KEY_DELETED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}