#  Piedrazul — Sistema de Gestión de Citas Médicas

Sistema completo para la gestión de citas médicas, construido con arquitectura de microservicios en Java. Incluye backend con Spring Boot, cliente de escritorio JavaFX, autenticación JWT, mensajería asíncrona con RabbitMQ y base de datos PostgreSQL.

---

##  Arquitectura

El proyecto sigue una arquitectura de microservicios orquestada por un API Gateway central:

```
FxClient (JavaFX Desktop)
        │
        ▼
  api-gateway  ◄── Filtros JWT + Logging
   /    |    \
MSAuth  MSUserManagement  MSScheduling
        │
     RabbitMQ (mensajería asíncrona)
        │
    PostgreSQL
```

### Módulos

| Módulo | Descripción |
|---|---|
| `api-gateway` | Enrutamiento, autenticación JWT y logging |
| `MSAuth` | Registro, login y validación de tokens |
| `MSUserManagement` | Gestión de usuarios, pacientes y profesionales |
| `MSScheduling` | Agendamiento, disponibilidad y gestión de citas |
| `FxClient` | Aplicación de escritorio JavaFX (cliente) |
| `Monolito` | Versión monolítica de referencia |

---

##  Tecnologías

- **Java 21**
- **Spring Boot 4** / **Spring Cloud 2025**
- **JavaFX** (cliente de escritorio)
- **PostgreSQL** (base de datos)
- **RabbitMQ** (mensajería)
- **JWT (JJWT 0.12.6)** (autenticación)
- **Maven** (gestión de dependencias, multi-módulo)
- **Docker Compose** (infraestructura)

---

##  Requisitos previos

- Java 21+
- Maven 3.9+
- Docker y Docker Compose
- PostgreSQL (o usar el script SQL incluido)

---

##  Configuración e instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/ProyectoPiedrazul.git
cd ProyectoPiedrazul
```

### 2. Levantar la infraestructura con Docker

```bash
docker-compose up -d
```

Esto levanta RabbitMQ con su panel de administración en `http://localhost:15672` (usuario: `admin`, contraseña: `admin`).

### 3. Crear la base de datos

Ejecutar el script SQL incluido en tu instancia de PostgreSQL:

```bash
psql -U postgres -d tu_base_de_datos -f SCRIPT_SQL/SCRIPT\ SQL.txt
```

### 4. Compilar el proyecto

```bash
mvn clean install
```

### 5. Ejecutar cada microservicio

Desde la raíz de cada módulo:

```bash
cd MSAuth && mvn spring-boot:run
cd MSUserManagement && mvn spring-boot:run
cd MSScheduling && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### 6. Ejecutar el cliente de escritorio

```bash
cd FxClient && mvn javafx:run
```

---

## 👤 Roles de usuario

El sistema maneja tres roles con paneles independientes:

| Rol | Permisos principales |
|---|---|
| **Admin** | Gestión de usuarios, profesionales, configuración general |
| **Agendador** | Crear, reprogramar y cancelar citas; gestionar disponibilidad |
| **Paciente** | Ver y solicitar citas propias |

---

##  Estructura del proyecto

```
ProyectoPiedrazul/
├── api-gateway/          # Filtros JWT y enrutamiento
├── MSAuth/               # Servicio de autenticación
├── MSUserManagement/     # Gestión de usuarios y profesionales
├── MSScheduling/         # Agendamiento y disponibilidad
├── FxClient/             # Cliente JavaFX de escritorio
│   └── src/main/
│       ├── java/.../controller/   # Controladores de cada vista
│       ├── java/.../client/       # Clientes HTTP hacia la API
│       ├── java/.../model/dto/    # DTOs de transferencia
│       └── resources/view/fxml/   # Vistas FXML
├── Monolito/             # Versión monolítica de referencia
├── SCRIPT_SQL/           # Script de creación de base de datos
├── artefactos/           # Diagramas C4, documentos de arquitectura
├── docker-compose.yml
└── pom.xml               # POM padre multi-módulo
```

---

##  Pruebas

El proyecto incluye pruebas unitarias en todos los módulos principales. Para ejecutarlas:

```bash
mvn test
```

Las pruebas cubren entidades del dominio, servicios de negocio y los filtros del API Gateway.

---

##  Documentación

En la carpeta `artefactos/` se encuentran:

- Documentos de arquitectura (PDF y DOCX)
- Diagramas C4 v1 y v2 (contexto, contenedores, componentes, clases)
- Historias de usuario en formato Excel

---

##  Personalización

El proyecto está diseñado para ser extendido:

- **Nuevas especialidades médicas**: agregar registros en la tabla `especialidades`.
- **Nuevos tipos de profesional**: extender el enum `TipoProfesional`.
- **Nuevas vistas en el cliente**: crear archivo FXML + controlador en `FxClient/controller/`.
- **Nuevos eventos asíncronos**: publicar en RabbitMQ desde cualquier microservicio usando el `EventBus`.
- **Nuevos microservicios**: agregarlos como módulos Maven al `pom.xml` padre y registrar rutas en el API Gateway.

---

##  Contribuciones

Las contribuciones son bienvenidas. Por favor abre un *issue* primero para discutir los cambios que deseas realizar.

---

##  Licencia

Este proyecto se distribuye bajo los términos acordados por el equipo de desarrollo. Consulta el archivo `LICENSE` para más detalles.
