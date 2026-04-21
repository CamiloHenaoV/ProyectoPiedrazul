package com.piedrazul.msnotifications.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Debe coincidir exactamente con UserManagement
    public static final String EXCHANGE         = "user.exchange";
    public static final String QUEUE            = "notifications.user.registered.queue";
    public static final String ROUTING_KEY      = "user.registered";

    // Preparado para Scheduling (se activa cuando ese BC esté listo)
    public static final String QUEUE_CITA_AGENDADA     = "notifications.cita.agendada.queue";
    public static final String ROUTING_KEY_CITA_AGENDADA = "cita.agendada";

    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    // --- Colas de User Management ---

    @Bean
    public Queue notificationsUserRegisteredQueue() {
        return QueueBuilder
                .durable(QUEUE)
                .build();
    }

    @Bean
    public Binding notificationsUserRegisteredBinding(Queue notificationsUserRegisteredQueue,
                                                      TopicExchange userExchange) {
        return BindingBuilder
                .bind(notificationsUserRegisteredQueue)
                .to(userExchange)
                .with(ROUTING_KEY);
    }

    // --- Colas de Scheduling (descomenta cuando Scheduling esté listo) ---
    /*
    @Bean
    public Queue notificationsCitaAgendadaQueue() {
        return QueueBuilder.durable(QUEUE_CITA_AGENDADA).build();
    }

    @Bean
    public Binding notificationsCitaAgendadaBinding(Queue notificationsCitaAgendadaQueue,
                                                     TopicExchange userExchange) {
        return BindingBuilder
                .bind(notificationsCitaAgendadaQueue)
                .to(userExchange)
                .with(ROUTING_KEY_CITA_AGENDADA);
    }
    */

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
