package com.piedrazul.msusermanagement.application.service.template;

import com.piedrazul.msusermanagement.application.service.interfaces.IPacienteService;
import com.piedrazul.msusermanagement.application.service.interfaces.IProfesionalService;
import com.piedrazul.msusermanagement.application.service.interfaces.IUsuarioService;
import com.piedrazul.msusermanagement.application.service.template.impl.RegistroAdminService;
import com.piedrazul.msusermanagement.application.service.template.impl.RegistroPacienteService;
import com.piedrazul.msusermanagement.application.service.template.impl.RegistroProfesionalService;
import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Especialidad;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msusermanagement.domain.model.entity.enums.TipoProfesional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Patrón Template Method — RegistroTemplate y subclases")
class RegistroTemplateTest {

    @Mock private IUsuarioService     usuarioService;
    @Mock private IPacienteService    pacienteService;
    @Mock private IProfesionalService profesionalService;

    private static final UsuarioDTO USUARIO_DTO = UsuarioDTO.builder()
            .id(1L).nombreCompleto("Test User").login("tuser")
            .rol(RolUsuario.paciente).activo(true).build();

    private static final Usuario USUARIO_ENTIDAD;
    static {
        USUARIO_ENTIDAD = new Usuario();
        USUARIO_ENTIDAD.setId(1L);
        USUARIO_ENTIDAD.setNombreCompleto("Test User");
        USUARIO_ENTIDAD.setLogin("tuser");
        USUARIO_ENTIDAD.setRol(RolUsuario.paciente);
        USUARIO_ENTIDAD.setActivo(true);
    }

    @BeforeEach
    void stubCrearUsuarioBase() {
        when(usuarioService.crearUsuarioBase(any())).thenReturn(USUARIO_ENTIDAD);
    }

    // -----------------------------------------------------------------------
    // RegistroAdminService
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RegistroAdminService")
    class RegistroAdminServiceTest {

        private RegistroAdminService adminService;

        @BeforeEach
        void setUp() {
            adminService = new RegistroAdminService(usuarioService);
        }

        @Test
        @DisplayName("registrar() llama a crearUsuarioBase() exactamente una vez")
        void registrar_llamaCrearUsuarioBase() {
            adminService.registrar(contextoAdmin());
            verify(usuarioService, times(1)).crearUsuarioBase(any());
        }

        @Test
        @DisplayName("registrar() retorna el DTO con los datos del usuario creado")
        void registrar_retornaDTOCorrecto() {
            UsuarioDTO resultado = adminService.registrar(contextoAdmin());

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getLogin()).isEqualTo("tuser");
            assertThat(resultado.getNombreCompleto()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("vincularPerfil() es no-op: no llama a ningún servicio de perfil")
        void vincularPerfil_esNoOp() {
            adminService.registrar(contextoAdmin());
            // No se inyectó ni llamó ningún servicio de perfil
            verifyNoInteractions(pacienteService, profesionalService);
        }

        @Test
        @DisplayName("registrar() preserva el rol del usuario creado")
        void registrar_preservaRol() {
            UsuarioDTO resultado = adminService.registrar(contextoAdmin());
            assertThat(resultado.getRol()).isEqualTo(RolUsuario.paciente);
        }
    }

    // -----------------------------------------------------------------------
    // RegistroPacienteService
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RegistroPacienteService")
    class RegistroPacienteServiceTest {

        private RegistroPacienteService pacienteRegistroService;

        @BeforeEach
        void setUp() {
            pacienteRegistroService = new RegistroPacienteService(usuarioService, pacienteService);
        }

        @Test
        @DisplayName("registrar() llama a crearUsuarioBase() exactamente una vez")
        void registrar_llamaCrearUsuarioBase() {
            pacienteRegistroService.registrar(contextoPaciente());
            verify(usuarioService, times(1)).crearUsuarioBase(any());
        }

        @Test
        @DisplayName("vincularPerfil() llama a pacienteService.crearPaciente() exactamente una vez")
        void vincularPerfil_llamaCrearPaciente() {
            pacienteRegistroService.registrar(contextoPaciente());
            verify(pacienteService, times(1)).crearPaciente(any(), any());
        }

        @Test
        @DisplayName("vincularPerfil() pasa la entidad Usuario creada en el paso 1")
        void vincularPerfil_pasaUsuarioCorrectoAlServicioPerfil() {
            pacienteRegistroService.registrar(contextoPaciente());
            verify(pacienteService).crearPaciente(eq(USUARIO_ENTIDAD), any());
        }

        @Test
        @DisplayName("registrar() retorna el DTO con los datos del usuario creado")
        void registrar_retornaDTOCorrecto() {
            UsuarioDTO resultado = pacienteRegistroService.registrar(contextoPaciente());
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getLogin()).isEqualTo("tuser");
        }

        @Test
        @DisplayName("No llama a profesionalService en ningún momento")
        void noLlamaProfesionalService() {
            pacienteRegistroService.registrar(contextoPaciente());
            verifyNoInteractions(profesionalService);
        }

        @Test
        @DisplayName("El orden es: crearUsuarioBase → crearPaciente → retornar DTO")
        void orden_esCrearUsuarioBaseAntesDePerfil() {
            var orden = inOrder(usuarioService, pacienteService);
            pacienteRegistroService.registrar(contextoPaciente());
            orden.verify(usuarioService).crearUsuarioBase(any());
            orden.verify(pacienteService).crearPaciente(any(), any());
        }
    }

    // -----------------------------------------------------------------------
    // RegistroProfesionalService
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RegistroProfesionalService")
    class RegistroProfesionalServiceTest {

        private RegistroProfesionalService profesionalRegistroService;

        @BeforeEach
        void setUp() {
            profesionalRegistroService =
                    new RegistroProfesionalService(usuarioService, profesionalService);
        }

        @Test
        @DisplayName("registrar() llama a crearUsuarioBase() exactamente una vez")
        void registrar_llamaCrearUsuarioBase() {
            profesionalRegistroService.registrar(contextoProfesional());
            verify(usuarioService, times(1)).crearUsuarioBase(any());
        }

        @Test
        @DisplayName("vincularPerfil() llama a profesionalService.crearProfesional() exactamente una vez")
        void vincularPerfil_llamaCrearProfesional() {
            profesionalRegistroService.registrar(contextoProfesional());
            verify(profesionalService, times(1)).crearProfesional(any(), any());
        }

        @Test
        @DisplayName("vincularPerfil() pasa la entidad Usuario creada en el paso 1")
        void vincularPerfil_pasaUsuarioCorrectoAlServicioPerfil() {
            profesionalRegistroService.registrar(contextoProfesional());
            verify(profesionalService).crearProfesional(eq(USUARIO_ENTIDAD), any());
        }

        @Test
        @DisplayName("registrar() retorna el DTO con los datos del usuario creado")
        void registrar_retornaDTOCorrecto() {
            UsuarioDTO resultado = profesionalRegistroService.registrar(contextoProfesional());
            assertThat(resultado.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("No llama a pacienteService en ningún momento")
        void noLlamaPacienteService() {
            profesionalRegistroService.registrar(contextoProfesional());
            verifyNoInteractions(pacienteService);
        }

        @Test
        @DisplayName("El orden es: crearUsuarioBase → crearProfesional → retornar DTO")
        void orden_esCrearUsuarioBaseAntesDePerfil() {
            var orden = inOrder(usuarioService, profesionalService);
            profesionalRegistroService.registrar(contextoProfesional());
            orden.verify(usuarioService).crearUsuarioBase(any());
            orden.verify(profesionalService).crearProfesional(any(), any());
        }
    }

    // -----------------------------------------------------------------------
    // RegistroContexto — record
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RegistroContexto (record)")
    class RegistroContextoTest {

        @Test
        @DisplayName("usuarioDTO() retorna el DTO asignado en el constructor")
        void usuarioDTO_retornaValorCorrecto() {
            RegistroContexto ctx = new RegistroContexto(USUARIO_DTO, null, null);
            assertThat(ctx.usuarioDTO()).isEqualTo(USUARIO_DTO);
        }

        @Test
        @DisplayName("pacienteDTO() retorna null cuando no se proporciona perfil de paciente")
        void pacienteDTO_retornaNullSiNoAplica() {
            RegistroContexto ctx = contextoAdmin();
            assertThat(ctx.pacienteDTO()).isNull();
        }

        @Test
        @DisplayName("profesionalDTO() retorna null cuando no se proporciona perfil de profesional")
        void profesionalDTO_retornaNullSiNoAplica() {
            RegistroContexto ctx = contextoPaciente();
            assertThat(ctx.profesionalDTO()).isNull();
        }

        @Test
        @DisplayName("Todos los campos son accesibles desde el registro")
        void todosLosCamposAccesibles() {
            PacienteDTO pDto = pacienteDTO();
            RegistroContexto ctx = new RegistroContexto(USUARIO_DTO, pDto, null);

            assertThat(ctx.usuarioDTO()).isEqualTo(USUARIO_DTO);
            assertThat(ctx.pacienteDTO()).isEqualTo(pDto);
            assertThat(ctx.profesionalDTO()).isNull();
        }
    }

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------
    private RegistroContexto contextoAdmin() {
        return new RegistroContexto(USUARIO_DTO, null, null);
    }

    private RegistroContexto contextoPaciente() {
        return new RegistroContexto(USUARIO_DTO, pacienteDTO(), null);
    }

    private RegistroContexto contextoProfesional() {
        return new RegistroContexto(USUARIO_DTO, null, profesionalDTO());
    }

    private PacienteDTO pacienteDTO() {
        return PacienteDTO.builder()
                .nombreCompleto("Test Paciente")
                .cedulaIdentidad("12345678")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefono("3001234567")
                .email("paciente@test.com")
                .build();
    }

    private ProfesionalDTO profesionalDTO() {
        return ProfesionalDTO.builder()
                .tipo(TipoProfesional.medico)
                .especialidadNombre("Medicina General")
                .licenciaProfesional("LIC-001")
                .duracionCitaMinutos(30)
                .build();
    }
}
