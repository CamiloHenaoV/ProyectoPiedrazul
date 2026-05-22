package com.piedrazul.msusermanagement.application.service.template;

import com.piedrazul.msusermanagement.application.service.interfaces.IUsuarioService;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;

/**
 * Clase abstracta que implementa el patrón <b>Template Method</b> para
 * el proceso de registro de usuarios.
 *
 * <p>Define el algoritmo fijo de registro en tres pasos:
 * <ol>
 *   <li>Crear el usuario base (invariante — siempre igual).</li>
 *   <li>Vincular el perfil específico (variante — implementada por cada subclase).</li>
 *   <li>Convertir la entidad a DTO y retornar (invariante — siempre igual).</li>
 * </ol>
 *
 * <p>Las subclases concretas solo necesitan sobrescribir {@link #vincularPerfil}
 * con la lógica particular de su tipo de registro. El orden de los pasos y la
 * gestión de la transacción son responsabilidad exclusiva de esta clase.
 *
 * <pre>
 * RegistroTemplate  (abstract)
 *  ├── RegistroAdminService       → hook: no-op
 *  ├── RegistroPacienteService    → hook: crea perfil Paciente
 *  └── RegistroProfesionalService → hook: crea perfil Profesional
 * </pre>
 */
public abstract class RegistroTemplate {

    protected final IUsuarioService usuarioService;

    protected RegistroTemplate(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Template method — final para que ninguna subclase altere el esqueleto
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ejecuta el proceso completo de registro.
     *
     * <p>Este método es {@code final}: define el esqueleto del algoritmo y
     * no puede ser redefinido por las subclases. La transacción cubre los tres
     * pasos como una unidad atómica.
     *
     * @param contexto datos necesarios para el registro (usuario + perfil opcional).
     * @return DTO del usuario recién creado.
     */
    public final UsuarioDTO registrar(RegistroContexto contexto) {

        // Paso 1 — invariante: crear el registro de autenticación/identificación
        Usuario usuario = usuarioService.crearUsuarioBase(contexto.usuarioDTO());

        // Paso 2 — variante: vincular el perfil clínico o administrativo
        vincularPerfil(usuario, contexto);

        // Paso 3 — invariante: proyectar la entidad a su representación externa
        return toDTO(usuario);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Hook — las subclases sobrescriben solo lo que cambia
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Hook del template method.
     *
     * <p>Vincula el perfil específico (Paciente, Profesional, etc.) al usuario
     * que acaba de ser creado. La implementación por defecto es un no-op, lo
     * que permite registrar usuarios sin perfil adicional (administradores)
     * sin necesidad de sobrescribir el método.
     *
     * @param usuario  entidad persistida en el paso 1, lista para ser referenciada.
     * @param contexto datos del perfil a vincular (PacienteDTO o ProfesionalDTO,
     *                 según la subclase).
     */
    protected void vincularPerfil(Usuario usuario, RegistroContexto contexto) {
        // no-op por defecto: registro de administrador (sin perfil clínico)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Paso compartido — privado para que las subclases no lo dupliquen
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convierte la entidad {@link Usuario} al DTO de salida.
     * Centralizado aquí para evitar duplicación en las subclases.
     */
    private UsuarioDTO toDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .login(usuario.getLogin())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .build();
    }
}
