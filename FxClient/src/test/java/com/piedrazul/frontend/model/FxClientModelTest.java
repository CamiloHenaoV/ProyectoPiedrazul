package com.piedrazul.frontend.model;

import com.piedrazul.frontend.model.dto.*;
import com.piedrazul.frontend.model.enums.EstadoCita;
import com.piedrazul.frontend.model.enums.RolUsuario;
import com.piedrazul.frontend.model.enums.TipoProfesional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FxClient — Modelo (DTOs y Enums)")
class FxClientModelTest {

    // -----------------------------------------------------------------------
    // DisponibilidadSemanalDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("DisponibilidadSemanalDTO")
    class DisponibilidadSemanalDTOTest {

        @ParameterizedTest(name = "diaSemana={0} → ''{1}''")
        @CsvSource({
            "0, Domingo",
            "1, Lunes",
            "2, Martes",
            "3, Miércoles",
            "4, Jueves",
            "5, Viernes",
            "6, Sábado"
        })
        @DisplayName("getNombreDia() retorna el nombre correcto para cada índice")
        void getNombreDia_retornaNombreCorrecto(int dia, String esperado) {
            DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
            dto.setDiaSemana(dia);

            assertThat(dto.getNombreDia()).isEqualTo(esperado);
        }

        @Test
        @DisplayName("getNombreDia() retorna '' cuando diaSemana es null")
        void getNombreDia_null_retornaVacio() {
            DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
            dto.setDiaSemana(null);

            assertThat(dto.getNombreDia()).isEmpty();
        }

        @ParameterizedTest(name = "diaSemana={0} → '?'")
        @ValueSource(ints = {-1, 7, 99})
        @DisplayName("getNombreDia() retorna '?' para índices fuera de rango")
        void getNombreDia_fueraDeRango_retornaInterrogacion(int diaInvalido) {
            DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
            dto.setDiaSemana(diaInvalido);

            assertThat(dto.getNombreDia()).isEqualTo("?");
        }

        @Test
        @DisplayName("toString() incluye nombre del día, horaInicio, horaFin y duración")
        void toString_contieneInfoCompleta() {
            DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
            dto.setDiaSemana(1);
            dto.setHoraInicio(LocalTime.of(8, 0));
            dto.setHoraFin(LocalTime.of(17, 0));
            dto.setDuracionCitaMinutos(30);

            String resultado = dto.toString();

            assertThat(resultado)
                    .contains("Lunes")
                    .contains("08:00")
                    .contains("17:00")
                    .contains("30");
        }

        @Test
        @DisplayName("Setters y getters funcionan correctamente")
        void settersYGetters_funcionan() {
            DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
            dto.setId(5L);
            dto.setProfesionalId(10L);
            dto.setDiaSemana(3);
            dto.setHoraInicio(LocalTime.of(9, 0));
            dto.setHoraFin(LocalTime.of(18, 0));
            dto.setDuracionCitaMinutos(45);

            assertThat(dto.getId()).isEqualTo(5L);
            assertThat(dto.getProfesionalId()).isEqualTo(10L);
            assertThat(dto.getDiaSemana()).isEqualTo(3);
            assertThat(dto.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
            assertThat(dto.getHoraFin()).isEqualTo(LocalTime.of(18, 0));
            assertThat(dto.getDuracionCitaMinutos()).isEqualTo(45);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new DisponibilidadSemanalDTO()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // DiaNoDisponibleDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("DiaNoDisponibleDTO")
    class DiaNoDisponibleDTOTest {

        @Test
        @DisplayName("getTipoLabel() retorna '🎉 Festivo' cuando tipo es 'FESTIVO'")
        void getTipoLabel_festivo_retornaEtiquetaFestivo() {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            dto.setTipo("FESTIVO");

            assertThat(dto.getTipoLabel()).isEqualTo("🎉 Festivo");
        }

        @Test
        @DisplayName("getTipoLabel() retorna '🔒 Bloqueo' cuando tipo es 'BLOQUEO_MANUAL'")
        void getTipoLabel_bloqueo_retornaEtiquetaBloqueo() {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            dto.setTipo("BLOQUEO_MANUAL");

            assertThat(dto.getTipoLabel()).isEqualTo("🔒 Bloqueo");
        }

        @Test
        @DisplayName("getTipoLabel() retorna '🔒 Bloqueo' para cualquier tipo no 'FESTIVO'")
        void getTipoLabel_otroTipo_retornaBloqueo() {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            dto.setTipo("DESCONOCIDO");

            assertThat(dto.getTipoLabel()).isEqualTo("🔒 Bloqueo");
        }

        @Test
        @DisplayName("toString() incluye fecha y motivo cuando motivo no es null")
        void toString_conMotivo_contieneAmbos() {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            dto.setFecha(LocalDate.of(2025, 12, 25));
            dto.setMotivo("Navidad");

            assertThat(dto.toString()).contains("2025-12-25").contains("Navidad");
        }

        @Test
        @DisplayName("toString() incluye fecha y tipo cuando motivo es null")
        void toString_sinMotivo_contieneFecharYTipo() {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            dto.setFecha(LocalDate.of(2025, 7, 4));
            dto.setMotivo(null);
            dto.setTipo("FESTIVO");

            assertThat(dto.toString()).contains("2025-07-04").contains("FESTIVO");
        }

        @Test
        @DisplayName("Setters y getters funcionan correctamente")
        void settersYGetters_funcionan() {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            LocalDate fecha = LocalDate.of(2025, 6, 20);
            dto.setId(7L);
            dto.setFecha(fecha);
            dto.setMotivo("Festivo nacional");
            dto.setTipo("FESTIVO");

            assertThat(dto.getId()).isEqualTo(7L);
            assertThat(dto.getFecha()).isEqualTo(fecha);
            assertThat(dto.getMotivo()).isEqualTo("Festivo nacional");
            assertThat(dto.getTipo()).isEqualTo("FESTIVO");
        }
    }

    // -----------------------------------------------------------------------
    // ConfiguracionAgendamientoDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("ConfiguracionAgendamientoDTO")
    class ConfiguracionAgendamientoDTOTest {

        @Test
        @DisplayName("Constructor con id y semanas asigna ambos campos")
        void constructor_asignaIdYSemanas() {
            ConfiguracionAgendamientoDTO dto = new ConfiguracionAgendamientoDTO(1L, 6);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getSemanasHabilitadas()).isEqualTo(6);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia con campos null")
        void noArgs_camposNull() {
            ConfiguracionAgendamientoDTO dto = new ConfiguracionAgendamientoDTO();

            assertThat(dto.getId()).isNull();
            assertThat(dto.getSemanasHabilitadas()).isNull();
        }

        @Test
        @DisplayName("setSemanasHabilitadas() actualiza el valor correctamente")
        void setter_actualizaSemanas() {
            ConfiguracionAgendamientoDTO dto = new ConfiguracionAgendamientoDTO(1L, 4);
            dto.setSemanasHabilitadas(8);

            assertThat(dto.getSemanasHabilitadas()).isEqualTo(8);
        }
    }

    // -----------------------------------------------------------------------
    // CitaDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("CitaDTO")
    class CitaDTOTest {

        @Test
        @DisplayName("Builder asigna todos los campos correctamente")
        void builder_asignaCamposCompletos() {
            ZonedDateTime ahora = ZonedDateTime.now();
            CitaDTO cita = CitaDTO.builder()
                    .id(1L)
                    .pacienteId(10L)
                    .profesionalId(20L)
                    .pacienteNombre("Juan Pérez")
                    .profesionalNombre("Dra. Ana Gómez")
                    .fechaHora(ahora)
                    .estado(EstadoCita.programada)
                    .build();

            assertThat(cita.getId()).isEqualTo(1L);
            assertThat(cita.getPacienteId()).isEqualTo(10L);
            assertThat(cita.getProfesionalId()).isEqualTo(20L);
            assertThat(cita.getPacienteNombre()).isEqualTo("Juan Pérez");
            assertThat(cita.getProfesionalNombre()).isEqualTo("Dra. Ana Gómez");
            assertThat(cita.getFechaHora()).isEqualTo(ahora);
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.programada);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new CitaDTO()).isNotNull();
        }

        @Test
        @DisplayName("Setter de estado muta el campo correctamente")
        void setter_estado_muta() {
            CitaDTO cita = new CitaDTO();
            cita.setEstado(EstadoCita.cancelada);

            assertThat(cita.getEstado()).isEqualTo(EstadoCita.cancelada);
        }
    }

    // -----------------------------------------------------------------------
    // UsuarioDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("UsuarioDTO")
    class UsuarioDTOTest {

        @Test
        @DisplayName("Builder asigna todos los campos correctamente")
        void builder_asignaCamposCompletos() {
            UsuarioDTO usuario = UsuarioDTO.builder()
                    .id(5L).nombreCompleto("María García")
                    .login("mgarcia").rol(RolUsuario.paciente).activo(true).build();

            assertThat(usuario.getId()).isEqualTo(5L);
            assertThat(usuario.getNombreCompleto()).isEqualTo("María García");
            assertThat(usuario.getLogin()).isEqualTo("mgarcia");
            assertThat(usuario.getRol()).isEqualTo(RolUsuario.paciente);
            assertThat(usuario.getActivo()).isTrue();
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new UsuarioDTO()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // LoginRequestDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("LoginRequestDTO")
    class LoginRequestDTOTest {

        @Test
        @DisplayName("Constructor asigna login y password")
        void constructor_asignaLoginYPassword() {
            LoginRequestDTO dto = new LoginRequestDTO("jperez", "Password1");

            assertThat(dto.getLogin()).isEqualTo("jperez");
            assertThat(dto.getPassword()).isEqualTo("Password1");
        }

        @Test
        @DisplayName("Setter de login actualiza el valor")
        void setter_login_actualiza() {
            LoginRequestDTO dto = new LoginRequestDTO("viejo", "pass");
            dto.setLogin("nuevo");

            assertThat(dto.getLogin()).isEqualTo("nuevo");
        }
    }

    // -----------------------------------------------------------------------
    // LoginResponseDTO
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("LoginResponseDTO")
    class LoginResponseDTOTest {

        @Test
        @DisplayName("Setters y getters funcionan correctamente")
        void settersYGetters_funcionan() {
            LoginResponseDTO dto = new LoginResponseDTO();
            dto.setAccessToken("jwt-access");
            dto.setRefreshToken("jwt-refresh");
            dto.setTipo("Bearer");
            dto.setExpiresIn(3600L);
            dto.setUsuarioId(42L);
            dto.setLogin("jperez");
            dto.setNombreCompleto("Juan Pérez");
            dto.setRol("paciente");

            assertThat(dto.getAccessToken()).isEqualTo("jwt-access");
            assertThat(dto.getRefreshToken()).isEqualTo("jwt-refresh");
            assertThat(dto.getTipo()).isEqualTo("Bearer");
            assertThat(dto.getExpiresIn()).isEqualTo(3600L);
            assertThat(dto.getUsuarioId()).isEqualTo(42L);
            assertThat(dto.getLogin()).isEqualTo("jperez");
            assertThat(dto.getNombreCompleto()).isEqualTo("Juan Pérez");
            assertThat(dto.getRol()).isEqualTo("paciente");
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new LoginResponseDTO()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Enums
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("EstadoCita enum")
    class EstadoCitaTest {

        @Test
        @DisplayName("Contiene exactamente tres estados")
        void tieneTresEstados() {
            assertThat(EstadoCita.values()).hasSize(3);
        }

        @Test
        @DisplayName("Contiene los estados esperados en minúsculas")
        void contieneEstadosEsperados() {
            assertThat(EstadoCita.values()).containsExactlyInAnyOrder(
                    EstadoCita.programada, EstadoCita.completada, EstadoCita.cancelada);
        }

        @Test
        @DisplayName("name() retorna en minúsculas")
        void nombre_enMinusculas() {
            assertThat(EstadoCita.programada.name()).isEqualTo("programada");
            assertThat(EstadoCita.completada.name()).isEqualTo("completada");
            assertThat(EstadoCita.cancelada.name()).isEqualTo("cancelada");
        }
    }

    @Nested
    @DisplayName("RolUsuario enum")
    class RolUsuarioTest {

        @Test
        @DisplayName("Contiene exactamente cuatro roles")
        void tieneCuatroRoles() {
            assertThat(RolUsuario.values()).hasSize(4);
        }

        @Test
        @DisplayName("Contiene todos los roles esperados")
        void contieneRolesEsperados() {
            assertThat(RolUsuario.values()).containsExactlyInAnyOrder(
                    RolUsuario.administrador, RolUsuario.paciente,
                    RolUsuario.profesional, RolUsuario.agendador);
        }
    }

    @Nested
    @DisplayName("TipoProfesional enum")
    class TipoProfesionalTest {

        @Test
        @DisplayName("Contiene exactamente dos tipos")
        void tieneDostipos() {
            assertThat(TipoProfesional.values()).hasSize(2);
        }

        @Test
        @DisplayName("Contiene medico y terapeuta")
        void contieneMedicoYTerapeuta() {
            assertThat(TipoProfesional.values()).containsExactlyInAnyOrder(
                    TipoProfesional.medico, TipoProfesional.terapeuta);
        }
    }
}
