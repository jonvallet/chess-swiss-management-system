# Chess Swiss Management System - Specification (SPEC)

## 1. Project Overview
The **Chess Swiss Management System** is a lightweight, modern web application for managing chess tournaments using Swiss-system pairing rules. It features automated player registration, round-by-round pairings (incorporating score groups, color balancing, and bye-handling), and a real-time leaderboard with tie-break calculations.

---

## 2. Technical Stack
*   **Backend:** Java 26, Spring Boot 3.x, Spring Data JPA, Gradle (via Gradle Wrapper).
*   **Database:** Supabase (Cloud PostgreSQL) - accessed via standard PostgreSQL JDBC.
*   **Frontend:** Vue 3 (Composition API, TypeScript), Vite, Tailwind CSS, PrimeVue, Pinia, Vue Router.
*   **Hosting Compatibility:** Built for simple, cheap deployment (e.g., Supabase free tier, Render/Railway free tiers for Spring Boot/Vue). No local Docker required.

---

## 3. Database Schema (Supabase)
The database consists of four highly structured tables to handle tournaments, players, and match records:

```sql
-- 1. Players Directory
CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    rating INTEGER DEFAULT 1200,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Tournaments
CREATE TABLE tournaments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    total_rounds INTEGER NOT NULL,
    current_round INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, IN_PROGRESS, FINISHED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. Tournament Players (Many-to-Many Enrollment and Standings)
CREATE TABLE tournament_players (
    tournament_id UUID REFERENCES tournaments(id) ON DELETE CASCADE,
    player_id UUID REFERENCES players(id) ON DELETE CASCADE,
    score DECIMAL(3, 1) DEFAULT 0.0,
    color_difference INTEGER DEFAULT 0, -- Track balance of White vs Black color assignments (e.g., +1 for White, -1 for Black)
    PRIMARY KEY (tournament_id, player_id)
);

-- 4. Matches
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tournament_id UUID REFERENCES tournaments(id) ON DELETE CASCADE,
    round_number INTEGER NOT NULL,
    white_player_id UUID REFERENCES players(id),
    black_player_id UUID REFERENCES players(id),
    result VARCHAR(20) DEFAULT 'UNPLAYED', -- WHITE_WIN, BLACK_WIN, DRAW, UNPLAYED
    is_bye BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);
```

---

## 4. Swiss Pairing Algorithm Requirements
Our `SwissPairingService` will execute FIDE-compliant Swiss rules:
1.  **No Rematches:** A pair of players can only play each other once per tournament.
2.  **Score Grouping:** Players are sorted by current tournament score. The engine attempts to pair players with identical or near-identical scores.
    *   *Float Down:* If a score group has an odd number of players, the lowest-ranked player floats down to the next score group.
3.  **Color Balance:**
    *   No player should play the same color three times consecutively.
    *   The difference between a player's White games and Black games must be minimized (ideally $\le 1$, maximum difference of $2$ in extreme cases).
4.  **Bye Handling:** If the tournament enrollment is odd, the lowest-ranking player who has not yet received a bye is awarded a 1.0 point "bye" (matching them with a null/bye opponent).

---

## 5. REST API Contract

### Players
*   `GET /api/players` - Retrieve all players in the system database.
*   `POST /api/players` - Register a new player globally.

### Tournaments
*   `POST /api/tournaments` - Create a new tournament.
*   `GET /api/tournaments` - Retrieve all tournaments and statuses.
*   `POST /api/tournaments/{id}/register` - Register player IDs to a tournament (must be in `DRAFT` status).
*   `GET /api/tournaments/{id}/standings` - Retrieve current standings including computed scores and tie-breaks (e.g., Buchholz score).

### Rounds & Matchplay
*   `POST /api/tournaments/{id}/rounds/generate` - Compute and generate pairings for the next round.
*   `GET /api/tournaments/{id}/rounds/{roundNum}/matches` - Retrieve matches for a specific round.
*   `POST /api/matches/{id}/result` - Update a match result (`WHITE_WIN`, `BLACK_WIN`, `DRAW`).

---

## 6. Frontend Layout & User Experience (UX)

### Dashboard Layout
*   **Sidebar Navigation:** Dashboard, Players, Tournaments.
*   **Header:** Dynamic system state and notifications.

### Core Views
1.  **Players Directory:** Add new players, view system-wide ratings, search and filter.
2.  **Tournaments Dashboard:** Modern grid of tournaments showing badges like `DRAFT`, `IN_PROGRESS`, or `FINISHED`.
3.  **Tournament Detail Board:**
    *   *Standings Tab:* A robust, interactive PrimeVue `DataTable` showing ranking, names, ratings, and current scores.
    *   *Matches/Rounds Tab:* Visual columns/cards showing the current round's pairings with result entry buttons.
    *   *Registration Drawer:* Slider/drawer to enroll registered players into the tournament draft.
