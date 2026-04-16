package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;

public interface IAuthService {
    UsuarioDTO autenticar(String login, String password);
}
