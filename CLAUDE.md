# OutfitAI

Wardrobe app: user uploads clothing photos, AI tags them, app recommends TOP 2 outfits
for today with match % and a one-line reason. Web first, then Capacitor wrap for iOS/Android.
The owner is learning: explain concepts briefly as you build.

## Stack
- Backend: Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL (H2 for early dev), Maven, JUnit 5
- Frontend: React + Vite + TypeScript, mobile-first
- Auth: email+password and Google sign-in -> backend issues its own JWT
- AI: Anthropic API vision, called from the backend only. It tags items (type, colors, warmth, formality, seasons).
- Scoring: deterministic Java ScoringService (weather via Open-Meteo, color harmony,
  formality, optional age/gender style prefs). The AI never invents percentages.
- Layout: /backend, /frontend

## Rules
- Layers: controller -> service -> repository. DTOs separate from entities.
- Secrets only from environment variables. NEVER read, print, or commit .env.
  Keep .env.example current with variable names only.
- Ask before product decisions. Plan each phase and wait for my OK.
- Small steps, one git commit each. Run tests before saying "done".
- After each feature, append 3-5 lines to LEARNING.md: what was built, why, key concept.
- Keep README run instructions accurate.

## Token discipline
- Don't re-read files you already have. Use grep/targeted reads, not whole directories.
- No long recaps. Short summaries of changes only.

## Phases
1 scaffold + health endpoint + DB | 2 auth (email + Google) | 3 upload + AI tagging
4 ScoringService + tests | 5 React UI | 6 deploy | 7 Capacitor + native camera + store prep

## Feature: Laundry cycle (build after scoring + UI work, as Phase 8)
- Each clothing item has a status: CLEAN, DIRTY, IN_LAUNDRY.
- When the user picks an outfit, its items become DIRTY the next day.
- ScoringService only considers CLEAN items.
- User profile stores a laundry preference (set at onboarding, editable): weekly wash day OR every N days.
- "Mark as washed" action returns items to CLEAN.
- Optional later: per-item-type "wears before wash" count (e.g. jackets/jeans > 1 wear).
- Schema changes go in new Flyway migrations. Do not rewrite existing ones.
