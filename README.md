# OutfitAI

Wardrobe app: upload clothing photos, AI tags them, the app recommends the top 2 outfits for today.
See `CLAUDE.md` for the stack and phases. Currently: **Phase 2 done (auth)**.

## Prerequisites
- Java 21+ (`java -version`)
- PostgreSQL 16 (only for the `postgres` profile)

No Maven install needed: use the bundled wrapper `./mvnw`.

## Configuration
Copy `.env.example` to `.env` (git-ignored) and fill it in. Always required: `JWT_SECRET`
(32+ random characters, e.g. `openssl rand -base64 48`) and `GOOGLE_CLIENT_ID`.
The postgres profile also needs `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Run the backend (H2, zero setup database)
```bash
cd backend
set -a; source ../.env; set +a      # loads the variables into this shell
./mvnw spring-boot:run
curl localhost:8080/api/health
# {"status":"UP","db":"UP","time":"..."}
```
H2 is in-memory (data resets on restart). Console: http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:outfitai`, user `sa`, empty password).

## Run the backend on PostgreSQL
1. Create the database once: `createdb outfitai`
2. In `.env` set `DB_URL` (e.g. `jdbc:postgresql://localhost:5432/outfitai`), `DB_USER`, `DB_PASSWORD`
   (leave empty if your local user has no password).
3. Start with the `postgres` profile:
```bash
cd backend
set -a; source ../.env; set +a
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```
Flyway creates/updates the tables automatically on startup.

## API (Phase 2)
| Method | Path | Auth | Body | Notes |
|---|---|---|---|---|
| POST | `/api/auth/register` | no | `{email, password (8-72), displayName}` | 201, returns `{token, user}` |
| POST | `/api/auth/login` | no | `{email, password}` | returns `{token, user}` |
| POST | `/api/auth/google` | no | `{idToken}` | Google ID token from the frontend; returns `{token, user}` |
| GET | `/api/users/me` | Bearer | | current user |
| PUT | `/api/users/me` | Bearer | `{displayName, age?(13-120), gender?}` | `gender`: FEMALE, MALE, NON_BINARY, OTHER, PREFER_NOT_TO_SAY. Omitted age/gender are cleared |

Email and Google accounts are separate: an email registered one way is rejected (409) the other way.
Errors always look like `{"status":409,"error":"Conflict","message":"..."}`.

```bash
curl -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"me@example.com","password":"password123","displayName":"Me"}'
curl localhost:8080/api/users/me -H "Authorization: Bearer <token from above>"
```
Google sign-in needs a real ID token, which the React app will obtain in Phase 5 (Google Sign-In button).
Until then it is covered by tests with a faked verifier.

## Tests
```bash
cd backend && ./mvnw test      # uses fake JWT_SECRET/GOOGLE_CLIENT_ID, no .env needed
```
