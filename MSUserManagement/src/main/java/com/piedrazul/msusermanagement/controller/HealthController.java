package com.piedrazul.msusermanagement.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired private DataSource ds;
    @Autowired
    private RabbitTemplate rabbit;

    @GetMapping("/health")
    public Map<String, String> check() throws SQLException {
        return Map.of(
                "db", ds.getConnection().isValid(2) ? "OK" : "FAIL",
                "rabbit", rabbit.getConnectionFactory().createConnection().isOpen() ? "OK" : "FAIL"
        );
    }
}
