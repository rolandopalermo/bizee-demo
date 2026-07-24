# AGENTS.md

Guía principal para agentes de IA (Cursor, Claude Code, Codex, ChatGPT, Gemini, etc.) que trabajen en este repositorio.

**Fuentes de verdad adicionales (Cursor rules):**

| Archivo | Alcance |
|---------|---------|
| `.cursor/rules/spring-boot-architecture.mdc` | Capas Java, DTOs, CRUD, REST vs Thymeleaf |
| `.cursor/rules/event-driven-messaging.mdc` | RabbitMQ, eventos, publish/consume |
| `.cursor/rules/controller-integration-tests.mdc` | ITs de controllers con Testcontainers |
| `.cursor/rules/frontend-styles.mdc` | UI Bootstrap 5, HTML/CSS/JS |

Si este documento y una rule de Cursor discrepan en detalle de implementación, **priorizar la rule específica del área** y actualizar `AGENTS.md`.

---

## 1. Descripción del proyecto

### Objetivo

Demo de gestión de **companies**, **users** y **registered agents** (servicio de agente registrado por estado en EE. UU.), con:

- Asignación de registered agent propio del usuario **o** del servicio Bizee (según capacidad y balanceo de carga).
- Notificaciones por eventos (simuladas en consola) al asignar un RA de servicio y cuando un estado alcanza **≥ 90%** de capacidad total del servicio.
- Panel administrativo web + API REST.

### Tipo de aplicación

Aplicación **backend + panel admin server-rendered**:

- **API REST** JSON bajo `/api/**`.
- **UI Thymeleaf** (HTML) con sesión HTTP (login contra tabla `users`).
- **No** es un SPA ni usa Spring Security completo.

### Tecnologías utilizadas

- Java **21**
- Spring Boot **4.1.0** (Web MVC, Data JPA, Validation, Flyway, AMQP, Thymeleaf)
- PostgreSQL **16** (Docker Compose)
- RabbitMQ **3.13** (management en Compose)
- Bootstrap **5.3.3** + Bootstrap Icons **1.11.3** (CDN)
- Maven Wrapper (`./mvnw`)
- Testcontainers (PostgreSQL + RabbitMQ) + JUnit + MockMvc + Awaitility
- `spring-security-crypto` **solo** para BCrypt (no starter de Spring Security)

### Arquitectura general

- Capas: **Controller / RestController → Service → Repository**.
- DTOs en el borde HTTP; entidades JPA no se exponen en controllers.
- Errores API centralizados en `GlobalExceptionHandler` (`@RestControllerAdvice`).
- Mensajería **paralela**: Service → EventPublisher → RabbitMQ → EventConsumer (side effects).
- Auth web: sesión (`SessionAuth` + `SessionAuthInterceptor`); API: header `X-User-Id`.

### Organización del código

Paquete base: `com.bizee.demo.bizee_demo`.

| Área | Ubicación |
|------|-----------|
| Backend Java | `src/main/java/com/bizee/demo/bizee_demo/` |
| Templates Thymeleaf | `src/main/resources/templates/` |
| Estáticos CSS/JS | `src/main/resources/static/` |
| Migraciones Flyway | `src/main/resources/db/migration/` |
| Tests | `src/test/java/...` |
| Infra local | `docker-compose.yml` |
| Rules Cursor | `.cursor/rules/` |

---

## 2. Stack tecnológico

| Área | Detectado en el proyecto |
|------|---------------------------|
| **HTML** | Thymeleaf templates (`templates/**/*.html`), HTML5, fragments de layout |
| **JavaScript** | Vanilla JS en `static/js/` (`app.js`, `http.js`, `states-select.js`); sin framework JS |
| **Bootstrap** | 5.3.3 CSS + `bootstrap.bundle.min.js` vía CDN en `fragments/layout.html` / `login-layout.html` |
| **Iconos** | Bootstrap Icons 1.11.3 (CDN) |
| **Librerías externas (CDN)** | Bootstrap, Bootstrap Icons |
| **APIs** | REST propias bajo `/api/**` (companies, registered-agents, users, states); sin APIs de terceros detectadas |
| **Build** | Maven (`pom.xml`, `./mvnw`), plugin `spring-boot-maven-plugin`, Surefire incluye `*Test`, `*Tests`, `*IT` |
| **Linter** | **TODO** — no hay ESLint, Checkstyle, SpotBugs ni config equivalente en el repo |
| **Formateadores** | **TODO** — no hay Prettier / EditorConfig / Spotless detectados |
| **Testing** | JUnit (vía starters Boot), MockMvc, Mockito (unit `CompanyServiceTest`), Testcontainers Postgres+RabbitMQ, Awaitility |
| **CI/CD** | **TODO** — no hay workflows `.github/` ni pipelines en el repo |
| **Dependencias importantes** | `spring-boot-starter-data-jpa`, `webmvc`, `thymeleaf`, `validation`, `flyway`, `amqp`, `postgresql`, `spring-security-crypto`, testcontainers, awaitility |
| **Runtime local** | Docker Compose: Postgres `localhost:5432` (db/user/pass `bizee`), RabbitMQ `5672` / UI `15672` (user/pass `bizee`) |

---

## 3. Arquitectura

### Organización de carpetas (Java)

| Paquete / carpeta | Responsabilidad |
|-------------------|-----------------|
| `controller` | `@RestController` (`/api/**`) y `@Controller` Thymeleaf |
| `service` | Reglas de negocio (`CompanyService`, `RegisteredAgentService`, `UserService`, `UserAuthService`, `StatesService`) |
| `repository` | Spring Data JPA |
| `entity` | Entidades JPA (`User`, `Company`, `RegisteredAgent`) |
| `domain` | Tipos de dominio (p. ej. `RegisteredAgentType`) |
| `dto` | Request/response/forms |
| `exception` | `BusinessException`, `ResourceNotFoundException`, `GlobalExceptionHandler` |
| `auth` | Constantes/helpers de sesión (`SessionAuth`) |
| `config` | `WebMvcConfig`, interceptor, `PasswordEncoderConfig` |
| `event` | Topology, config AMQP, `dto`, `publish`, `consume` |

### Recursos web

| Ruta | Responsabilidad |
|------|-----------------|
| `templates/fragments/` | Layout admin, login layout, sidebar, header, alerts, confirm modal |
| `templates/companies/`, `registered-agents/`, `users/` | Listas y formularios CRUD |
| `templates/login.html`, `home.html` | Auth y home |
| `static/css/app.css` | Variables CSS y base |
| `static/css/layout.css` | Shell admin (sidebar, header, contenido) |
| `static/css/components.css` | Componentes (`app-capacity-*`, etc.) |
| `static/js/http.js` | Helper HTTP (`fetch` JSON) |
| `static/js/app.js` | Validación de forms, loading en submit, confirm modal |
| `static/js/states-select.js` | Puebla `select[data-states-select]` desde `GET /api/states` |

### Flujo de navegación (UI)

1. Usuario no autenticado → `/login` (layout de login).
2. Login OK → sesión (`AUTHENTICATED_USER_ID`, `AUTHENTICATED_DISPLAY_NAME`, `AUTHENTICATED_EMAIL`) → `/` o `/home`.
3. Rutas protegidas por `SessionAuthInterceptor` (excluye `/api/**`, `/login`, estáticos, `/error`).
4. Sidebar: Home, Companies, Registered agents, Users.
5. Logout: `POST /logout` invalida sesión.

### Flujo de datos

**REST**

```
Cliente → RestController → Service → Repository → PostgreSQL
                ↓ (asignación RA servicio)
         EventPublisher → RabbitMQ → EventConsumer → log “MAIL SENT (simulated)”
```

- Companies: ownership vía header **`X-User-Id`**.
- Users / Registered agents (API): CRUD admin/demo sin ownership por header.
- States: `GET /api/states` público (sin `X-User-Id`).

**Thymeleaf**

```
Browser → Controller → Service (mismos servicios) → Repository
         → Model + template → HTML
```

- Companies web: listado de todas + filtro `?userId=`; create usa el user de sesión.
- Registered agents web: listado + filtro `?state=`.
- State dropdowns: JS → `GET /api/states`.

### Dominio relevante

- **Use registered agent service** (`useRegisteredAgentService`):
  - `true` → asigna agente de `registered_agents` en el estado (capacidad + balanceo por menor carga).
  - `false` → el usuario dueño es el RA (`RegisteredAgentType.USER`).
- Capacidad de estado para umbral 90%:  
  `(companies del estado con tipo REGISTERED_AGENT) / (suma capacity de agentes del estado)`.
- UI de capacidad por agente: ACTUAL/TOTAL + swatch verde ≤30%, amarillo ≤60%, rojo >60% (métrica **por agente**, distinta del umbral de evento **por estado**).

### Componentes reutilizables (UI)

- `fragments/layout.html` — shell admin + Bootstrap + CSS/JS.
- `fragments/login-layout.html` — login.
- `fragments/sidebar.html`, `header.html`.
- `fragments/ui.html` — flash alerts + modal de confirmación.
- Forms con `data-app-validate`; deletes con `data-app-confirm`.
- Selects de estado: `data-states-select` + `/js/states-select.js`.

### Utilidades compartidas

- `http.js` — `fetchJson` / errores de red.
- `app.js` — enhance forms/modals.
- `StatesService` — fuente única de códigos/nombres US (+ DC).

---

## 4. Convenciones de desarrollo

### Java

- Paquete base: `com.bizee.demo.bizee_demo`.
- Controllers REST: `*Controller` bajo `/api/...`.
- Controllers web: `*WebController` (Thymeleaf) o `AuthController` / `HomeController`.
- Services: `*Service`; publishers/consumers en `event.publish` / `event.consume`.
- DTOs: records o clases; forms Thymeleaf como `*Form` con JavaBean setters para binding.
- Excepciones de negocio → `BusinessException`; no encontrado → `ResourceNotFoundException`.
- Indentación observada: tabs en Java y muchos templates.
- Comentarios: Javadoc breve en servicios/eventos cuando aclara reglas (p. ej. umbral, after-commit).

### Archivos frontend

- Templates por dominio: `companies/list.html`, `companies/form.html`, etc.
- CSS: `app.css`, `layout.css`, `components.css`.
- JS: kebab-case (`states-select.js`); IIFE / módulos por archivo sin bundler.

### Nombres

- Campos enviados al backend / `th:field` / JSON: **no renombrar** sin migración coordinada (p. ej. `useRegisteredAgentService`, `userId`, `state`).
- Clases CSS personalizadas: prefijo **`app-`**.

### Organización

- Lógica de negocio **solo** en services.
- Controllers web no duplican reglas; delegan a services.
- Eventos: DTOs propios; no reutilizar DTOs de API como payload AMQP.

---

## 5. Reglas de interfaz

**Todas las interfaces deben seguir** `.cursor/rules/frontend-styles.mdc`.

Antes de generar o modificar cualquier pantalla, el agente debe:

1. Leer `frontend-styles.mdc`.
2. Reutilizar fragments (`layout`, `sidebar`, `header`, `ui`).
3. Preferir utilidades/componentes Bootstrap 5.
4. No introducir otra librería CSS que compita con Bootstrap.
5. Iconos solo con Bootstrap Icons cuando se necesiten.

---

## 6. Estándares HTML

Alineados con el código actual y `frontend-styles.mdc`:

- HTML5 semántico (`header`, `main`, `aside`, `section`, `nav` donde aplique).
- Layout admin: sidebar + header + `container-fluid` + page header + contenido en `card`.
- Formularios: `label` + `for`/`id`, `form-label`, `form-control` / `form-select`, `invalid-feedback`, `novalidate` + `data-app-validate` cuando corresponda.
- Tablas: `table-responsive`, `table table-hover align-middle`, `thead.table-light`, `scope="col"`, acciones a la derecha con `btn-sm`.
- Botones: `type` explícito; primario `btn-primary`; secundario `btn-outline-secondary`; destructivo `btn-danger` / `btn-outline-danger` con confirmación.
- Responsive: grid Bootstrap, offcanvas sidebar en viewports &lt; `lg`.
- Accesibilidad: labels asociados, `aria-label` / `aria-current` en nav, capacidad no solo por color (texto / `visually-hidden` / `aria-label`).
- Thymeleaf: `th:replace` del layout fragment; flash via `fragments/ui :: alerts`.

---

## 7. Estándares JavaScript

- No usar `var`; preferir `const`, `let` solo si hay reasignación.
- `async/await` + `try/catch` en llamadas HTTP (`http.js`, `states-select.js`).
- Eventos con `addEventListener` (no `onclick` / `onsubmit` inline).
- Comprobar existencia de elementos antes de usarlos.
- Evitar globales; envolver en IIFE o scope de módulo.
- No usar `innerHTML` con datos de usuario/API sin sanitizar; preferir `textContent`, `createElement`, `replaceChildren`, `append`.
- No cambiar endpoints, métodos HTTP, headers ni shapes JSON al refactorizar JS.
- Centralizar HTTP en `http.js` y UX de forms/confirm en `app.js`.

---

## 8. Estándares CSS

- **Bootstrap primero**; CSS custom solo cuando Bootstrap no cubra el caso.
- Variables en `:root` (`app.css`): `--app-sidebar-*`, `--app-capacity-*`, etc.
- Clases propias con prefijo `app-` (p. ej. `app-shell`, `app-capacity-swatch`, `capacity-green|yellow|red` usadas desde Java/`RegisteredAgentResponse`).
- Sin estilos inline ni bloques `<style>` en templates (centralizar en CSS).
- Evitar `!important` e IDs como selectores de estilo.
- Archivos: `app.css` (tokens/base), `layout.css` (estructura), `components.css` (piezas UI).

**Nota:** las clases `capacity-green|yellow|red` las expone el backend en `RegisteredAgentResponse.capacityColorClass()`; no renombrar sin actualizar Java + CSS.

---

## 9. Flujo recomendado para nuevas funcionalidades

1. Analizar funcionalidad y rules (`.cursor/rules/*`, este `AGENTS.md`).
2. Reutilizar services, fragments, CSS y JS existentes.
3. Mantener consistencia visual (Bootstrap + layout admin).
4. Mantener accesibilidad y responsive.
5. No romper contratos API / nombres de campos / auth.
6. Si hay `@RestController` o endpoints nuevos: agregar/actualizar `*IT` con Testcontainers (ver `controller-integration-tests.mdc`).
7. Si hay eventos: DTOs + publish + consume dedicados (ver `event-driven-messaging.mdc`).

---

## 10. Antes de modificar código

Todo agente debe:

- Buscar reutilización (fragments, services, `http.js`, `StatesService`).
- Buscar código similar (otro CRUD web/REST).
- Revisar dependencias (`pom.xml`, Compose, Rabbit topology).
- Revisar impacto en tests IT y en selectores JS (`data-states-select`, `data-app-validate`, `data-app-confirm`).
- Mantener compatibilidad de endpoints y payloads.
- **Nunca** modificar UI sin revisar `frontend-styles.mdc`.
- **Nunca** modificar capas Java sin revisar `spring-boot-architecture.mdc`.

---

## 11. Antes de crear código nuevo

Verificar si ya existe:

| Buscar | Dónde |
|--------|--------|
| Layout / nav / alerts / confirm | `templates/fragments/` |
| Form / table patterns | `companies/`, `registered-agents/`, `users/` |
| HTTP / validate / confirm JS | `static/js/` |
| States dropdown | `StatesService`, `GET /api/states`, `states-select.js` |
| Password hashing | `PasswordEncoderConfig` + `UserAuthService` / `UserService` |
| Eventos RA / capacity | `event/**` |
| IT base | `AbstractControllerIT`, `TestcontainersConfiguration` |

Si existe algo similar, **reutilizarlo o extenderlo**.

---

## 12. Checklist obligatorio

Antes de finalizar una tarea:

- [ ] Compila (`./mvnw -DskipTests compile` o tests relevantes) con **Java 21**.
- [ ] No rompe funcionalidades existentes (REST + Thymeleaf + eventos).
- [ ] No introduce duplicación innecesaria.
- [ ] UI sigue Bootstrap 5 y `frontend-styles.mdc`.
- [ ] Responsive y accesibilidad básicos conservados.
- [ ] Consistencia visual con el layout admin.
- [ ] Sin código muerto ni TODOs innecesarios en el diff.
- [ ] Controllers nuevos/cambiados tienen IT (Testcontainers) según la rule.
- [ ] No se alteraron endpoints/payloads/nombres de campos salvo requisito explícito.
- [ ] Docker/Testcontainers: Postgres + RabbitMQ disponibles cuando se corren ITs.

---

## 13. Buenas prácticas

- Pensar antes de modificar; cambiar solo lo necesario.
- Preferir claridad sobre ingenierización excesiva.
- Funciones/métodos pequeños; una responsabilidad.
- Explicar decisiones no obvias (umbral 90%, after-commit, ownership).
- Publicar eventos de dominio **después del commit** cuando el side effect no deba ocurrir si hay rollback.
- No enviar SMTP real: notificaciones = logs simulados.
- Seeds: password en claro = email; en BD = BCrypt (V1/V3). Login con el email como password.

---

## 14. Restricciones

Nunca:

- Reescribir archivos completos sin necesidad.
- Cambiar APIs públicas, endpoints, métodos HTTP o shapes JSON sin requisito explícito.
- Cambiar nombres de campos enviados al backend (`th:field`, JSON, form `name`).
- Cambiar comportamiento funcional (capacidad, balanceo, umbral 90%, ownership) sin requisito.
- Agregar Spring Security completo “por defecto” (el demo usa sesión propia + crypto BCrypt).
- Agregar librerías CSS/JS que dupliquen Bootstrap sin justificación.
- Usar H2 para ITs de controllers (usar Testcontainers).
- Meter `RabbitTemplate` o “enviar mail” dentro de controllers / lógica de dominio fuera de publish/consume.
- Exponer passwords de usuarios en responses API.
- Duplicar estilos, fragments o reglas de negocio.

---

## 15. Mantenimiento

Cuando el proyecto evolucione, **actualizar este archivo** (y las rules en `.cursor/rules/`) para reflejar:

- Nuevas carpetas o paquetes.
- Nuevas convenciones.
- Nuevas dependencias o versiones mayores.
- Nuevos estándares UI/backend.
- Nuevos fragments, helpers JS o servicios compartidos.
- Cambios en auth, mensajería o contratos API.

**TODO** (no inferible / ausente hoy):

- Pipeline CI/CD.
- Linters/formateadores formales (Checkstyle, ESLint, Spotless, etc.).
- Descripción/name del artefacto Maven en `pom.xml` (campos vacíos).
- Guía de despliegue a producción / perfiles Spring distintos de local.
- Política formal de versionado semántico / branching.

---

## Referencia rápida: cómo correr

```bash
docker compose up -d
export JAVA_HOME=…/jdk-21   # el proyecto requiere Java 21
./mvnw spring-boot:run
./mvnw test                 # requiere Docker para Testcontainers
```

Login seed (ej.): `alice.johnson@example.com` / `alice.johnson@example.com`.

API companies: header `X-User-Id: <id>`.
