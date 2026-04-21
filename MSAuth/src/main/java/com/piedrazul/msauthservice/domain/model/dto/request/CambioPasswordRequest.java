package com.piedrazul.msauthservice.domain.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambioPasswordRequest {

    @NotBlank
    private String passwordActual;

    @NotBlank
    private String passwordNuevo;
}
