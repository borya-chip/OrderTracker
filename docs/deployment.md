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
   - The current nginx config proxies `/api`, `/v3`, and `/swagger-ui` to `http://order-tracker:8080`, which works inside Docker Compose. For separate Render services, replace `proxy_pass http://order-tracker:8080;` in `frontend/nginx.conf` with the public backend URL or use Render rewrite/proxy rules.

Useful Render docs:
- Docker on Render: https://render.com/docs/docker
- Web services: https://render.com/docs/web-services
- Environment variables and secrets: https://render.com/docs/configure-environment-variables
- Health checks: https://render.com/docs/health-checks
- Free plan limits: https://render.com/docs/free

## GitHub Actions CI/CD instructions

The current project already has a CI workflow for Maven verification. To implement the full CI/CD pattern from `FinanceTracker`, split the pipeline into reusable workflows:

```text
.github/workflows/pipeline.yaml
.github/workflows/build.yaml
.github/workflows/test.yaml
.github/workflows/push.yaml
.github/workflows/deploy.yaml
.github/workflows/health.yaml
```

Recommended flow:

1. `build.yaml`
   - Checkout repository.
   - Set up JDK 21.
   - Cache Maven dependencies.
   - Set up Node.js 22.
   - Build backend:

```bash
./mvnw -B -DskipTests package
```

   - Build frontend:

```bash
cd frontend
npm install
npm run build
```

2. `test.yaml`
   - Checkout repository.
   - Set up JDK 21.
   - Cache Maven dependencies.
   - Run backend tests:

```bash
./mvnw -B verify
```

   - Optionally run frontend lint:

```bash
cd frontend
npm install
npm run lint
```

3. `push.yaml`
   - Run only on `push`.
   - Log in to a Docker registry.
   - Build and push the backend image:

```bash
docker buildx build \
  --no-cache \
  --push \
  --tag $REGISTRY/$BACKEND_IMAGE_NAME:latest \
  --file Dockerfile \
  .
```

   - Build and push the frontend image:

```bash
docker buildx build \
  --no-cache \
  --push \
  --tag $REGISTRY/$FRONTEND_IMAGE_NAME:latest \
  --file frontend/Dockerfile \
  frontend
```

4. `deploy.yaml`
   - For Render, use one of these approaches:
     - Connect Render directly to GitHub and let Render auto-deploy after pushes to `main`.
     - Or call a Render Deploy Hook from GitHub Actions after build/test/push.

Example deploy hook step:

```yaml
- name: Deploy to Render
  run: curl -X POST "${{ secrets.RENDER_DEPLOY_HOOK_URL }}"
```

5. `health.yaml`
   - Wait for deploy.
   - Call the public health endpoint.

Example:

```yaml
- name: Health check
  run: |
    for attempt in $(seq 1 30); do
      if curl --fail --silent "${{ secrets.APP_URL }}/api/v1/health"; then
        exit 0
      fi

      echo "Attempt ${attempt}/30 failed"
      sleep 10
    done

    exit 1
```

6. `pipeline.yaml`
   - Call workflows in this order:
     - build
     - test
     - push
     - deploy
     - health

Required GitHub secrets:

```text
REGISTRY_USERNAME
REGISTRY_PASSWORD
RENDER_DEPLOY_HOOK_URL
APP_URL
```

Required GitHub variables:

```text
REGISTRY
BACKEND_IMAGE_NAME
FRONTEND_IMAGE_NAME
```

If Render builds directly from GitHub and you do not push Docker images to a registry, the registry secrets and `push.yaml` step are optional. In that simpler setup, GitHub Actions runs build/test, then triggers Render deploy, then checks `/api/v1/health`.
