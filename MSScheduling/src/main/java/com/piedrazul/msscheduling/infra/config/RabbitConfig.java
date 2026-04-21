package com.piedrazul.msscheduling.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Exchange de UserManagement — solo se consume, no se declara (ya existe)
    public static final String USER_EXCHANGE             = "user.exchange";
    public static final String QUEUE_USER_REGISTERED     = "scheduling.user.registered.queue";
    public static final String ROUTING_USER_REGISTERED   = "user.registered";
    public static final String QUEUE_PROFESIONAL_CREADO  = "scheduling.profesional.creado.queue";
    public static final String ROUTING_PROFESIONAL_CREADO = "profesional.creado";

    // Exchange propio de Scheduling — publica eventos de citas
    public static final String SCHEDULING_EXCHANGE       = "scheduling.exchange";
    public static final String ROUTING_CITA_AGENDADA     = "cita.agendada";
    public static final String ROUTING_CITA_CANCELADA    = "cita.cancelada";
    public static final String ROUTING_CITA_COMPLETADA   = "cita.completada";

    // --- Exchange de UserManagement (durable, debe coincidir) ---
    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder.topicExchange(USER_EXCHANGE).durable(true).build();
    }

    // --- Cola para consumir UserRegistered ---
    @Bean
    public Queue schedulingUserRegisteredQueue() {
        return QueueBuilder.durable(QUEUE_USER_REGISTERED).build();
    }

    @Bean
    public Binding schedulingUserRegisteredBinding(Queue schedulingUserRegisteredQueue,
                                                   TopicExchange userExchange) {
        return BindingBuilder
                .bind(schedulingUserRegisteredQueue)
                .to(userExchange)
                .with(ROUTING_USER_REGISTERED);
    }

    // --- Cola para consumir ProfesionalCreado ---
    @Bean
    public Queue schedulingProfesionalCreadoQueue() {
        return QueueBuilder.durable(QUEUE_PROFESIONAL_CREADO).build();
    }

    @Bean
    public Binding schedulingProfesionalCreadoBinding(Queue schedulingProfesionalCreadoQueue,
                                                      TopicExchange userExchange) {
        return BindingBuilder
                .bind(schedulingProfesionalCreadoQueue)
                .to(userExchange)
                .with(ROUTING_PROFESIONAL_CREADO);
    }

    // --- Exchange propio de Scheduling ---
    @Bean
    public TopicExchange schedulingExchange() {
        return ExchangeBuilder.topicExchange(SCHEDULING_EXCHANGE).durable(true).build();
    }

    // --- Infraestructura común ---
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