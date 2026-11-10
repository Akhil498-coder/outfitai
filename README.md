# OutfitAI

Wardrobe app: upload clothing photos, AI tags them, the app recommends the top 2 outfits for today.
See `CLAUDE.md` for the stack and phases. Currently: **Phase 1 (backend scaffold)**.

## Prerequisites
- Java 21+ (`java -version`)
- PostgreSQL 16 (only for the `postgres` profile)

No Maven install needed: use the bundled wrapper `./mvnw`.

## Run the backend (H2, zero setup)
```bash
cd backend
./mvnw spring-boot:run
curl localhost:8080/api/health
# {"status":"UP","db":"UP","time":"..."}
```
H2 is an in-memory database (data resets on restart). Console: http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:outfitai`, user `sa`, empty password).

## Run the backend on PostgreSQL
1. Create the database once: `createdb outfitai`
2. Copy `.env.example` to `.env` and fill in `DB_URL` (e.g. `jdbc:postgresql://localhost:5432/outfitai`),
   `DB_USER`, `DB_PASSWORD`. `.env` is git-ignored.
3. Export the variables and start with the `postgres` profile:
```bash
cd backend
set -a; source ../.env; set +a
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```
(Alternative: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`.)

## Tests
```bash
cd backend && ./mvnw test
```
