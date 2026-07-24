# Bizee Demo

Spring Boot 4 demo: REST API (`/api/**` with `X-User-Id`) plus a simple Thymeleaf browser UI.

## Browser web login (no Spring Security)

Browser routes use session-based auth against the `users` table (email + **BCrypt**-hashed password).
Only `spring-security-crypto` is used for hashing/verification — not full Spring Security.

Seeded users use their **email as the password** (stored as BCrypt in the DB):

| Email | Password (plaintext to type at login) | Name |
|-------|----------------------------------------|------|
| `alice.johnson@example.com` | `alice.johnson@example.com` | Alice Johnson |
| `brian.smith@example.com` | `brian.smith@example.com` | Brian Smith |
| `carla.davis@example.com` | `carla.davis@example.com` | Carla Davis |
| `daniel.wilson@example.com` | `daniel.wilson@example.com` | Daniel Wilson |
| `emma.brown@example.com` | `emma.brown@example.com` | Emma Brown |

- Login: `GET/POST /login` (email + password)
- Home: `GET /` and `GET /home` (requires session)
- Logout: `POST /logout`
- Domain CRUD (session required): `/companies`, `/registered-agents`, `/users`
- Companies list: `GET /companies` shows all companies; optional `?userId=` filters by owner. New companies are owned by the logged-in session user.

Session attributes after login: `AUTHENTICATED_USER_ID`, `AUTHENTICATED_DISPLAY_NAME`, `AUTHENTICATED_EMAIL`.

`/api/**` is excluded from the session interceptor and continues to use `X-User-Id` only (required for company ownership). Registered-agent and user REST CRUD are open admin/demo endpoints.

## How to test

### Prerequisites

- **Java 21** (JDK)
- **Docker** running
- Maven (`mvn`) or the project wrapper (`./mvnw`)

### Steps

1. Start Postgres and RabbitMQ:

```bash
docker compose up -d
```

2. Run the app, either:
   - Open the project in an IDE (e.g. **IntelliJ IDEA**) and run the Spring Boot main class, or
   - From a terminal (with JDK 21 available):

```bash
./mvnw spring-boot:run
```

   (or `mvn spring-boot:run` if you use a global Maven install)

3. Open the app: [http://localhost:8080](http://localhost:8080)

4. Log in with a seeded user. Password is the same as the email, for example:

| Email | Password |
|-------|----------|
| `alice.johnson@example.com` | `alice.johnson@example.com` |

## AI questions

### 1) If you used AI, tell me what you have done differently from what AI originally proposed?

AI was used as an implementation assistant. The first proposals were often incomplete or too “demo-like”; as the developer I steered the solution toward more standard practices. Differences and improvements I requested (and that are now in the codebase) include:

- **Layered backend**: RestController → Service → Repository, DTOs for requests/responses, and a centralized `@RestControllerAdvice` for errors (captured in Cursor rules so agents keep following them).
- **Testing standards**: every controller must have Spring Boot **integration tests**; later move ITs to **Testcontainers** (Postgres + RabbitMQ) instead of depending only on a local DB.
- **Event-driven notifications with RabbitMQ**: dedicated event DTOs, publish/consume services, topology config — not ad-hoc `RabbitTemplate` calls or “use the queue just to send a mail.”
- **Web UI without full Spring Security**: Thymeleaf admin panel; then login against the real **`users` table** with **BCrypt** (password plaintext = email in seed data), not a hardcoded demo user.
- **Full CRUD** for companies, registered agents, and users (REST + Thymeleaf), with ownership and FK-safe deletes.
- **UX / data standards**: state dropdowns populated via **`GET /api/states`** (not hardcoded options); agents list ordered by state/name; capacity shown as **ACTUAL/TOTAL** with utilization colors; companies list shows all rows + **User** column and filter; agents list filterable by state.
- **Frontend standardization**: Bootstrap 5 admin layout (sidebar/header/cards), shared Thymeleaf fragments, centralized CSS/JS, forms/tables/alerts aligned with `.cursor/rules/frontend-styles.mdc`.
- **Docs**: keep the README practical (simple How to test) and document agent/architecture conventions.

In short: AI drafted features quickly; I pushed architecture, testing, messaging, security basics, CRUD completeness, and UI consistency so the app looks and behaves closer to a standard Spring Boot admin + API project.

### 2) What verification do you perform to make sure your proposed solution works?

Verification combines automated and manual checks:

- **Unit tests** for core domain logic (e.g. `CompanyServiceTest`: assignment, capacity rules, load balancing).
- **Integration tests** (`*IT`) with **MockMvc** against a full Spring context, using **Testcontainers** for PostgreSQL and RabbitMQ. These cover the main API and web flows (companies, registered agents, users, states, auth/session UI paths, and domain-event notification where applicable).
- **Manual testing** after running `docker compose` and the app: login, CRUD screens, filters, state dropdowns, create company with/without registered-agent service, capacity indicators, and smoke checks of the REST API with `X-User-Id`.

Together, automated tests protect the main flows in CI-like local runs, and manual passes confirm end-to-end UX and infrastructure (Postgres, RabbitMQ, browser) behave as expected.
