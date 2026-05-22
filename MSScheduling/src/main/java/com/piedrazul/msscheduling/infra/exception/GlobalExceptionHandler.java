package com.piedrazul.msscheduling.infra.exception;

import com.piedrazul.msscheduling.domain.model.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Excepciones de citas ─────────────────────────────────────────────────

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ApiError> handleCitaNoEncontrada(CitaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado", ex.getMessage()));
    }

    @ExceptionHandler(HorarioOcupadoException.class)
    public ResponseEntity<ApiError> handleHorarioOcupado(HorarioOcupadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(HttpStatus.CONFLICT.value(), "Horario no disponible", ex.getMessage()));
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ResponseEntity<ApiError> handleTransicionInvalida(TransicionEstadoInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Transición de estado inválida", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Usuario no sincronizado", ex.getMessage()));
    }

    // ── HU-1.5 / HU-1.6: configuración de disponibilidad ────────────────────

    /**
     * HU-1.6 SC-2: intervalo inválido o franja horaria incorrecta.
     * HTTP 422: la solicitud es semánticamente inaceptable.
     */
    @ExceptionHandler(ConfiguracionInvalidaException.class)
    public ResponseEntity<ApiError> handleConfiguracionInvalida(ConfiguracionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Configuración de disponibilidad inválida", ex.getMessage()));
    }

    // ── HU-1.7: ventana de agendamiento ─────────────────────────────────────

    /**
     * HU-1.7 SC-2: fecha fuera de la ventana de agendamiento configurada.
     * HTTP 422: la fecha no está dentro del rango permitido.
     */
    @ExceptionHandler(FueraDeVentanaAgendamientoException.class)
    public ResponseEntity<ApiError> handleFueraDeVentana(FueraDeVentanaAgendamientoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Fecha fuera del rango de agendamiento", ex.getMessage()));
    }

    // ── HU-1.8: días no disponibles y festivos ────────────────────────────────

    /**
     * HU-1.8 SC-1/SC-2: intento de agendar en un día no disponible o festivo.
     * HTTP 422: la fecha está bloqueada a nivel global.
     */
    @ExceptionHandler(FechaNoDisponibleException.class)
    public ResponseEntity<ApiError> handleFechaNoDisponible(FechaNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Fecha no disponible", ex.getMessage()));
    }

    // ── Validación de Bean (campos obligatorios / rangos) ────────────────────

    /**
     * HU-1.5 SC-2 / HU-1.6 SC-2: campos obligatorios vacíos o fuera de rango.
     * Captura errores de @Valid / @NotNull / @Min / @Max.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(HttpStatus.BAD_REQUEST.value(),
                        "Datos incompletos o inválidos", detalle));
    }

    // ── Argumentos ilegales generales ────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(HttpStatus.BAD_REQUEST.value(), "Solicitud inválida", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error interno del servidor", "Ocurrió un error inesperado"));
    }
}
