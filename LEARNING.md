# Learning log

## Phase 1: backend scaffold + health endpoint
- Built a Spring Boot 3.5 / Java 21 app with `GET /api/health`, split into controller -> service -> DTO.
- **Profiles**: `application-h2.yml` (default, in-memory) vs `application-postgres.yml` (reads `DB_URL`,
  `DB_USER`, `DB_PASSWORD` from env vars). One codebase, different config per environment; secrets never live in files.
- **Flyway**: the schema is defined by versioned SQL files (`db/migration/V1__init.sql`), and Hibernate only
  validates (`ddl-auto: validate`). Same schema on H2 and Postgres, and safe to deploy.
- **Tests**: `@WebMvcTest` loads only the web layer (service mocked, fast); `@SpringBootTest` loads everything.
- **Maven Wrapper** (`mvnw`) pins the Maven version so builds are the same on every machine.

## Phase 2: authentication
- Built email/password and Google sign-in; both return our own **JWT**, plus `GET/PUT /api/users/me`.
- **JWT**: a signed string `{user id, expiry}`. The server stores no session; each request sends
  `Authorization: Bearer <token>` and `JwtAuthFilter` verifies the signature. Tampered/expired tokens are rejected.
- **Passwords** are stored as BCrypt hashes (slow, salted, one-way). Login failures all give the same message
  so attackers can't discover which emails exist.
- **Google**: the frontend gets an ID token from Google; the backend verifies Google's signature and that it
  was issued for our `GOOGLE_CLIENT_ID`, then issues its own JWT. The verifier sits behind an interface so tests fake it.
- **Spring Security filter chain**: a list of filters every request passes through; `SecurityConfig` says which URLs
  are public. Entities never leave the backend: controllers return DTOs (`UserResponse`) without the password hash.
