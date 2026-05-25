package com.piedrazul.msauthservice.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil")
class JwtUtilTest {

    // Secret de al menos 32 chars para HMAC-SHA256
    private static final String SECRET =
            "clave-super-secreta-para-tests-unitarios-1234";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hora

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);
    }

    // -----------------------------------------------------------------------
    // generarAccessToken
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("generarAccessToken() retorna un token no vacío")
    void generarAccessToken_retornaTokenNoVacio() {
        String token = jwtUtil.generarAccessToken(1L, "jperez", "paciente");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generarAccessToken() produce tokens distintos para usuarios diferentes")
    void generarAccessToken_usuariosDistintos_tokensDistintos() {
        String t1 = jwtUtil.generarAccessToken(1L, "jperez", "paciente");
        String t2 = jwtUtil.generarAccessToken(2L, "agomez", "profesional");
        assertThat(t1).isNotEqualTo(t2);
    }

    // -----------------------------------------------------------------------
    // extraerUsuarioId
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("extraerUsuarioId() recupera el ID correcto del token")
    void extraerUsuarioId_retornaIdCorrecto() {
        String token = jwtUtil.generarAccessToken(42L, "user42", "paciente");
        assertThat(jwtUtil.extraerUsuarioId(token)).isEqualTo(42L);
    }

    // -----------------------------------------------------------------------
    // extraerLogin
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("extraerLogin() recupera el login del token")
    void extraerLogin_retornaLoginCorrecto() {
        String token = jwtUtil.generarAccessToken(1L, "maria.garcia", "agendador");
        assertThat(jwtUtil.extraerLogin(token)).isEqualTo("maria.garcia");
    }

    // -----------------------------------------------------------------------
    // extraerRol
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("extraerRol() recupera el rol del token")
    void extraerRol_retornaRolCorrecto() {
        String token = jwtUtil.generarAccessToken(1L, "admin", "administrador");
        assertThat(jwtUtil.extraerRol(token)).isEqualTo("administrador");
    }

    // -----------------------------------------------------------------------
    // esValido
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("esValido() retorna true para un token recién generado")
    void esValido_tokenReciente_retornaTrue() {
        String token = jwtUtil.generarAccessToken(1L, "user", "paciente");
        assertThat(jwtUtil.esValido(token)).isTrue();
    }

    @Test
    @DisplayName("esValido() retorna false para un token con firma alterada")
    void esValido_tokenManipulado_retornaFalse() {
        String token = jwtUtil.generarAccessToken(1L, "user", "paciente");
        // Alteramos el último carácter de la firma
        String corrupto = token.substring(0, token.length() - 1) + "X";
        assertThat(jwtUtil.esValido(corrupto)).isFalse();
    }

    @Test
    @DisplayName("esValido() retorna false para un token expirado")
    void esValido_tokenExpirado_retornaFalse() {
        // Creamos un JwtUtil con expiración de -1 ms (ya expiró)
        JwtUtil utilExpirado = new JwtUtil(SECRET, -1L);
        String token = utilExpirado.generarAccessToken(1L, "user", "paciente");
        assertThat(utilExpirado.esValido(token)).isFalse();
    }

    @Test
    @DisplayName("esValido() retorna false para una cadena aleatoria sin formato JWT")
    void esValido_cadenaAleatoria_retornaFalse() {
        assertThat(jwtUtil.esValido("esto.no.es.un.jwt")).isFalse();
    }

    @Test
    @DisplayName("esValido() retorna false para cadena vacía")
    void esValido_cadenaVacia_retornaFalse() {
        assertThat(jwtUtil.esValido("")).isFalse();
    }

    // -----------------------------------------------------------------------
    // extraerClaims — token inválido lanza excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("extraerClaims() lanza excepción para token con firma incorrecta")
    void extraerClaims_firmaIncorrecta_lanzaExcepcion() {
        String token = jwtUtil.generarAccessToken(1L, "user", "rol");
        String corrupto = token.substring(0, token.length() - 1) + "Z";

        assertThatThrownBy(() -> jwtUtil.extraerClaims(corrupto))
                .isInstanceOf(Exception.class);
    }

    // -----------------------------------------------------------------------
    // Consistencia de claims en un mismo token
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Los tres claims (id, login, rol) son consistentes en el mismo token")
    void claims_consistentes_enMismoToken() {
        String token = jwtUtil.generarAccessToken(99L, "profesional.lopez", "profesional");

        assertThat(jwtUtil.extraerUsuarioId(token)).isEqualTo(99L);
        assertThat(jwtUtil.extraerLogin(token)).isEqualTo("profesional.lopez");
        assertThat(jwtUtil.extraerRol(token)).isEqualTo("profesional");
    }
}
