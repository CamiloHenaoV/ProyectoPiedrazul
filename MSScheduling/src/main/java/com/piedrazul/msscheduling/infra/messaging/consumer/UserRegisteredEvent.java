package com.piedrazul.msscheduling.infra.messaging.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private Long userId;
    private String login;
    private String password;
    private String rol;
}
