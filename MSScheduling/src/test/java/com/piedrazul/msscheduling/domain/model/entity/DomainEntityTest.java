package com.piedrazul.msscheduling.domain.model.entity;

import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msscheduling.domain.model.entity.enums.TipoDiaNoDisponible;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entidades de dominio")
class DomainEntityTest {

    // -----------------------------------------------------------------------
    // Cita
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Cita")
    class CitaTest {

        @Test
        @DisplayName("Estado por defecto es programada")
        void estadoDefecto_esProgramada() {
            Cita cita = new Cita();
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.programada);
        }

        @Test
        @DisplayName("Builder crea Cita con todos los campos asignados")
        void builder_creaConCamposCompletos() {
            ZonedDateTime ahora = ZonedDateTime.now();
            Cita cita = Cita.builder()
                    .pacienteId(1L)
                    .pacienteNombre("Carlos Torres")
                    .profesionalId(2L)
                    .profesionalNombre("Dr. López")
                    .fechaHora(ahora)
                    .duracionMinutos(45)
                    .estado(EstadoCita.programada)
                    .creadoEn(ahora)
                    .build();

            assertThat(cita.getPacienteId()).isEqualTo(1L);
            assertThat(cita.getPacienteNombre()).isEqualTo("Carlos Torres");
            assertThat(cita.getProfesionalId()).isEqualTo(2L);
            assertThat(cita.getProfesionalNombre()).isEqualTo("Dr. López");
            assertThat(cita.getFechaHora()).isEqualTo(ahora);
            assertThat(cita.getDuracionMinutos()).isEqualTo(45);
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.programada);
            assertThat(cita.getCreadoEn()).isEqualTo(ahora);
        }

        @Test
        @DisplayName("Setter de estado muta la entidad correctamente")
        void setter_cambiasEstado() {
            Cita cita = new Cita();
            cita.setEstado(EstadoCita.cancelada);

            assertThat(cita.getEstado()).isEqualTo(EstadoCita.cancelada);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgsConstrutor_creaInstancia() {
            assertThat(new Cita()).isNotNull();
        }

        @Test
        @DisplayName("AllArgsConstructor asigna todos los campos")
        void allArgsConstrutor_asignaCampos() {
            ZonedDateTime ts = ZonedDateTime.now();
            Cita cita = new Cita(10L, 1L, "Paciente", 2L, "Profesional",
                    ts, 30, EstadoCita.completada, ts);

            assertThat(cita.getId()).isEqualTo(10L);
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.completada);
        }
    }

    // -----------------------------------------------------------------------
    // ConfiguracionAgendamiento
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("ConfiguracionAgendamiento")
    class ConfiguracionAgendamientoTest {

        @Test
        @DisplayName("Valor por defecto de semanasHabilitadas es 4")
        void valorDefecto_semanasHabilitadas_esCuatro() {
            ConfiguracionAgendamiento config = ConfiguracionAgendamiento.builder()
                    .id(1L)
                    .build();

            assertThat(config.getSemanasHabilitadas()).isEqualTo(4);
        }

        @Test
        @DisplayName("Builder respeta el valor de semanasHabilitadas cuando se especifica")
        void builder_respetaSemanasHabilitadas() {
            ConfiguracionAgendamiento config = ConfiguracionAgendamiento.builder()
                    .id(1L)
                    .semanasHabilitadas(8)
                    .build();

            assertThat(config.getSemanasHabilitadas()).isEqualTo(8);
        }

        @Test
        @DisplayName("Setter modifica semanasHabilitadas")
        void setter_modificaSemanasHabilitadas() {
            ConfiguracionAgendamiento config = new ConfiguracionAgendamiento();
            config.setSemanasHabilitadas(12);

            assertThat(config.getSemanasHabilitadas()).isEqualTo(12);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgsConstrutor_creaInstancia() {
            assertThat(new ConfiguracionAgendamiento()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // DiaNoDisponible
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("DiaNoDisponible")
    class DiaNoDisponibleTest {

        @Test
        @DisplayName("Tipo por defecto es BLOQUEO_MANUAL")
        void tipoPorDefecto_esBloqueManual() {
            DiaNoDisponible dia = DiaNoDisponible.builder()
                    .fecha(LocalDate.now())
                    .build();

            assertThat(dia.getTipo()).isEqualTo(TipoDiaNoDisponible.BLOQUEO_MANUAL);
        }

        @Test
        @DisplayName("Builder con tipo FESTIVO asigna el tipo correcto")
        void builder_conFestivo_asignaTipo() {
            DiaNoDisponible dia = DiaNoDisponible.builder()
                    .fecha(LocalDate.of(2025, 12, 25))
                    .motivo("Navidad")
                    .tipo(TipoDiaNoDisponible.FESTIVO)
                    .build();

            assertThat(dia.getTipo()).isEqualTo(TipoDiaNoDisponible.FESTIVO);
            assertThat(dia.getMotivo()).isEqualTo("Navidad");
            assertThat(dia.getFecha()).isEqualTo(LocalDate.of(2025, 12, 25));
        }

        @Test
        @DisplayName("Setters mutan correctamente la entidad")
        void setters_mutanEntidad() {
            DiaNoDisponible dia = new DiaNoDisponible();
            LocalDate fecha = LocalDate.of(2025, 7, 4);
            dia.setFecha(fecha);
            dia.setMotivo("Festivo nacional");
            dia.setTipo(TipoDiaNoDisponible.FESTIVO);

            assertThat(dia.getFecha()).isEqualTo(fecha);
            assertThat(dia.getMotivo()).isEqualTo("Festivo nacional");
            assertThat(dia.getTipo()).isEqualTo(TipoDiaNoDisponible.FESTIVO);
        }
    }

    // -----------------------------------------------------------------------
    // DisponibilidadSemanal
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("DisponibilidadSemanal")
    class DisponibilidadSemanalTest {

        @Test
        @DisplayName("Builder asigna profesionalId, diaSemana, horaInicio y horaFin")
        void builder_asignaCamposPrincipales() {
            DisponibilidadSemanal d = DisponibilidadSemanal.builder()
                    .profesionalId(10L)
                    .diaSemana(1)   // Lunes
                    .horaInicio(LocalTime.of(8, 0))
                    .horaFin(LocalTime.of(17, 0))
                    .duracionCitaMinutos(30)
                    .build();

            assertThat(d.getProfesionalId()).isEqualTo(10L);
            assertThat(d.getDiaSemana()).isEqualTo(1);
            assertThat(d.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
            assertThat(d.getHoraFin()).isEqualTo(LocalTime.of(17, 0));
            assertThat(d.getDuracionCitaMinutos()).isEqualTo(30);
        }

        @Test
        @DisplayName("Setters mutan correctamente los campos")
        void setters_mutanCampos() {
            DisponibilidadSemanal d = new DisponibilidadSemanal();
            d.setProfesionalId(5L);
            d.setDiaSemana(3);
            d.setHoraInicio(LocalTime.of(9, 0));
            d.setHoraFin(LocalTime.of(13, 0));

            assertThat(d.getProfesionalId()).isEqualTo(5L);
            assertThat(d.getDiaSemana()).isEqualTo(3);
        }
    }

    // -----------------------------------------------------------------------
    // BloqueoDisponibilidad
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("BloqueoDisponibilidad")
    class BloqueoDisponibilidadTest {

        @Test
        @DisplayName("Builder asigna profesionalId, fechaInicio, fechaFin y motivo")
        void builder_asignaCampos() {
            ZonedDateTime inicio = ZonedDateTime.now();
            ZonedDateTime fin = inicio.plusHours(4);

            BloqueoDisponibilidad bloqueo = BloqueoDisponibilidad.builder()
                    .profesionalId(3L)
                    .fechaInicio(inicio)
                    .fechaFin(fin)
                    .motivo("Capacitación interna")
                    .build();

            assertThat(bloqueo.getProfesionalId()).isEqualTo(3L);
            assertThat(bloqueo.getFechaInicio()).isEqualTo(inicio);
            assertThat(bloqueo.getFechaFin()).isEqualTo(fin);
            assertThat(bloqueo.getMotivo()).isEqualTo("Capacitación interna");
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgsConstrutor_creaInstancia() {
            assertThat(new BloqueoDisponibilidad()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // UsuarioLocal
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("UsuarioLocal")
    class UsuarioLocalTest {

        @Test
        @DisplayName("Builder asigna todos los campos correctamente")
        void builder_asignaTodosLosCampos() {
            UsuarioLocal usuario = UsuarioLocal.builder()
                    .id(1L)
                    .nombreCompleto("María García")
                    .login("mgarcia")
                    .rol(RolUsuario.paciente)
                    .activo(true)
                    .build();

            assertThat(usuario.getId()).isEqualTo(1L);
            assertThat(usuario.getNombreCompleto()).isEqualTo("María García");
            assertThat(usuario.getLogin()).isEqualTo("mgarcia");
            assertThat(usuario.getRol()).isEqualTo(RolUsuario.paciente);
            assertThat(usuario.getActivo()).isTrue();
        }

        @Test
        @DisplayName("Setter de activo puede marcarlo como inactivo")
        void setter_activoPuedeMarcarse_inactivo() {
            UsuarioLocal usuario = UsuarioLocal.builder()
                    .id(2L).nombreCompleto("Luis").login("lperez")
                    .rol(RolUsuario.profesional).activo(true).build();

            usuario.setActivo(false);

            assertThat(usuario.getActivo()).isFalse();
        }

        @Test
        @DisplayName("getRol() retorna el rol asignado al construir")
        void getRol_retornaRolCorrecto() {
            UsuarioLocal admin = UsuarioLocal.builder()
                    .id(3L).nombreCompleto("Admin").login("admin")
                    .rol(RolUsuario.administrador).activo(true).build();

            assertThat(admin.getRol()).isEqualTo(RolUsuario.administrador);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgsConstrutor_creaInstancia() {
            assertThat(new UsuarioLocal()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Enums de dominio
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("EstadoCita enum")
    class EstadoCitaEnumTest {

        @Test
        @DisplayName("Contiene exactamente tres valores: programada, cancelada, completada")
        void tieneExactamenteTresValores() {
            assertThat(EstadoCita.values()).hasSize(3);
        }

        @Test
        @DisplayName("name() retorna el nombre en minúsculas tal como está declarado")
        void nombre_enMinusculas() {
            assertThat(EstadoCita.programada.name()).isEqualTo("programada");
            assertThat(EstadoCita.cancelada.name()).isEqualTo("cancelada");
            assertThat(EstadoCita.completada.name()).isEqualTo("completada");
        }
    }

    @Nested
    @DisplayName("TipoDiaNoDisponible enum")
    class TipoDiaNoDisponibleEnumTest {

        @Test
        @DisplayName("Contiene exactamente dos valores: FESTIVO y BLOQUEO_MANUAL")
        void tieneExactamenteDosValores() {
            assertThat(TipoDiaNoDisponible.values()).hasSize(2);
        }

        @Test
        @DisplayName("name() retorna en mayúsculas")
        void nombre_enMayusculas() {
            assertThat(TipoDiaNoDisponible.FESTIVO.name()).isEqualTo("FESTIVO");
            assertThat(TipoDiaNoDisponible.BLOQUEO_MANUAL.name()).isEqualTo("BLOQUEO_MANUAL");
        }
    }

    @Nested
    @DisplayName("RolUsuario enum")
    class RolUsuarioEnumTest {

        @Test
        @DisplayName("Contiene los cuatro roles del sistema")
        void tieneLosCuatroRoles() {
            assertThat(RolUsuario.values())
                    .containsExactlyInAnyOrder(
                            RolUsuario.profesional,
                            RolUsuario.agendador,
                            RolUsuario.administrador,
                            RolUsuario.paciente
                    );
        }
    }
}
