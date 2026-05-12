# OrderTracker deployment

## Local Docker Compose

1. Check `.env` values:

```env
POSTGRES_USER=order-tracker-user
POSTGRES_PASSWORD=order-tracker-password
POSTGRES_DB=order-tracker
POSTGRES_PORT=5432
POSTGRES_HOST=pg
ORDER_TRACKER_PORT=8080
REGISTRY=local
BACKEND_IMAGE_NAME=order-tracker
FRONTEND_IMAGE_NAME=order-tracker-frontend
FRONTEND_PORT=3000
BACKEND_URL=http://order-tracker:8080
```

2. Build and start the application with PostgreSQL and frontend:

```bash
docker compose up --build
```

If old containers from a previous compose configuration are still running, stop them first:

```bash
docker compose down --remove-orphans
docker compose up --build
```

3. Check application health:

```bash
curl http://localhost:8080/api/v1/health
```

Expected response:

```json
{
  "service": "order-tracker",
  "status": "UP",
  "database": "UP",
  "timestamp": "2026-05-12T12:00:00"
}
```

4. Open the frontend:

```text
http://localhost:3000
```

5. Stop containers:

```bash
docker compose down
```

Use `docker compose down -v` only if you also want to delete the PostgreSQL volume.

Docker Compose starts three containers:

```text
postgres-order-tracker
order-tracker
order-tracker-frontend
```

## Render deployment

Render supports free web services and free PostgreSQL databases. The free PostgreSQL database has important limits, including a 30-day lifetime, so this option is suitable for coursework, demos, and testing rather than production.

1. Push the repository to GitHub.

2. In Render, create a PostgreSQL database:
   - New > Postgres.
   - Choose the free instance type.
   - Save the database name, user, password, host, port, and internal database URL.

3. Create the backend web service:
   - New > Web Service.
   - Connect the GitHub repository.
   - Select Docker as the runtime/language.
   - Use the repository root as the root directory.
   - Keep `Dockerfile` as the Dockerfile path.
   - Choose the free instance type.

4. Set environment variables for the web service:

```env
POSTGRES_USER=<render database user>
POSTGRES_PASSWORD=<render database password>
POSTGRES_DB=<render database name>
POSTGRES_HOST=<render internal database host>
POSTGRES_PORT=5432
DB_URL=jdbc:postgresql://<render internal database host>:5432/<render database name>
```

Do not set `POSTGRES_HOST=pg` on Render. That value is only for local Docker Compose.

5. Set the health check path:

```text
/api/v1/health
```

6. Deploy the service and open:

```text
https://<your-render-service>.onrender.com/api/v1/health
```

The endpoint should return HTTP 200 and `"status": "UP"`.

7. To deploy the frontend on Render, create one more Docker web service:
   - Root directory: `frontend`.
   - Dockerfile path: `Dockerfile`.
   - Health check path: `/`.
   - Add this environment variable:

```env
BACKEND_URL=https://ordertracker-9l11.onrender.com
```

The frontend nginx config proxies `/api`, `/v3`, and `/swagger-ui` to `BACKEND_URL`. Locally Docker Compose sets it to `http://order-tracker:8080`; on Render set it to the public backend URL.

Useful Render docs:
- Docker on Render: https://render.com/docs/docker
- Web services: https://render.com/docs/web-services
- Environment variables and secrets: https://render.com/docs/configure-environment-variables
- Health checks: https://render.com/docs/health-checks
- Free plan limits: https://render.com/docs/free

## GitHub Actions CI/CD instructions

The project uses the same reusable workflow structure as `FinanceTracker`:

```text
.github/workflows/pipeline.yaml
.github/workflows/build.yaml
.github/workflows/lint.yaml
.github/workflows/test.yaml
.github/workflows/push.yaml
.github/workflows/deploy.yaml
.github/workflows/health.yaml
```

Pipeline flow:

1. `build.yaml`
   - Builds the Spring Boot backend.
   - Builds the React frontend.

2. `lint.yaml`
   - Runs backend Checkstyle.
   - Runs frontend ESLint.
   - Runs SonarQube if `SONAR_TOKEN` is configured.

3. `test.yaml`
   - Runs backend tests with coverage.

4. `push.yaml`
   - Builds backend and frontend Docker images.
   - Pushes them only if registry variables and secrets are configured.
   - For Render source deploys, this still validates Docker builds even without a registry.

5. `deploy.yaml`
   - Calls Render deploy hooks for backend and frontend.

6. `health.yaml`
   - Checks backend `/api/v1/health`.
   - Checks frontend `/`.

7. `pipeline.yaml`
   - Calls workflows in this order: build, lint, test, push, deploy, health.

Add these GitHub secrets:

```text
RENDER_BACKEND_DEPLOY_HOOK_URL
RENDER_FRONTEND_DEPLOY_HOOK_URL
BACKEND_URL
FRONTEND_URL
```

Optional GitHub secrets:

```text
REGISTRY_USERNAME
REGISTRY_PASSWORD
SONAR_TOKEN
```

Optional GitHub variables:

```text
REGISTRY
BACKEND_IMAGE_NAME
FRONTEND_IMAGE_NAME
SONAR_ORGANIZATION
SONAR_PROJECT_KEY
```

Recommended values:

```text
BACKEND_URL=https://ordertracker-9l11.onrender.com
FRONTEND_URL=https://<your-frontend-service>.onrender.com
BACKEND_IMAGE_NAME=order-tracker-backend
FRONTEND_IMAGE_NAME=order-tracker-frontend
```

If Render builds directly from GitHub and you do not push Docker images to a registry, do not configure registry secrets. The workflow will build Docker images locally for validation and skip pushing them.
