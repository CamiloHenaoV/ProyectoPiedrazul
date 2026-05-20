package com.piedrazul.msscheduling.domain.model.state;

import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Componente del patrón <b>State</b> que actúa como registro de handlers.
 *
 * <p>Resuelve un valor del enum {@link EstadoCita} al {@link EstadoCitaHandler}
 * concreto que encapsula las reglas de transición de ese estado. Al arrancar
 * la aplicación, Spring inyecta automáticamente todos los beans que implementan
 * {@link EstadoCitaHandler} y este resolver construye el mapa de despacho.
 *
 * <p><b>Extensibilidad:</b> añadir un nuevo estado al ciclo de vida de la cita
 * solo requiere:
 * <ol>
 *   <li>Agregar el valor al enum {@link EstadoCita}.</li>
 *   <li>Crear un {@code @Component} que implemente {@link EstadoCitaHandler}.</li>
 * </ol>
 * Ni este resolver ni {@code CitaServiceImpl} necesitan modificarse
 * (principio Open/Closed).
 */
@Component
public class CitaEstadoResolver {

    private final Map<EstadoCita, EstadoCitaHandler> handlers;

    /**
     * Spring inyecta la lista completa de beans {@link EstadoCitaHandler}
     * registrados en el contexto: {@code EstadoProgramadaHandler},
     * {@code EstadoCanceladaHandler} y {@code EstadoCompletadaHandler}.
     */
    public CitaEstadoResolver(List<EstadoCitaHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        EstadoCitaHandler::getEstado,
                        Function.identity()
                ));
    }

    /**
     * Devuelve el handler correspondiente al estado dado.
     *
     * @param estado estado actual de la cita.
     * @return handler que encapsula las reglas de transición para ese estado.
     * @throws IllegalStateException si no existe handler registrado para el estado
     *         (situación que solo puede ocurrir si se añade un valor al enum sin
     *         crear su handler correspondiente).
     */
    public EstadoCitaHandler resolve(EstadoCita estado) {
        return Optional.ofNullable(handlers.get(estado))
                .orElseThrow(() -> new IllegalStateException(
                        "No hay handler de estado registrado para: " + estado
                ));
    }
}
