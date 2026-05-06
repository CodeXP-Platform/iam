# IAM Microservice Architecture (CodeXP Standard)

## 1. Objetivo

Este documento define la implementación y las decisiones arquitectónicas del microservicio **IAM (Identity and Access Management)**.

Este microservicio es el pilar central de la seguridad de la plataforma. Es el **único responsable de emitir tokens JWT** (el API Gateway los valida) y de gestionar la identidad de los usuarios, siguiendo estrictamente el estándar del equipo:
- Arquitectura por capas (Hexagonal)
- DDD Táctico (Entity + Value Objects)
- CQRS liviano (Commands y Queries separados)
- Patrón Assembler
- Publicación de Eventos de Integración (RabbitMQ)

## 2. Principios de arquitectura y Separación por capas

El proyecto respeta los límites de contexto (`bounded-context`) estructurados en:
- `domain`: Modelo central (User), VOs, contratos de servicio (Command/Query).
- `application`: Casos de uso (`UserCommandServiceImpl`, `UserQueryServiceImpl`).
- `infrastructure`: Configuración de base de datos (PostgreSQL), RabbitMQ, configuración inicial de Spring Security.
- `interfaces/rest`: Endpoints públicos y protegidos, Assemblers, orquestación HTTP.
- `shared`: Utilidades JWT compartidas y manejo de excepciones.

## 3. Estándares de modelado de dominio

### 3.1 Entidades y Aggregate Root
- **`User` (Aggregate Root)**: Controla el ciclo de vida del usuario. Hereda de `AbstractEntity` para la auditoría técnica (`createdAt`, `updatedAt`).

### 3.2 Value Objects (VO)
Se aplican VOs para encapsular invariantes de negocio y validaciones de formato:
- `UserId`: UUID encapsulado, base de la identidad en toda la plataforma.
- `UserEmail`: Validación de formato de correo.
- `UserNickname`: Restricción de longitud (máx 20 caracteres alineado a BD).
- `UserPassword`: Cifrado y validación.

## 4. CQRS Liviano y Casos de Uso

La lectura y escritura están estrictamente separadas en la capa de aplicación:

**Command Services (Cambios de estado):**
- `SignUpCommand`: Registra un nuevo usuario en el sistema.
- `UpdateProfileCommand`: Actualiza datos permitidos del perfil.
- `DeleteUserCommand`: Elimina la cuenta y dispara eventos de dominio.

**Query Services (Consultas y Autenticación):**
- `SignInQuery`: Valida credenciales y retorna el token JWT.
- `GetUserByIdQuery`: Retorna la información pública/privada del perfil.

## 5. Patrón Assembler

Para aislar el dominio de la capa REST, los Controladores utilizan clases estáticas de transformación:
- `UserCommandAssembler`: Transforma `SignUpRequest` / `UpdateProfileRequest` a sus respectivos Commands utilizando Value Objects.
- `UserAssembler`: Mapea la entidad `User` a un `UserResponse` o `AuthResponse` de forma segura, omitiendo datos sensibles (como contraseñas).

*Regla estricta aplicada*: Ningún controlador mapea strings o primitivos directamente a la entidad. Todo pasa por el Assembler.

## 6. Reglas de negocio en Application Services

El `UserCommandServiceImpl` y `UserQueryServiceImpl` centralizan reglas críticas:
- **Colisión de Proveedores**: Si un correo ya existe vía OAuth y se intenta registro tradicional, se lanza `AuthProviderConflictException` (401 Unauthorized / 409 Conflict).
- **Anti-IDOR**: Los endpoints protegidos (como `/api/v1/iam/me`) utilizan el `UserContext` / `@AuthenticationPrincipal` para garantizar que el usuario solo modifique su propia data.
- **Generación JWT**: Inyección de los claims exactos requeridos por el contrato del equipo (`sub`, `nickname`, `email`, `role`).

## 7. Eventos y Mensajería (RabbitMQ)

El IAM actúa como un *Publisher* de eventos críticos para la consistencia eventual del sistema.
Operaciones transaccionales en la base de datos están ligadas a la emisión de eventos:
- **`UserDeletedEvent`**: Publicado antes del borrado físico/lógico en BD, utilizando las routing keys correctas para que microservicios como `challenges` o `gamification` puedan limpiar la data asociada al usuario.

## 8. Manejo de Errores

Centralizado en el `GlobalExceptionHandler`:
- `400 Bad Request`: Errores de validación de Value Objects (ej. Nickname > 20 chars).
- `401 Unauthorized`: Credenciales inválidas o conflicto de AuthProvider.
- `404 Not Found`: Usuario no encontrado.
- `422 Unprocessable Entity`: Fallos de precondiciones de negocio.