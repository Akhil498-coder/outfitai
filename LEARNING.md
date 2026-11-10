# Learning log

## Phase 1: backend scaffold + health endpoint
- Built a Spring Boot 3.5 / Java 21 app with `GET /api/health`, split into controller -> service -> DTO.
- **Profiles**: `application-h2.yml` (default, in-memory) vs `application-postgres.yml` (reads `DB_URL`,
  `DB_USER`, `DB_PASSWORD` from env vars). One codebase, different config per environment; secrets never live in files.
- **Flyway**: the schema is defined by versioned SQL files (`db/migration/V1__init.sql`), and Hibernate only
  validates (`ddl-auto: validate`). Same schema on H2 and Postgres, and safe to deploy.
- **Tests**: `@WebMvcTest` loads only the web layer (service mocked, fast); `@SpringBootTest` loads everything.
- **Maven Wrapper** (`mvnw`) pins the Maven version so builds are the same on every machine.
