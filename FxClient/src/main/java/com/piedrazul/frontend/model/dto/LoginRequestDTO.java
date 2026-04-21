package com.piedrazul.frontend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class LoginRequestDTO {
    private String login;
    private String password;
}
