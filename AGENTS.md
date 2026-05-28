# AGENTS.md

## Project Structure

Monorepo with two independent packages:
- `backend/` - Java 26, Spring Boot 3.4.1, Gradle
- `frontend/` - Vue 3, TypeScript, Vite, Tailwind CSS v4

## Commands

**Backend** (run from `backend/`):
```bash
./gradlew build          # Build + run tests
./gradlew bootRun        # Start dev server on :8080
./gradlew test           # Run tests only
```

**Frontend** (run from `frontend/`):
```bash
npm run dev              # Start dev server on :3000 (proxies /api to :8080)
npm run build            # Typecheck (vue-tsc) + production build
```

**Both services**:
```bash
./start.sh               # Starts backend + frontend together
```

## Key Architecture

- **Database**: Backend defaults to H2 in-memory DB. Override with env vars (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_DRIVER`, `SPRING_JPA_PLATFORM`) for PostgreSQL.
- **API proxy**: Frontend dev server proxies `/api/*` to backend at `localhost:8080` (configured in `vite.config.ts`).
- **Swiss pairing**: `SwissPairingService` implements FIDE rules: score groups, color balance (max 2 difference), no rematches, bye handling. Complex logic in `backend/src/main/java/com/jonvallet/chess/swiss/service/SwissPairingService.java`.

## Testing

- Backend: JUnit tests in `backend/src/test/`. Only `SwissPairingServiceTest` exists.
- Frontend: No test framework configured.

## Notes

- Frontend has no lint or standalone typecheck scripts; `npm run build` runs `vue-tsc -b` before vite build.
- Backend uses `spring.jpa.hibernate.ddl-auto=update` (auto-creates schema from entities).
- Chess domain: Tournaments have states (`DRAFT`, `IN_PROGRESS`, `FINISHED`), players have ratings and color differences, matches track results and byes.
