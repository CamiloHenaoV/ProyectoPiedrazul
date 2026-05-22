package com.piedrazul.frontend.app;

import com.piedrazul.frontend.client.AuthClient;
import com.piedrazul.frontend.client.CitaClient;
import com.piedrazul.frontend.client.DisponibilidadClient;
import com.piedrazul.frontend.client.EspecialidadClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.config.AppConfig;
import com.piedrazul.frontend.http.ApiClient;
import com.piedrazul.frontend.http.SessionManager;
import com.piedrazul.frontend.observer.EventBus;

/**
 * Contenedor manual de dependencias.
 *
 * Reemplaza el ApplicationContext de Spring. Instancia y cablea
 * todos los componentes una sola vez (singleton por convención).
 *
 * Orden de inicialización:
 *   1. AppConfig (lee application.properties)
 *   2. SessionManager (estado de sesión JWT)
 *   3. ApiClient (HttpClient con el gateway)
 *   4. Clients de cada microservicio
 *   5. EventBus (observer)
 *   6. StageInitializer (navegación JavaFX)
 *
 * Clientes añadidos:
 *   - DisponibilidadClient (HU-1.5, HU-1.6, HU-1.7, HU-1.8)
 */
public class AppContext {

    private static AppContext instance;

    // ── Infraestructura ──────────────────────────────────────────
    private AppConfig        appConfig;
    private SessionManager   sessionManager;
    private ApiClient        apiClient;
    private EventBus         eventBus;
    private StageInitializer stageInitializer;

    // ── Clients HTTP ─────────────────────────────────────────────
    private AuthClient          authClient;
    private UsuarioClient       usuarioClient;
    private CitaClient          citaClient;
    private EspecialidadClient  especialidadClient;
    private DisponibilidadClient disponibilidadClient;   // ← NUEVO (HU-1.5–1.8)

    private AppContext() {}

    public static AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    /** Inicializa todas las dependencias. Llamar solo desde MainApp. */
    public void initialize() {
        appConfig          = new AppConfig();
        sessionManager     = SessionManager.getInstance();
        apiClient          = new ApiClient(appConfig, sessionManager);
        eventBus           = new EventBus();
        stageInitializer   = new StageInitializer(this);

        authClient           = new AuthClient(apiClient);
        usuarioClient        = new UsuarioClient(apiClient);
        citaClient           = new CitaClient(apiClient);
        especialidadClient   = new EspecialidadClient(apiClient);
        disponibilidadClient = new DisponibilidadClient(apiClient); // ← NUEVO
    }

    // ── Getters ──────────────────────────────────────────────────
    public AppConfig           getAppConfig()            { return appConfig; }
    public SessionManager      getSessionManager()       { return sessionManager; }
    public ApiClient           getApiClient()            { return apiClient; }
    public EventBus            getEventBus()             { return eventBus; }
    public StageInitializer    getStageInitializer()     { return stageInitializer; }
    public AuthClient          getAuthClient()           { return authClient; }
    public UsuarioClient       getUsuarioClient()        { return usuarioClient; }
    public CitaClient          getCitaClient()           { return citaClient; }
    public EspecialidadClient  getEspecialidadClient()   { return especialidadClient; }
    /** Usado por los controladores de configuración de disponibilidad. */
    public DisponibilidadClient getDisponibilidadClient() { return disponibilidadClient; }
}
