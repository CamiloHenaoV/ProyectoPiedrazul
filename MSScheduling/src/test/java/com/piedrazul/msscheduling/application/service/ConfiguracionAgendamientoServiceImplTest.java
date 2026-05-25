package com.piedrazul.msscheduling.application.service;

import com.piedrazul.msscheduling.application.service.impl.ConfiguracionAgendamientoServiceImpl;
import com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO;
import com.piedrazul.msscheduling.domain.model.entity.ConfiguracionAgendamiento;
import com.piedrazul.msscheduling.domain.model.repository.ConfiguracionAgendamientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracionAgendamientoServiceImpl")
class ConfiguracionAgendamientoServiceImplTest {

    @Mock  private ConfiguracionAgendamientoRepository repository;
    @InjectMocks private ConfiguracionAgendamientoServiceImpl service;

    private static final Long CONFIG_ID  = 1L;
    private static final int  DEFAULT_SEMANAS = 4;

    // -----------------------------------------------------------------------
    // obtener()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("obtener()")
    class ObtenerTest {

        @Test
        @DisplayName("Retorna el DTO cuando existe la configuración en BD")
        void configExistente_retornaDTO() {
            ConfiguracionAgendamiento config = config(CONFIG_ID, 6);
            when(repository.findById(CONFIG_ID)).thenReturn(Optional.of(config));

            ConfiguracionAgendamientoDTO resultado = service.obtener();

            assertThat(resultado.getId()).isEqualTo(CONFIG_ID);
            assertThat(resultado.getSemanasHabilitadas()).isEqualTo(6);
        }

        @Test
        @DisplayName("Crea configuración por defecto (4 semanas) cuando no existe en BD")
        void configNoExistente_creaDefault() {
            when(repository.findById(CONFIG_ID)).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ConfiguracionAgendamientoDTO resultado = service.obtener();

            assertThat(resultado.getSemanasHabilitadas()).isEqualTo(DEFAULT_SEMANAS);
            verify(repository).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // actualizar()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("actualizar()")
    class ActualizarTest {

        @Test
        @DisplayName("Persiste el nuevo valor de semanasHabilitadas")
        void actualizar_persisteNuevoValor() {
            ConfiguracionAgendamiento config = config(CONFIG_ID, DEFAULT_SEMANAS);
            when(repository.findById(CONFIG_ID)).thenReturn(Optional.of(config));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ConfiguracionAgendamientoDTO dto = ConfiguracionAgendamientoDTO.builder()
                    .semanasHabilitadas(8).build();

            ConfiguracionAgendamientoDTO resultado = service.actualizar(dto);

            assertThat(resultado.getSemanasHabilitadas()).isEqualTo(8);
            verify(repository).save(any());
        }

        @Test
        @DisplayName("Crea configuración por defecto y la actualiza cuando no existía en BD")
        void actualizar_creaDefaultSiNoExiste() {
            when(repository.findById(CONFIG_ID)).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ConfiguracionAgendamientoDTO dto = ConfiguracionAgendamientoDTO.builder()
                    .semanasHabilitadas(12).build();

            ConfiguracionAgendamientoDTO resultado = service.actualizar(dto);

            assertThat(resultado.getSemanasHabilitadas()).isEqualTo(12);
            // save() es llamado dos veces: una al crear el default, otra al actualizar
            verify(repository, times(2)).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // obtenerFechaMaximaAgendamiento()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("obtenerFechaMaximaAgendamiento()")
    class FechaMaximaTest {

        @Test
        @DisplayName("Retorna LocalDate.now() + semanasHabilitadas cuando existe configuración")
        void configExistente_retornaFechaCorrecta() {
            when(repository.findById(CONFIG_ID))
                    .thenReturn(Optional.of(config(CONFIG_ID, 3)));

            LocalDate esperada = LocalDate.now().plusWeeks(3);
            assertThat(service.obtenerFechaMaximaAgendamiento()).isEqualTo(esperada);
        }

        @Test
        @DisplayName("Usa 4 semanas por defecto cuando no existe configuración en BD")
        void configNoExistente_usaDefault4Semanas() {
            when(repository.findById(CONFIG_ID)).thenReturn(Optional.empty());

            LocalDate esperada = LocalDate.now().plusWeeks(DEFAULT_SEMANAS);
            assertThat(service.obtenerFechaMaximaAgendamiento()).isEqualTo(esperada);
        }

        @Test
        @DisplayName("La fecha máxima siempre es mayor que hoy")
        void fechaMaxima_siempreMayorQueHoy() {
            when(repository.findById(CONFIG_ID))
                    .thenReturn(Optional.of(config(CONFIG_ID, 1)));

            assertThat(service.obtenerFechaMaximaAgendamiento()).isAfter(LocalDate.now());
        }

        @Test
        @DisplayName("Con 52 semanas, la fecha máxima está aproximadamente un año en el futuro")
        void con52Semanas_fechaAproximadamenteUnAnio() {
            when(repository.findById(CONFIG_ID))
                    .thenReturn(Optional.of(config(CONFIG_ID, 52)));

            LocalDate fecha = service.obtenerFechaMaximaAgendamiento();
            assertThat(fecha).isAfterOrEqualTo(LocalDate.now().plusDays(360));
        }
    }

    // -----------------------------------------------------------------------
    // Fixture
    // -----------------------------------------------------------------------
    private ConfiguracionAgendamiento config(Long id, int semanas) {
        return ConfiguracionAgendamiento.builder()
                .id(id)
                .semanasHabilitadas(semanas)
                .build();
    }
}
