# Chess Swiss Management System

Swiss-system tournament management app with a Vue 3 frontend and Spring Boot backend.

## Local Development

```bash
./start.sh
```

Starts both backend (`:8080`) and frontend dev server (`:3000` with API proxy to `:8080`).

Alternatively, run each separately:

```bash
cd backend && ./gradlew bootRun   # Backend on :8080
cd frontend && npm run dev         # Frontend on :3000
```

## Deployment (Railway)

### Prerequisites

- [Railway](https://railway.app) account (Starter plan: $5/mo, includes $5 credit)
- GitHub repository connected to Railway

### Steps

1. **Create a Railway project**
   - Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo**
   - Select this repository

2. **Add a Persistent Volume**
   - In your project dashboard, click **New** → **Volume**
   - Mount point: `/data` (1GB free)
   - This ensures the H2 file database survives redeploys

3. **Set Environment Variables**
   - Go to your service's **Variables** tab and add:

   | Variable | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:h2:file:/data/chessdb;DB_CLOSE_DELAY=-1` |
   | `APP_JWT_SECRET` | Generate a random 32+ character string |
   | `APP_ADMIN_USERNAME` | Desired admin username |
   | `APP_ADMIN_PASSWORD` | Desired admin password |

4. **Deploy**
   - Railway auto-detects the `Dockerfile` at the project root
   - Push to `master` → automatic rebuild and deploy
   - Your app is live at `https://<project>.railway.app`

### Database

Default is H2 file-based (persisted to the Railway volume). Override with PostgreSQL by setting the `SPRING_DATASOURCE_*` and `SPRING_JPA_PLATFORM` env vars.

### Schema Migrations

Add Flyway migration files to `backend/src/main/resources/db/migration/`:
```
V2__add_column.sql
V3__create_new_table.sql
```
Applied automatically on next deploy.

### CI/CD

- **GitHub Actions**: push to `master` builds and pushes a Docker image to `ghcr.io`
- **Railway**: automatically deploys when it detects changes on the connected branch
