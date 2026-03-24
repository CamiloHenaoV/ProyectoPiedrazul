package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.DisponibilidadSemanal;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.exceptions.*;
import com.piedrazul.gestioncitasmedicas.model.exceptions.PasswordInvalidaException;
import com.piedrazul.gestioncitasmedicas.model.repositories.*;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Implementación del servicio de gestión de usuarios.
 * <p>
 * Centraliza toda la lógica de negocio relacionada con usuarios,
 * delegando la persistencia al repositorio y la encriptación
 * al servicio de passwords.
 */
@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IPasswordService  passwordService;
    private final EventBus          eventBus;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadRepository especialidadRepository;
    private final DisponibilidadSemanalRepository disponibilidadRepository;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            IPasswordService  passwordService,
            EventBus          eventBus,
            PacienteRepository pacienteRepository,
            ProfesionalRepository  profesionalRepository,
            EspecialidadRepository especialidadRepository,
            DisponibilidadSemanalRepository disponibilidadRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService   = passwordService;
        this.eventBus          = eventBus;
        this.pacienteRepository = pacienteRepository;
        this.profesionalRepository  = profesionalRepository;
        this.especialidadRepository = especialidadRepository;
        this.disponibilidadRepository = disponibilidadRepository;
    }
    /**
     * Autentica un usuario verificando sus credenciales.
     * <p>
     * El orden de validación es:
     * <ol>
     *     <li>Verifica que el login exista</li>
     *     <li>Verifica que el usuario esté activo</li>
     *     <li>Verifica que la contraseña coincida con el hash</li>
     * </ol>
     * Si cualquiera falla lanza la misma excepción para no revelar
     * cuál validación específica falló.
     *
     * @param login    identificador único del usuario
     * @param password contraseña en texto plano
     * @return {@link UsuarioDTO} con los datos del usuario autenticado
     * @throws CredencialesInvalidasException si alguna validación falla
     */
    @Override
    public UsuarioDTO autenticar(String login, String password) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordService.verificar(password, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return toDTO(usuario);
    }
    /**
     * Crea un nuevo usuario en el sistema.
     * <p>
     * Verifica duplicidad del login antes de persistir.
     * La contraseña se encripta antes de almacenarse —
     * nunca se guarda en texto plano.
     *
     * @param dto datos del usuario a crear
     * @return {@link UsuarioDTO} con los datos del usuario creado
     * @throws LoginDuplicadoException si el login ya está registrado
     */
    private static final int PASSWORD_MIN_LENGTH = 8;

    private void validarPasswordSinFormato(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new PasswordInvalidaException("La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
        }
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioDTO dto) {
        if (usuarioRepository.existsByLogin(dto.getLogin())) {
            throw new LoginDuplicadoException(dto.getLogin());
        }

        validarPasswordSinFormato(dto.getPassword());

        Usuario usuario = Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .login(dto.getLogin())
                .passwordHash(passwordService.encriptar(dto.getPassword()))
                .rol(dto.getRol())
                .activo(true)
                .build();

        UsuarioDTO guardado = toDTO(usuarioRepository.save(usuario));
        eventBus.publish(AppEvent.USUARIO_CREADO, guardado);
        return guardado;
    }

    @Override
    @Transactional
    public UsuarioDTO crearUsuarioConPaciente(UsuarioDTO usuarioDTO, PacienteDTO pacienteDTO) {
        if (usuarioRepository.existsByLogin(usuarioDTO.getLogin())) {
            throw new LoginDuplicadoException(usuarioDTO.getLogin());
        }

        validarPasswordSinFormato(usuarioDTO.getPassword());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreCompleto(usuarioDTO.getNombreCompleto())
                .login(usuarioDTO.getLogin())
                .passwordHash(passwordService.encriptar(usuarioDTO.getPassword()))
                .rol(usuarioDTO.getRol())
                .activo(true)
                .build());

        pacienteRepository.save(Paciente.builder()
                .usuario(usuario)
                .nombreCompleto(pacienteDTO.getNombreCompleto())
                .cedulaIdentidad(pacienteDTO.getCedulaIdentidad())
                .fechaNacimiento(pacienteDTO.getFechaNacimiento())
                .telefono(pacienteDTO.getTelefono())
                .email(pacienteDTO.getEmail())
                .direccion(pacienteDTO.getDireccion())
                .creadoEn(ZonedDateTime.now())
                .build());

        UsuarioDTO creado = toDTO(usuario);
        eventBus.publish(AppEvent.USUARIO_CREADO, creado);
        return creado;
    }

    @Override
    @Transactional
    public UsuarioDTO crearUsuarioConProfesional(UsuarioDTO usuarioDTO, ProfesionalDTO profesionalDTO) {
        if (usuarioRepository.existsByLogin(usuarioDTO.getLogin())) {
            throw new LoginDuplicadoException(usuarioDTO.getLogin());
        }

        validarPasswordSinFormato(usuarioDTO.getPassword());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreCompleto(usuarioDTO.getNombreCompleto())
                .login(usuarioDTO.getLogin())
                .passwordHash(passwordService.encriptar(usuarioDTO.getPassword()))
                .rol(usuarioDTO.getRol())
                .activo(true)
                .build());

        var especialidad = especialidadRepository
                .findByNombre(profesionalDTO.getEspecialidadNombre())
                .orElseThrow();

        Profesional profesional = profesionalRepository.save(Profesional.builder()
                .usuario(usuario)
                .tipo(profesionalDTO.getTipo())
                .especialidad(especialidad)
                .licenciaProfesional(profesionalDTO.getLicenciaProfesional())
                .activo(true)
                .build());

        crearDisponibilidadPorDefecto(profesional);

        UsuarioDTO creado = toDTO(usuario);
        eventBus.publish(AppEvent.USUARIO_CREADO, creado);
        return creado;
    }
    /**
     * Busca un usuario por su identificador único.
     *
     * @param id identificador UUID del usuario
     * @return {@link UsuarioDTO} con los datos del usuario
     * @throws UsuarioNoEncontradoException si no existe un usuario con ese ID
     */
    @Override
    public UsuarioDTO buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
    }

    /**
     * Retorna todos los usuarios registrados en el sistema.
     *
     * @return lista de {@link UsuarioDTO}, vacía si no hay usuarios
     */
    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todos los usuarios que tienen un rol específico.
     *
     * @param rol rol por el que filtrar
     * @return lista de {@link UsuarioDTO} con ese rol
     */
    @Override
    public List<UsuarioDTO> listarPorRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un usuario existente.
     * <p>
     * Solo permite modificar {@code nombreCompleto} y {@code rol}.
     * El login y la contraseña no se modifican en esta operación.
     *
     * @param id  identificador del usuario a actualizar
     * @param dto datos nuevos del usuario
     * @return {@link UsuarioDTO} con los datos actualizados
     * @throws UsuarioNoEncontradoException si no existe un usuario con ese ID
     */
    // HU 1.3 - implementacion edicion de usuario por admin
    @Override
    public UsuarioDTO actualizarUsuario(UUID id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));

        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setRol(dto.getRol());

        UsuarioDTO actualizado = toDTO(usuarioRepository.save(usuario));
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, actualizado);
        return actualizado;
    }

    /**
     * Desactiva un usuario impidiendo que pueda autenticarse.
     * <p>
     * La desactivación es lógica — el registro permanece en la BD
     * pero el usuario no puede iniciar sesión.
     *
     * @param id identificador del usuario a desactivar
     * @throws UsuarioNoEncontradoException si no existe un usuario con ese ID
     */
    @Override
    public void desactivarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));

        usuario.setActivo(false);
        UsuarioDTO desactivado = toDTO(usuarioRepository.save(usuario));
        eventBus.publish(AppEvent.USUARIO_DESACTIVADO, desactivado);
    }

    /**
     * Reactiva un usuario previamente desactivado.
     *
     * @param id identificador del usuario a activar
     * @throws UsuarioNoEncontradoException si no existe un usuario con ese ID
     */
    @Override
    public void activarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, toDTO(usuario));
    }

    /**
     * Verifica si un login ya está registrado en el sistema.
     * <p>
     * Útil para validar en la UI antes de intentar crear un usuario
     * y mostrar un error preventivo al usuario.
     *
     * @param login login a verificar
     * @return {@code true} si el login ya existe, {@code false} si está disponible
     */
    @Override
    public boolean existeLogin(String login) {
        return usuarioRepository.existsByLogin(login);
    }

    @Override
    public boolean cambiarContrasena(String login, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsuarioNoEncontradoException(login));

        if (!usuario.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordService.verificar(passwordActual, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        validarPasswordSinFormato(passwordNueva);

        usuario.setPasswordHash(passwordService.encriptar(passwordNueva));
        usuarioRepository.save(usuario);
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, toDTO(usuario));
        return true;
    }

    @Override
    public boolean recuperarContrasena(String login, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsuarioNoEncontradoException(login));

        validarPasswordSinFormato(passwordNueva);

        usuario.setPasswordHash(passwordService.encriptar(passwordNueva));
        usuarioRepository.save(usuario);
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, toDTO(usuario));
        return true;
    }

    private static final int[] DIAS_HABILES = {1, 2, 3, 4, 5};

    private void crearDisponibilidadPorDefecto(Profesional profesional) {
        int duracion = profesional.getTipo() == TipoProfesional.medico ? 5 : 20;

        for (int dia : DIAS_HABILES) {
            disponibilidadRepository.save(DisponibilidadSemanal.builder()
                    .profesional(profesional)
                    .diaSemana(dia)
                    .horaInicio(LocalTime.of(7, 0))
                    .horaFin(LocalTime.of(14, 0))
                    .duracionCitaMinutos(duracion)
                    .build());
        }
    }
    /**
     * Convierte una entidad {@link Usuario} a su representación {@link UsuarioDTO}.
     * <p>
     * El {@code passwordHash} nunca se incluye en el DTO para evitar
     * que llegue a la capa de presentación.
     *
     * @param u entidad a convertir
     * @return DTO con los datos del usuario sin información sensible
     */
    private UsuarioDTO toDTO(Usuario u) {
        return UsuarioDTO.builder()
                .id(u.getId())
                .nombreCompleto(u.getNombreCompleto())
                .login(u.getLogin())
                .rol(u.getRol())
                .activo(u.getActivo())
                .build();
    }

    @Override
    public UUID buscarPacienteIdPorUsuarioId(UUID usuarioId) {
        return pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId.toString()))
                .getId();
    }
    @Override
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }
}
