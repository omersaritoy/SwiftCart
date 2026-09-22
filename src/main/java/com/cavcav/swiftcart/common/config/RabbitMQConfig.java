package com.cavcav.swiftcart.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "swiftcart.exchange";

    // Queue'lar
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    // Routing key'ler
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String PAYMENT_SUCCESS_KEY = "payment.success";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE);
    }
    @Bean
    public Queue emailQueue(){
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }
    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }
    @Bean
    public Binding emailBinding(Queue emailQueue,TopicExchange exchange){
        return BindingBuilder.bind(emailQueue).to(exchange).with("order.#");
    }
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(exchange)
                .with("order.#");
    }
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}
