package com.piedrazul.msnotifications.infra.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espejo del evento publicado por MSUserManagement.
 * Campos: userId, login, password, rol — deben coincidir exactamente con UserRegisteredEvent de UserManagement.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private Long userId;
    private String login;
    private String password;
    private String rol;
}
