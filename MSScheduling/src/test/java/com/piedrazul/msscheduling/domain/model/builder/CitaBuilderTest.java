package com.piedrazul.msscheduling.domain.model.builder;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Patrón Builder — CitaProgramadaBuilder / CitaUrgenteBuilder / DirectorCita")
class CitaBuilderTest {

    private UsuarioLocal paciente;
    private UsuarioLocal profesional;
    private ZonedDateTime fechaHora;
    private static final int DURACION = 30;

    @BeforeEach
    void setUp() {
        paciente = UsuarioLocal.builder()
                .id(1L).nombreCompleto("Juan Pérez").login("jperez")
                .rol(RolUsuario.paciente).activo(true).build();

        profesional = UsuarioLocal.builder()
                .id(2L).nombreCompleto("Dra. Ana Gómez").login("agomez")
                .rol(RolUsuario.profesional).activo(true).build();

        fechaHora = ZonedDateTime.now().plusDays(3);
    }

    // -------------------------------------------------------------------
    // CitaProgramadaBuilder
    // -------------------------------------------------------------------
    @Nested
    @DisplayName("CitaProgramadaBuilder")
    class CitaProgramadaBuilderTests {

        private CitaProgramadaBuilder builder;

        @BeforeEach
        void setUp() {
            builder = new CitaProgramadaBuilder();
            builder.iniciarNuevaCita();
        }

        @Test
        @DisplayName("buildPaciente() asigna id y nombre del paciente")
        void buildPaciente_asignaIdYNombre() {
            builder.buildPaciente(paciente);
            assertThat(builder.getCita().getPacienteId()).isEqualTo(1L);
            assertThat(builder.getCita().getPacienteNombre()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("buildProfesional() asigna id y nombre del profesional")
        void buildProfesional_asignaIdYNombre() {
            builder.buildProfesional(profesional);
            assertThat(builder.getCita().getProfesionalId()).isEqualTo(2L);
            assertThat(builder.getCita().getProfesionalNombre()).isEqualTo("Dra. Ana Gómez");
        }

        @Test
        @DisplayName("buildFechaHora() asigna la fecha y hora")
        void buildFechaHora_asignaFechaHora() {
            builder.buildFechaHora(fechaHora);
            assertThat(builder.getCita().getFechaHora()).isEqualTo(fechaHora);
        }

        @Test
        @DisplayName("buildDuracion() asigna la duración en minutos")
        void buildDuracion_asignaDuracion() {
            builder.buildDuracion(DURACION);
            assertThat(builder.getCita().getDuracionMinutos()).isEqualTo(DURACION);
        }

        @Test
        @DisplayName("buildEstado() siempre fija el estado en programada (argumento ignorado)")
        void buildEstado_fijaProgramadaSinImportarArgumento() {
            builder.buildEstado(EstadoCita.cancelada);
            assertThat(builder.getCita().getEstado()).isEqualTo(EstadoCita.programada);
        }

        @Test
        @DisplayName("buildFechaCreacion() asigna creadoEn cercano al instante actual")
        void buildFechaCreacion_asignaAhora() {
            ZonedDateTime antes = ZonedDateTime.now().minusSeconds(1);
            builder.buildFechaCreacion();
            ZonedDateTime despues = ZonedDateTime.now().plusSeconds(1);

            assertThat(builder.getCita().getCreadoEn())
                    .isAfterOrEqualTo(antes)
                    .isBeforeOrEqualTo(despues);
        }

        @Test
        @DisplayName("getCita() retorna la misma instancia en llamadas sucesivas")
        void getCita_retornaMismaInstancia() {
            assertThat(builder.getCita()).isSameAs(builder.getCita());
        }
    }

    // -------------------------------------------------------------------
    // CitaUrgenteBuilder
    // -------------------------------------------------------------------
    @Nested
    @DisplayName("CitaUrgenteBuilder")
    class CitaUrgenteBuilderTests {

        private CitaUrgenteBuilder builder;

        @BeforeEach
        void setUp() {
            builder = new CitaUrgenteBuilder();
            builder.iniciarNuevaCita();
        }

        @Test
        @DisplayName("buildPaciente() asigna id y nombre del paciente")
        void buildPaciente_asignaIdYNombre() {
            builder.buildPaciente(paciente);
            assertThat(builder.getCita().getPacienteId()).isEqualTo(1L);
            assertThat(builder.getCita().getPacienteNombre()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("buildProfesional() asigna id y nombre del profesional")
        void buildProfesional_asignaIdYNombre() {
            builder.buildProfesional(profesional);
            assertThat(builder.getCita().getProfesionalId()).isEqualTo(2L);
            assertThat(builder.getCita().getProfesionalNombre()).isEqualTo("Dra. Ana Gómez");
        }

        @Test
        @DisplayName("buildFechaHora() asigna la fecha y hora")
        void buildFechaHora_asignaFechaHora() {
            builder.buildFechaHora(fechaHora);
            assertThat(builder.getCita().getFechaHora()).isEqualTo(fechaHora);
        }

        @Test
        @DisplayName("buildDuracion() asigna la duración en minutos")
        void buildDuracion_asignaDuracion() {
            builder.buildDuracion(DURACION);
            assertThat(builder.getCita().getDuracionMinutos()).isEqualTo(DURACION);
        }

        @Test
        @DisplayName("buildEstado() siempre fija el estado en programada")
        void buildEstado_fijaProgramada() {
            builder.buildEstado(EstadoCita.completada);
            assertThat(builder.getCita().getEstado()).isEqualTo(EstadoCita.programada);
        }

        @Test
        @DisplayName("buildFechaCreacion() asigna creadoEn cercano al instante actual")
        void buildFechaCreacion_asignaAhora() {
            ZonedDateTime antes = ZonedDateTime.now().minusSeconds(1);
            builder.buildFechaCreacion();
            ZonedDateTime despues = ZonedDateTime.now().plusSeconds(1);

            assertThat(builder.getCita().getCreadoEn())
                    .isAfterOrEqualTo(antes)
                    .isBeforeOrEqualTo(despues);
        }
    }

    // -------------------------------------------------------------------
    // DirectorCita
    // -------------------------------------------------------------------
    @Nested
    @DisplayName("DirectorCita")
    class DirectorCitaTest {

        private DirectorCita director;

        @BeforeEach
        void setUp() {
            director = new DirectorCita();
        }

        @Test
        @DisplayName("construirCita() con CitaProgramadaBuilder produce una Cita completa y programada")
        void construirCita_conProgramadaBuilder_produceCitaCompleta() {
            director.setCitaBuilder(new CitaProgramadaBuilder());
            director.construirCita(paciente, profesional, fechaHora, DURACION);
            Cita cita = director.getCita();

            assertThat(cita).isNotNull();
            assertThat(cita.getPacienteId()).isEqualTo(1L);
            assertThat(cita.getPacienteNombre()).isEqualTo("Juan Pérez");
            assertThat(cita.getProfesionalId()).isEqualTo(2L);
            assertThat(cita.getProfesionalNombre()).isEqualTo("Dra. Ana Gómez");
            assertThat(cita.getFechaHora()).isEqualTo(fechaHora);
            assertThat(cita.getDuracionMinutos()).isEqualTo(DURACION);
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.programada);
            assertThat(cita.getCreadoEn()).isNotNull();
        }

        @Test
        @DisplayName("construirCita() con CitaUrgenteBuilder produce una Cita completa y programada")
        void construirCita_conUrgenteBuilder_produceCitaCompleta() {
            director.setCitaBuilder(new CitaUrgenteBuilder());
            director.construirCita(paciente, profesional, fechaHora, DURACION);
            Cita cita = director.getCita();

            assertThat(cita).isNotNull();
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.programada);
            assertThat(cita.getDuracionMinutos()).isEqualTo(DURACION);
            assertThat(cita.getCreadoEn()).isNotNull();
        }

        @Test
        @DisplayName("construirCita() invocada dos veces produce instancias de Cita distintas")
        void construirCita_llamadasMultiples_producenCitasDistintas() {
            director.setCitaBuilder(new CitaProgramadaBuilder());

            director.construirCita(paciente, profesional, fechaHora, DURACION);
            Cita primera = director.getCita();

            ZonedDateTime otraFecha = fechaHora.plusDays(1);
            director.construirCita(paciente, profesional, otraFecha, 60);
            Cita segunda = director.getCita();

            assertThat(primera).isNotSameAs(segunda);
            assertThat(segunda.getFechaHora()).isEqualTo(otraFecha);
            assertThat(segunda.getDuracionMinutos()).isEqualTo(60);
        }

        @Test
        @DisplayName("setCitaBuilder() permite intercambiar el builder en tiempo de ejecución")
        void setCitaBuilder_permiteIntercambio() {
            director.setCitaBuilder(new CitaProgramadaBuilder());
            director.construirCita(paciente, profesional, fechaHora, DURACION);
            assertThat(director.getCita().getEstado()).isEqualTo(EstadoCita.programada);

            director.setCitaBuilder(new CitaUrgenteBuilder());
            director.construirCita(paciente, profesional, fechaHora, DURACION);
            assertThat(director.getCita().getEstado()).isEqualTo(EstadoCita.programada);
        }
    }
}
