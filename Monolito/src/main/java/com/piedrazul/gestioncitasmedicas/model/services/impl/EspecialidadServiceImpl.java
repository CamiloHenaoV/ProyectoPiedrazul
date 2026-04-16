package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.repositories.EspecialidadRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IEspecialidadService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadServiceImpl implements IEspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadServiceImpl(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public List<String> listarNombres() {
        return especialidadRepository.findAll()
                .stream()
                .map(e -> e.getNombre())
                .sorted()
                .collect(Collectors.toList());
    }
}