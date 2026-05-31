package com.piedrazul.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.*;
import com.piedrazul.frontend.model.enums.EstadoCita;
import com.piedrazul.frontend.model.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FxClient — Clientes HTTP")
class ClientTest {

    @Mock private ApiClient api;

    /** Mapper real compartido para serializar los objetos de respuesta en los stubs. */
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ═══════════════════════════════════════════════════════════════════════
    // AuthClient
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("AuthClient")
    class AuthClientTest {

        private AuthClient client;

        @BeforeEach
        void setUp() {
            client = new AuthClient(api);
        }

        // ── login() ────────────────────────────────────────────────────────

        @Test
        @DisplayName("login() retorna LoginResponseDTO cuando el servidor responde 200")
        void login_200_retornaDTO() throws Exception {
            LoginResponseDTO response = loginResponse("jperez", "paciente");
            stubPost(200, mapper.writeValueAsString(response));

            LoginResponseDTO resultado = client.login("jperez", "Password1");

            assertThat(resultado.getLogin()).isEqualTo("jperez");
            assertThat(resultado.getRol()).isEqualTo("paciente");
        }

        @Test
        @DisplayName("login() lanza HttpException(401) cuando el servidor responde 401")
        void login_401_lanzaHttpException401() throws Exception {
            stubPost(401, "");

            assertThatThrownBy(() -> client.login("jperez", "wrongpass"))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(401));
        }

        @Test
        @DisplayName("login() lanza HttpException cuando el servidor responde 500")
        void login_500_lanzaHttpException() throws Exception {
            stubPost(500, "");

            assertThatThrownBy(() -> client.login("jperez", "pass"))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(500));
        }

        @Test
        @DisplayName("login() llama a api.post() con la ruta correcta")
        void login_llamaApiPost_conRutaCorrecta() throws Exception {
            stubPost(200, mapper.writeValueAsString(loginResponse("u", "r")));

            client.login("u", "p");

            verify(api).post(eq("/api/auth/login"), any());
        }

        // ── registrarCredencial() ──────────────────────────────────────────

        @Test
        @DisplayName("registrarCredencial() no lanza excepción cuando el servidor responde 201")
        void registrarCredencial_201_noLanzaExcepcion() throws Exception {
            stubPost(201, "");

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> client.registrarCredencial(1L, "jperez", "Password1"));
        }

        @Test
        @DisplayName("registrarCredencial() lanza HttpException cuando el servidor responde error")
        void registrarCredencial_error_lanzaHttpException() throws Exception {
            stubPost(500, "");

            assertThatThrownBy(() -> client.registrarCredencial(1L, "jperez", "pass"))
                    .isInstanceOf(HttpException.class);
        }

        // ── Helpers ────────────────────────────────────────────────────────

        private void stubPost(int status, String body) throws Exception {
            @SuppressWarnings("unchecked")
            HttpResponse<String> resp = mock(HttpResponse.class);
            when(resp.statusCode()).thenReturn(status);
            when(resp.body()).thenReturn(body);
            when(api.isSuccess(resp)).thenReturn(status >= 200 && status < 300);
            when(api.post(anyString(), any())).thenReturn(resp);
        }

        private LoginResponseDTO loginResponse(String login, String rol) {
            LoginResponseDTO dto = new LoginResponseDTO();
            dto.setLogin(login);
            dto.setRol(rol);
            dto.setAccessToken("jwt");
            dto.setRefreshToken("refresh");
            dto.setUsuarioId(1L);
            dto.setNombreCompleto("Usuario Test");
            return dto;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CitaClient
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("CitaClient")
    class CitaClientTest {

        private CitaClient client;

        @BeforeEach
        void setUp() {
            client = new CitaClient(api);
        }

        // ── agendarCita() ──────────────────────────────────────────────────

        @Test
        @DisplayName("agendarCita() retorna CitaDTO cuando el servidor responde 201")
        void agendarCita_201_retornaDTO() throws Exception {
            CitaDTO respuesta = citaDTO(1L, EstadoCita.programada);
            stubPost(201, mapper.writeValueAsString(respuesta));

            CitaDTO resultado = client.agendarCita(citaDTO(null, null));

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getEstado()).isEqualTo(EstadoCita.programada);
        }

        @Test
        @DisplayName("agendarCita() lanza HttpException(409) cuando el horario está ocupado")
        void agendarCita_409_lanzaConflict() throws Exception {
            stubPost(409, "");

            assertThatThrownBy(() -> client.agendarCita(citaDTO(null, null)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).isConflict()).isTrue());
        }

        @Test
        @DisplayName("agendarCita() lanza HttpException(422) cuando el usuario no está sincronizado")
        void agendarCita_422_lanzaUnprocessable() throws Exception {
            stubPost(422, "");

            assertThatThrownBy(() -> client.agendarCita(citaDTO(null, null)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(422));
        }

        // ── cancelarCita() ─────────────────────────────────────────────────

        @Test
        @DisplayName("cancelarCita() retorna CitaDTO con estado cancelada cuando responde 200")
        void cancelarCita_200_retornaCancelada() throws Exception {
            CitaDTO respuesta = citaDTO(5L, EstadoCita.cancelada);
            stubPatch(200, mapper.writeValueAsString(respuesta));

            CitaDTO resultado = client.cancelarCita(5L);

            assertThat(resultado.getEstado()).isEqualTo(EstadoCita.cancelada);
        }

        @Test
        @DisplayName("cancelarCita() lanza HttpException(422) cuando la cita ya fue atendida o cancelada")
        void cancelarCita_422_lanzaExcepcion() throws Exception {
            stubPatch(422, "");

            assertThatThrownBy(() -> client.cancelarCita(5L))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(422));
        }

        // ── actualizarCita() ───────────────────────────────────────────────

        @Test
        @DisplayName("actualizarCita() retorna CitaDTO actualizado cuando responde 200")
        void actualizarCita_200_retornaDTO() throws Exception {
            CitaDTO respuesta = citaDTO(7L, EstadoCita.programada);
            stubPut(200, mapper.writeValueAsString(respuesta));

            CitaDTO resultado = client.actualizarCita(7L, citaDTO(7L, EstadoCita.programada));

            assertThat(resultado.getId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("actualizarCita() lanza HttpException(409) cuando el nuevo horario está ocupado")
        void actualizarCita_409_lanzaConflict() throws Exception {
            stubPut(409, "");

            assertThatThrownBy(() -> client.actualizarCita(1L, citaDTO(1L, EstadoCita.programada)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).isConflict()).isTrue());
        }

        @Test
        @DisplayName("actualizarCita() lanza HttpException(422) cuando la cita no está en estado programada")
        void actualizarCita_422_lanzaExcepcion() throws Exception {
            stubPut(422, "");

            assertThatThrownBy(() -> client.actualizarCita(1L, citaDTO(1L, EstadoCita.cancelada)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(422));
        }

        // ── listarPorPaciente() / listarPorProfesional() ───────────────────

        @Test
        @DisplayName("listarPorPaciente() retorna lista vacía cuando el servidor responde []")
        void listarPorPaciente_listaVacia() throws Exception {
            stubGet(200, "[]");

            assertThat(client.listarPorPaciente(1L)).isEmpty();
        }

        @Test
        @DisplayName("listarPorProfesional() retorna la lista correctamente")
        void listarPorProfesional_retornaLista() throws Exception {
            List<CitaDTO> lista = List.of(citaDTO(1L, EstadoCita.programada));
            stubGet(200, mapper.writeValueAsString(lista));

            assertThat(client.listarPorProfesional(2L)).hasSize(1);
        }

        @Test
        @DisplayName("listarPorProfesionalYFecha() llama a GET con ruta correcta")
        void listarPorProfesionalYFecha_llamaGetConRuta() throws Exception {
            stubGet(200, "[]");
            client.listarPorProfesionalYFecha(3L, LocalDate.of(2025, 7, 15));

            verify(api).get(contains("/profesional/3/fecha"));
        }

        // ── obtenerHorariosDisponibles() ───────────────────────────────────

        @Test
        @DisplayName("obtenerHorariosDisponibles() retorna lista de strings de horarios")
        void obtenerHorariosDisponibles_retornaLista() throws Exception {
            stubGet(200, "[\"09:00\",\"09:30\",\"10:00\"]");

            List<String> horarios = client.obtenerHorariosDisponibles(2L, LocalDate.now());

            assertThat(horarios).containsExactly("09:00", "09:30", "10:00");
        }

        // ── Helpers ────────────────────────────────────────────────────────

        private void stubPost(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.post(anyString(), any())).thenReturn(resp);
        }

        private void stubPatch(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.patch(anyString(), any())).thenReturn(resp);
        }

        private void stubPut(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.put(anyString(), any())).thenReturn(resp);
        }

        private void stubGet(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.get(anyString())).thenReturn(resp);
        }

        private CitaDTO citaDTO(Long id, EstadoCita estado) {
            return CitaDTO.builder()
                    .id(id).pacienteId(1L).profesionalId(2L)
                    .fechaHora(ZonedDateTime.now().plusDays(1))
                    .estado(estado).build();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DisponibilidadClient
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("DisponibilidadClient")
    class DisponibilidadClientTest {

        private DisponibilidadClient client;

        @BeforeEach
        void setUp() {
            client = new DisponibilidadClient(api);
        }

        // ── crearDisponibilidad() ──────────────────────────────────────────

        @Test
        @DisplayName("crearDisponibilidad() retorna DTO cuando el servidor responde 201")
        void crearDisponibilidad_201_retornaDTO() throws Exception {
            DisponibilidadSemanalDTO dto = dispDTO(1L, 1);
            stubPost(201, mapper.writeValueAsString(dto));

            DisponibilidadSemanalDTO resultado = client.crearDisponibilidad(dto);

            assertThat(resultado.getProfesionalId()).isEqualTo(1L);
        }

        @ParameterizedTest(name = "status={0}")
        @ValueSource(ints = {400, 422})
        @DisplayName("crearDisponibilidad() lanza HttpException para 400 y 422")
        void crearDisponibilidad_errorValidacion_lanzaExcepcion(int status) throws Exception {
            stubPost(status, "{\"detalle\":\"franja inválida\"}");

            assertThatThrownBy(() -> client.crearDisponibilidad(dispDTO(1L, 1)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(status));
        }

        // ── listarPorProfesional() ─────────────────────────────────────────

        @Test
        @DisplayName("listarPorProfesional() retorna lista vacía cuando el servidor responde []")
        void listarPorProfesional_listaVacia() throws Exception {
            stubGet(200, "[]");

            assertThat(client.listarPorProfesional(5L)).isEmpty();
        }

        @Test
        @DisplayName("listarPorProfesional() llama a GET con el ID correcto en la ruta")
        void listarPorProfesional_llamaGetConIdCorrecto() throws Exception {
            stubGet(200, "[]");
            client.listarPorProfesional(99L);

            verify(api).get(contains("/99"));
        }

        // ── eliminarDisponibilidad() ────────────────────────────────────────

        @Test
        @DisplayName("eliminarDisponibilidad() no lanza excepción cuando el servidor responde 204")
        void eliminarDisponibilidad_204_noLanzaExcepcion() throws Exception {
            stubDelete(204, "");

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> client.eliminarDisponibilidad(10L));
        }

        // ── obtenerConfiguracion() ─────────────────────────────────────────

        @Test
        @DisplayName("obtenerConfiguracion() retorna DTO con semanasHabilitadas correctas")
        void obtenerConfiguracion_retornaDTO() throws Exception {
            ConfiguracionAgendamientoDTO cfg = new ConfiguracionAgendamientoDTO(1L, 6);
            stubGet(200, mapper.writeValueAsString(cfg));

            ConfiguracionAgendamientoDTO resultado = client.obtenerConfiguracion();

            assertThat(resultado.getSemanasHabilitadas()).isEqualTo(6);
        }

        // ── actualizarConfiguracion() ──────────────────────────────────────

        @Test
        @DisplayName("actualizarConfiguracion() lanza HttpException(400) cuando el valor es inválido")
        void actualizarConfiguracion_400_lanzaExcepcion() throws Exception {
            stubPut(400, "{\"detalle\":\"semanas debe ser positivo\"}");

            assertThatThrownBy(() -> client.actualizarConfiguracion(new ConfiguracionAgendamientoDTO(1L, -1)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(400));
        }

        @Test
        @DisplayName("actualizarConfiguracion() retorna DTO actualizado cuando responde 200")
        void actualizarConfiguracion_200_retornaDTO() throws Exception {
            ConfiguracionAgendamientoDTO cfg = new ConfiguracionAgendamientoDTO(1L, 12);
            stubPut(200, mapper.writeValueAsString(cfg));

            ConfiguracionAgendamientoDTO resultado =
                    client.actualizarConfiguracion(new ConfiguracionAgendamientoDTO(1L, 12));

            assertThat(resultado.getSemanasHabilitadas()).isEqualTo(12);
        }

        // ── obtenerFechaMaximaAgendamiento() ───────────────────────────────

        @Test
        @DisplayName("obtenerFechaMaximaAgendamiento() parsea la fecha ISO devuelta por el backend")
        void obtenerFechaMaxima_parseoFechaISO() throws Exception {
            stubGet(200, "\"2025-08-30\"");

            LocalDate fecha = client.obtenerFechaMaximaAgendamiento();

            assertThat(fecha).isEqualTo(LocalDate.of(2025, 8, 30));
        }

        @Test
        @DisplayName("obtenerFechaMaximaAgendamiento() también parsea fecha sin comillas")
        void obtenerFechaMaxima_parseoFechaSinComillas() throws Exception {
            stubGet(200, "2025-09-15");

            LocalDate fecha = client.obtenerFechaMaximaAgendamiento();

            assertThat(fecha).isEqualTo(LocalDate.of(2025, 9, 15));
        }

        // ── registrarDiaNoDisponible() ─────────────────────────────────────

        @Test
        @DisplayName("registrarDiaNoDisponible() retorna DTO cuando el servidor responde 201")
        void registrarDiaNoDisponible_201_retornaDTO() throws Exception {
            DiaNoDisponibleDTO dto = diaDTO(LocalDate.of(2025, 12, 25), "Navidad", "FESTIVO");
            stubPost(201, mapper.writeValueAsString(dto));

            DiaNoDisponibleDTO resultado = client.registrarDiaNoDisponible(dto);

            assertThat(resultado.getMotivo()).isEqualTo("Navidad");
        }

        @Test
        @DisplayName("registrarDiaNoDisponible() lanza HttpException(400) cuando la fecha es inválida")
        void registrarDiaNoDisponible_400_lanzaExcepcion() throws Exception {
            stubPost(400, "{\"detalle\":\"fecha ya registrada\"}");

            assertThatThrownBy(() -> client.registrarDiaNoDisponible(
                    diaDTO(LocalDate.now(), "dup", "FESTIVO")))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).getStatusCode()).isEqualTo(400));
        }

        // ── listarDiasNoDisponibles() ──────────────────────────────────────

        @Test
        @DisplayName("listarDiasNoDisponibles() retorna lista de días correctamente")
        void listarDiasNoDisponibles_retornaLista() throws Exception {
            List<DiaNoDisponibleDTO> lista = List.of(
                    diaDTO(LocalDate.of(2025, 12, 25), "Navidad", "FESTIVO"),
                    diaDTO(LocalDate.of(2026, 1, 1), "Año Nuevo", "FESTIVO")
            );
            stubGet(200, mapper.writeValueAsString(lista));

            List<DiaNoDisponibleDTO> resultado = client.listarDiasNoDisponibles();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getMotivo()).isEqualTo("Navidad");
        }

        // ── eliminarDiaNoDisponible() ──────────────────────────────────────

        @Test
        @DisplayName("eliminarDiaNoDisponible() no lanza excepción cuando el servidor responde 204")
        void eliminarDiaNoDisponible_204_noLanzaExcepcion() throws Exception {
            stubDelete(204, "");

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> client.eliminarDiaNoDisponible(3L));
        }

        // ── Helpers ────────────────────────────────────────────────────────

        private void stubPost(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.post(anyString(), any())).thenReturn(resp);
        }

        private void stubGet(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.get(anyString())).thenReturn(resp);
        }

        private void stubPut(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.put(anyString(), any())).thenReturn(resp);
        }

        private void stubDelete(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.delete(anyString())).thenReturn(resp);
        }

        private DisponibilidadSemanalDTO dispDTO(Long profesionalId, int dia) {
            DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
            dto.setProfesionalId(profesionalId);
            dto.setDiaSemana(dia);
            dto.setHoraInicio(LocalTime.of(8, 0));
            dto.setHoraFin(LocalTime.of(17, 0));
            dto.setDuracionCitaMinutos(30);
            return dto;
        }

        private DiaNoDisponibleDTO diaDTO(LocalDate fecha, String motivo, String tipo) {
            DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
            dto.setFecha(fecha);
            dto.setMotivo(motivo);
            dto.setTipo(tipo);
            return dto;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UsuarioClient
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("UsuarioClient")
    class UsuarioClientTest {

        private UsuarioClient client;

        @BeforeEach
        void setUp() {
            client = new UsuarioClient(api);
        }

        // ── listarTodos() ─────────────────────────────────────────────────

        @Test
        @DisplayName("listarTodos() retorna lista de UsuarioDTO correctamente")
        void listarTodos_retornaLista() throws Exception {
            List<UsuarioDTO> lista = List.of(usuario(1L, "jperez", RolUsuario.paciente));
            stubGet(200, mapper.writeValueAsString(lista));

            assertThat(client.listarTodos()).hasSize(1);
        }

        @Test
        @DisplayName("listarTodos() retorna lista vacía cuando el servidor responde []")
        void listarTodos_listaVacia() throws Exception {
            stubGet(200, "[]");

            assertThat(client.listarTodos()).isEmpty();
        }

        // ── buscarPorId() ─────────────────────────────────────────────────

        @Test
        @DisplayName("buscarPorId() retorna el UsuarioDTO correcto")
        void buscarPorId_retornaDTO() throws Exception {
            UsuarioDTO u = usuario(42L, "agomez", RolUsuario.profesional);
            stubGet(200, mapper.writeValueAsString(u));

            UsuarioDTO resultado = client.buscarPorId(42L);

            assertThat(resultado.getId()).isEqualTo(42L);
            assertThat(resultado.getLogin()).isEqualTo("agomez");
        }

        // ── crearUsuario() ────────────────────────────────────────────────

        @Test
        @DisplayName("crearUsuario() retorna UsuarioDTO cuando el servidor responde 201")
        void crearUsuario_201_retornaDTO() throws Exception {
            UsuarioDTO u = usuario(1L, "nuevo", RolUsuario.paciente);
            stubPost(201, mapper.writeValueAsString(u));

            UsuarioDTO resultado = client.crearUsuario(u);

            assertThat(resultado.getLogin()).isEqualTo("nuevo");
        }

        @Test
        @DisplayName("crearUsuario() lanza HttpException(409) cuando el login ya está en uso")
        void crearUsuario_409_lanzaConflict() throws Exception {
            stubPost(409, "");

            assertThatThrownBy(() -> client.crearUsuario(usuario(null, "dup", RolUsuario.paciente)))
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).isConflict()).isTrue());
        }

        // ── activarUsuario() / desactivarUsuario() ─────────────────────────

        @Test
        @DisplayName("activarUsuario() llama a PATCH con la ruta /activar")
        void activarUsuario_llamaPatchConRutaCorrecta() throws Exception {
            stubPatch(200, "");

            client.activarUsuario(5L);

            verify(api).patch(contains("/5/activar"), any());
        }

        @Test
        @DisplayName("desactivarUsuario() llama a PATCH con la ruta /desactivar")
        void desactivarUsuario_llamaPatchConRutaCorrecta() throws Exception {
            stubPatch(200, "");

            client.desactivarUsuario(5L);

            verify(api).patch(contains("/5/desactivar"), any());
        }

        // ── contarActivos() ───────────────────────────────────────────────

        @Test
        @DisplayName("contarActivos() extrae el campo 'total' del JSON de respuesta")
        void contarActivos_extraeTotal() throws Exception {
            stubGet(200, "{\"total\":37}");

            assertThat(client.contarActivos()).isEqualTo(37L);
        }

        // ── buscarPacienteIdPorUsuarioId() ────────────────────────────────

        @Test
        @DisplayName("buscarPacienteIdPorUsuarioId() extrae el campo 'pacienteId' del JSON")
        void buscarPacienteId_extraePacienteId() throws Exception {
            stubGet(200, "{\"pacienteId\":99}");

            assertThat(client.buscarPacienteIdPorUsuarioId(1L)).isEqualTo(99L);
        }

        // ── listarProfesionales() — fallback 404 ──────────────────────────

        @Test
        @DisplayName("listarProfesionales() usa fallback cuando /profesionales responde 404")
        void listarProfesionales_404_usaFallback() throws Exception {
            // Primera llamada → 404 (endpoint dedicado no existe)
            HttpResponse<String> resp404 = respMock(404, "");
            // Segunda llamada (fallback /api/users/usuarios) → lista completa
            List<UsuarioDTO> todos = List.of(
                    usuario(1L, "prof1", RolUsuario.profesional),
                    usuario(2L, "pac1",  RolUsuario.paciente)
            );
            HttpResponse<String> resp200 = respMock(200, mapper.writeValueAsString(todos));

            when(api.get(anyString()))
                    .thenReturn(resp404)  // /api/users/profesionales → 404
                    .thenReturn(resp200); // /api/users/usuarios → 200

            List<ProfesionalDTO> resultado = client.listarProfesionales();

            // Solo debe retornar los que tienen rol profesional
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombreCompleto()).isEqualTo("prof1");
        }

        @Test
        @DisplayName("listarProfesionales() retorna lista directa cuando el endpoint dedicado responde 200")
        void listarProfesionales_200_retornaListaDirecta() throws Exception {
            ProfesionalDTO p = new ProfesionalDTO();
            p.setId(1L);
            p.setNombreCompleto("Dr. López");
            stubGet(200, mapper.writeValueAsString(List.of(p)));

            List<ProfesionalDTO> resultado = client.listarProfesionales();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombreCompleto()).isEqualTo("Dr. López");
        }

        // ── Helpers ────────────────────────────────────────────────────────

        private void stubGet(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.get(anyString())).thenReturn(resp);
        }

        private void stubPost(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.post(anyString(), any())).thenReturn(resp);
        }

        private void stubPatch(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.patch(anyString(), any())).thenReturn(resp);
        }

        private UsuarioDTO usuario(Long id, String login, RolUsuario rol) {
            return UsuarioDTO.builder()
                    .id(id).login(login).nombreCompleto(login)
                    .rol(rol).activo(true).build();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EspecialidadClient
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("EspecialidadClient")
    class EspecialidadClientTest {

        private EspecialidadClient client;

        @BeforeEach
        void setUp() {
            client = new EspecialidadClient(api);
        }

        @Test
        @DisplayName("listarNombres() retorna la lista de especialidades como strings")
        void listarNombres_retornaLista() throws Exception {
            stubGet(200, "[\"Medicina General\",\"Fisioterapia\",\"Odontología\"]");

            List<String> resultado = client.listarNombres();

            assertThat(resultado).containsExactly(
                    "Medicina General", "Fisioterapia", "Odontología");
        }

        @Test
        @DisplayName("listarNombres() lanza HttpException cuando el servidor responde error")
        void listarNombres_error_lanzaExcepcion() throws Exception {
            stubGet(500, "");

            assertThatThrownBy(() -> client.listarNombres())
                    .isInstanceOf(HttpException.class)
                    .satisfies(ex -> assertThat(((HttpException) ex).isServerError()).isTrue());
        }

        @Test
        @DisplayName("listarActivosPorEspecialidad() llama a GET con especialidad URL-encoded")
        void listarActivosPorEspecialidad_llamaGetConEspecialidadEncoded() throws Exception {
            stubGet(200, "[]");

            client.listarActivosPorEspecialidad("Medicina General");

            verify(api).get(contains("Medicina+General"));
        }

        @Test
        @DisplayName("listarActivosPorEspecialidad() retorna lista de ProfesionalDTO")
        void listarActivosPorEspecialidad_retornaLista() throws Exception {
            ProfesionalDTO p = new ProfesionalDTO();
            p.setId(1L);
            p.setNombreCompleto("Dr. Pérez");
            stubGet(200, mapper.writeValueAsString(List.of(p)));

            List<ProfesionalDTO> resultado =
                    client.listarActivosPorEspecialidad("Cardiología");

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombreCompleto()).isEqualTo("Dr. Pérez");
        }

        @Test
        @DisplayName("listarActivosPorEspecialidad() retorna lista vacía cuando no hay profesionales")
        void listarActivosPorEspecialidad_listaVacia() throws Exception {
            stubGet(200, "[]");

            assertThat(client.listarActivosPorEspecialidad("Especialidad Rara")).isEmpty();
        }

        // ── Helpers ────────────────────────────────────────────────────────

        private void stubGet(int status, String body) throws Exception {
            HttpResponse<String> resp = respMock(status, body);
            when(api.get(anyString())).thenReturn(resp);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper compartido para todos los clientes
    // ═══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private HttpResponse<String> respMock(int status, String body) {
        HttpResponse<String> resp = mock(HttpResponse.class);
        lenient().when(resp.statusCode()).thenReturn(status);
        lenient().when(resp.body()).thenReturn(body);
        lenient().when(api.isSuccess(resp)).thenReturn(status >= 200 && status < 300);
        return resp;
    }
}
