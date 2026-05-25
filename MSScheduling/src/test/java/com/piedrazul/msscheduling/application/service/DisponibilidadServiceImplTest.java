package com.piedrazul.msscheduling.application.service;

import com.piedrazul.msscheduling.application.service.impl.DisponibilidadServiceImpl;
import com.piedrazul.msscheduling.domain.model.dto.DisponibilidadSemanalDTO;
import com.piedrazul.msscheduling.domain.model.exceptions.ConfiguracionInvalidaException;
import com.piedrazul.msscheduling.domain.model.repository.CitaRepository;
import com.piedrazul.msscheduling.domain.model.repository.DisponibilidadSemanalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DisponibilidadServiceImpl — validaciones de negocio")
class DisponibilidadServiceImplTest {

    @Mock  private DisponibilidadSemanalRepository disponibilidadRepository;
    @Mock  private CitaRepository citaRepository;
    @InjectMocks private DisponibilidadServiceImpl service;

    // -----------------------------------------------------------------------
    // validarFranjaHoraria
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("validarFranjaHoraria()")
    class ValidarFranjaHorariaTest {

        @Test
        @DisplayName("Lanza ConfiguracionInvalidaException cuando horaFin == horaInicio")
        void horaFinIgualHoraInicio_lanzaExcepcion() {
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(9, 0));
            dto.setHoraFin(LocalTime.of(9, 0));

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ConfiguracionInvalidaException.class);
        }

        @Test
        @DisplayName("Lanza ConfiguracionInvalidaException cuando horaFin < horaInicio")
        void horaFinAntesDeHoraInicio_lanzaExcepcion() {
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(14, 0));
            dto.setHoraFin(LocalTime.of(9, 0));

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ConfiguracionInvalidaException.class);
        }

        @Test
        @DisplayName("Mensaje de excepción menciona horaFin y horaInicio")
        void mensajeExcepcion_mencionaHoras() {
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(10, 0));
            dto.setHoraFin(LocalTime.of(9, 0));

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ConfiguracionInvalidaException.class)
                    .hasMessageContaining("09:00")
                    .hasMessageContaining("10:00");
        }
    }

    // -----------------------------------------------------------------------
    // validarIntervaloCaben
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("validarIntervaloCaben()")
    class ValidarIntervaloTest {

        @Test
        @DisplayName("Lanza excepción cuando el intervalo es mayor que la franja")
        void intervaloMayorQueFraga_lanzaExcepcion() {
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(9, 0));
            dto.setHoraFin(LocalTime.of(9, 20));   // 20 min
            dto.setDuracionCitaMinutos(30);          // 30 min — no cabe

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ConfiguracionInvalidaException.class);
        }

        @Test
        @DisplayName("Lanza excepción cuando el intervalo es igual a la franja pero sin margen")
        void intervaloIgualAFranja_lanzaExcepcion() {
            // La validación exige que minutosTotales >= duracion.
            // Con minutos=30 e intervalo=31, no cabe ninguna cita.
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(9, 0));
            dto.setHoraFin(LocalTime.of(9, 30));    // 30 min
            dto.setDuracionCitaMinutos(31);           // 31 > 30

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ConfiguracionInvalidaException.class);
        }

        @Test
        @DisplayName("No lanza excepción cuando el intervalo cabe exactamente una vez en la franja")
        void intervaloCabeExacto_noLanzaExcepcion() {
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(9, 0));
            dto.setHoraFin(LocalTime.of(9, 30));    // 30 min
            dto.setDuracionCitaMinutos(30);           // 30 == 30 → exactamente una cita

            when(disponibilidadRepository.save(any())).thenAnswer(inv -> {
                var e = inv.getArgument(0, com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal.class);
                return e;
            });

            // No debe lanzar excepción
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.crear(dto));
        }

        @Test
        @DisplayName("Mensaje de excepción menciona el intervalo y los minutos de la franja")
        void mensajeExcepcion_mencionaIntervalYFranja() {
            DisponibilidadSemanalDTO dto = dtoValido();
            dto.setHoraInicio(LocalTime.of(9, 0));
            dto.setHoraFin(LocalTime.of(9, 15));    // 15 min
            dto.setDuracionCitaMinutos(60);           // 60 > 15

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ConfiguracionInvalidaException.class)
                    .hasMessageContaining("60")
                    .hasMessageContaining("15");
        }
    }

    // -----------------------------------------------------------------------
    // crear — camino feliz
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("crear() — camino feliz")
    class CrearCaminoFelizTest {

        @Test
        @DisplayName("Persiste la entidad a través del repositorio")
        void crear_llamaASave() {
            DisponibilidadSemanalDTO dto = dtoValido();

            when(disponibilidadRepository.save(any())).thenAnswer(inv ->
                    inv.getArgument(0, com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal.class));

            service.crear(dto);

            verify(disponibilidadRepository).save(any());
        }

        @Test
        @DisplayName("Retorna un DTO con los mismos valores que el DTO de entrada")
        void crear_retornaDTOCorrecto() {
            DisponibilidadSemanalDTO dto = dtoValido();

            when(disponibilidadRepository.save(any())).thenAnswer(inv ->
                    inv.getArgument(0, com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal.class));

            DisponibilidadSemanalDTO resultado = service.crear(dto);

            assertThat(resultado.getProfesionalId()).isEqualTo(1L);
            assertThat(resultado.getDiaSemana()).isEqualTo(1);
            assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
            assertThat(resultado.getHoraFin()).isEqualTo(LocalTime.of(17, 0));
            assertThat(resultado.getDuracionCitaMinutos()).isEqualTo(30);
        }
    }

    // -----------------------------------------------------------------------
    // listarPorProfesional
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("listarPorProfesional() delega en el repositorio y retorna lista vacía si no hay datos")
    void listarPorProfesional_delegaEnRepositorio() {
        when(disponibilidadRepository.findByProfesionalId(5L)).thenReturn(List.of());

        List<DisponibilidadSemanalDTO> resultado = service.listarPorProfesional(5L);

        assertThat(resultado).isEmpty();
        verify(disponibilidadRepository).findByProfesionalId(5L);
    }

    // -----------------------------------------------------------------------
    // eliminar
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("eliminar() llama a deleteById en el repositorio")
    void eliminar_llamaADeleteById() {
        service.eliminar(99L);
        verify(disponibilidadRepository).deleteById(99L);
    }

    // -----------------------------------------------------------------------
    // Fixture
    // -----------------------------------------------------------------------
    private DisponibilidadSemanalDTO dtoValido() {
        return DisponibilidadSemanalDTO.builder()
                .profesionalId(1L)
                .diaSemana(1)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(17, 0))
                .duracionCitaMinutos(30)
                .build();
    }
}
