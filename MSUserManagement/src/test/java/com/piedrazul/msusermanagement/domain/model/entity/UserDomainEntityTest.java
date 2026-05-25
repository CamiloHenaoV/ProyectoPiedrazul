package com.piedrazul.msusermanagement.domain.model.entity;

import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msusermanagement.domain.model.entity.enums.TipoProfesional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MSUserManagement — Entidades de dominio")
class UserDomainEntityTest {

    // -----------------------------------------------------------------------
    // Usuario
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Usuario")
    class UsuarioTest {

        @Test
        @DisplayName("Builder crea Usuario con todos los campos asignados")
        void builder_creaConCamposCompletos() {
            Usuario usuario = Usuario.builder()
                    .id(1L)
                    .login("jperez")
                    .nombreCompleto("Juan Pérez")
                    .rol(RolUsuario.paciente)
                    .activo(true)
                    .build();

            assertThat(usuario.getId()).isEqualTo(1L);
            assertThat(usuario.getLogin()).isEqualTo("jperez");
            assertThat(usuario.getNombreCompleto()).isEqualTo("Juan Pérez");
            assertThat(usuario.getRol()).isEqualTo(RolUsuario.paciente);
            assertThat(usuario.getActivo()).isTrue();
        }

        @Test
        @DisplayName("Setter de activo puede marcarlo como inactivo")
        void setter_activo_puedeMarcarse_inactivo() {
            Usuario usuario = Usuario.builder()
                    .login("u").nombreCompleto("N").rol(RolUsuario.agendador).activo(true).build();
            usuario.setActivo(false);

            assertThat(usuario.getActivo()).isFalse();
        }

        @Test
        @DisplayName("Setter de rol cambia el rol del usuario")
        void setter_rol_cambiaRol() {
            Usuario usuario = Usuario.builder()
                    .login("u").nombreCompleto("N").rol(RolUsuario.paciente).activo(true).build();
            usuario.setRol(RolUsuario.administrador);

            assertThat(usuario.getRol()).isEqualTo(RolUsuario.administrador);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new Usuario()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Paciente
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Paciente")
    class PacienteTest {

        @Test
        @DisplayName("Builder asigna los campos principales correctamente")
        void builder_asignaCamposPrincipales() {
            LocalDate nacimiento = LocalDate.of(1990, 5, 15);
            Paciente paciente = Paciente.builder()
                    .nombreCompleto("María García")
                    .cedulaIdentidad("12345678")
                    .fechaNacimiento(nacimiento)
                    .telefono("3001234567")
                    .email("maria@email.com")
                    .direccion("Calle 1 # 2-3")
                    .build();

            assertThat(paciente.getNombreCompleto()).isEqualTo("María García");
            assertThat(paciente.getCedulaIdentidad()).isEqualTo("12345678");
            assertThat(paciente.getFechaNacimiento()).isEqualTo(nacimiento);
            assertThat(paciente.getTelefono()).isEqualTo("3001234567");
            assertThat(paciente.getEmail()).isEqualTo("maria@email.com");
            assertThat(paciente.getDireccion()).isEqualTo("Calle 1 # 2-3");
        }

        @Test
        @DisplayName("Setter de telefono actualiza el valor")
        void setter_telefono_actualiza() {
            Paciente p = new Paciente();
            p.setTelefono("3109998877");
            assertThat(p.getTelefono()).isEqualTo("3109998877");
        }

        @Test
        @DisplayName("Setter de email actualiza el valor")
        void setter_email_actualiza() {
            Paciente p = new Paciente();
            p.setEmail("nuevo@email.com");
            assertThat(p.getEmail()).isEqualTo("nuevo@email.com");
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new Paciente()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Profesional
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Profesional")
    class ProfesionalTest {

        @Test
        @DisplayName("Builder asigna tipo, licencia, activo y duracionCitaMinutos")
        void builder_asignaCamposPrincipales() {
            Especialidad esp = Especialidad.builder().id(1).nombre("Medicina General").build();
            Profesional prof = Profesional.builder()
                    .tipo(TipoProfesional.medico)
                    .especialidad(esp)
                    .licenciaProfesional("LIC-001")
                    .activo(true)
                    .duracionCitaMinutos(30)
                    .build();

            assertThat(prof.getTipo()).isEqualTo(TipoProfesional.medico);
            assertThat(prof.getEspecialidad()).isEqualTo(esp);
            assertThat(prof.getLicenciaProfesional()).isEqualTo("LIC-001");
            assertThat(prof.getActivo()).isTrue();
            assertThat(prof.getDuracionCitaMinutos()).isEqualTo(30);
        }

        @Test
        @DisplayName("Setter de activo puede desactivar al profesional")
        void setter_activo_puedDesactivar() {
            Profesional prof = Profesional.builder()
                    .tipo(TipoProfesional.terapeuta).licenciaProfesional("LIC-002")
                    .activo(true).duracionCitaMinutos(45).build();
            prof.setActivo(false);

            assertThat(prof.getActivo()).isFalse();
        }

        @Test
        @DisplayName("Setter de duracionCitaMinutos actualiza la duración")
        void setter_duracion_actualiza() {
            Profesional prof = new Profesional();
            prof.setDuracionCitaMinutos(60);
            assertThat(prof.getDuracionCitaMinutos()).isEqualTo(60);
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new Profesional()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Especialidad
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Especialidad")
    class EspecialidadTest {

        @Test
        @DisplayName("Builder asigna id y nombre")
        void builder_asignaIdYNombre() {
            Especialidad esp = Especialidad.builder()
                    .id(3)
                    .nombre("Fisioterapia")
                    .build();

            assertThat(esp.getId()).isEqualTo(3);
            assertThat(esp.getNombre()).isEqualTo("Fisioterapia");
        }

        @Test
        @DisplayName("Setter de nombre actualiza el valor")
        void setter_nombre_actualiza() {
            Especialidad esp = new Especialidad();
            esp.setNombre("Odontología");
            assertThat(esp.getNombre()).isEqualTo("Odontología");
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new Especialidad()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Enums de MSUserManagement
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RolUsuario enum (MSUserManagement)")
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

        @Test
        @DisplayName("name() retorna en minúsculas")
        void nombre_enMinusculas() {
            assertThat(RolUsuario.paciente.name()).isEqualTo("paciente");
            assertThat(RolUsuario.profesional.name()).isEqualTo("profesional");
        }
    }

    @Nested
    @DisplayName("TipoProfesional enum")
    class TipoProfesionalEnumTest {

        @Test
        @DisplayName("Contiene exactamente dos valores: medico y terapeuta")
        void tieneDosTipos() {
            assertThat(TipoProfesional.values())
                    .containsExactlyInAnyOrder(TipoProfesional.medico, TipoProfesional.terapeuta);
        }

        @Test
        @DisplayName("name() retorna en minúsculas")
        void nombre_enMinusculas() {
            assertThat(TipoProfesional.medico.name()).isEqualTo("medico");
            assertThat(TipoProfesional.terapeuta.name()).isEqualTo("terapeuta");
        }
    }
}
