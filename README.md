# Order Tracker

Restaurant Order Tracker is a laboratory full-stack project for managing restaurant orders. The backend exposes a Spring REST API, and the frontend is a React SPA styled as a light manager dashboard.

## Stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Maven
- Docker / Docker Compose
- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Axios
- React Hook Form
- Zod
- lucide-react

## Implemented Features

- Manager dashboard with real totals:
  - total orders
  - total customers
  - total meals
  - total restaurants
  - recent orders
  - meals by category
  - quick actions
- CRUD pages:
  - Customers
  - Categories
  - Restaurants
  - Meals
  - Orders
- Client-side filters:
  - customers by name or email
  - categories by name
  - restaurants by name, city, address, email, or phone
  - meals by name, category, restaurant, or price
- Backend-supported features:
  - meals pagination and sorting
  - orders filtering by date range
  - restaurant search by meal category and price range
- Detail pages for entity relationships.
- Loading, error, and empty states.
- Dialog forms and confirm dialogs for create, edit, and delete flows.

## Relationships Shown

- OneToMany:
  - Customer -> Orders
  - Category -> Meals
  - Restaurant -> Meals
- ManyToMany:
  - Order -> Meals
  - Meal -> Orders

## CRUD Operations

- Customers:
  - list, create, update, delete, details
- Categories:
  - list, create, update, delete, details
- Restaurants:
  - list, create, update, delete, details
- Meals:
  - paginated list, create, update, delete, details
- Orders:
  - list, date filter, create, update, delete, details

## Backend API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Run Backend With Docker

1. Create `.env` in the project root:

   ```env
   POSTGRES_USER=order-tracker-user
   POSTGRES_PASSWORD=order-tracker-password
   POSTGRES_DB=order-tracker
   POSTGRES_PORT=5432
   POSTGRES_HOST=pg
   ORDER_TRACKER_PORT=8080
   ```

2. Create logs directory:

   ```bash
   mkdir -p logs
   ```

3. Start database and backend:

   ```bash
   docker compose up --build -d
   ```

4. Check containers:

   ```bash
   docker compose ps
   docker logs -f order-tracker-app
   docker logs -f postgres-order-tracker
   ```

## Run Backend Locally

Load environment variables from `.env` and start Spring Boot:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

## Run Frontend

The frontend is in `frontend/`.

```bash
cd frontend
npm install
npm run dev
```

Vite runs on:

```text
http://localhost:5173
```

If `5173` is busy, Vite will choose the next available port.

The frontend uses Vite proxy for API calls:

```text
/api -> http://localhost:8080
```

You can also set a custom API base URL:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Useful Commands

Backend tests:

```bash
./mvnw test
```

Frontend checks:

```bash
cd frontend
npm run lint
npx tsc -b
npm run build
```

Stop Docker containers:

```bash
docker compose down
```

Stop Docker containers and remove database volume:

```bash
docker compose down -v
```

## SonarQube Cloud

[Sonar Analysis](https://sonarcloud.io/summary/overall?id=borya-chip_OrderTracker&branch=main)

Coverage report is generated at `target/site/jacoco/jacoco.xml`.

Local coverage run:

```bash
./mvnw -Pcoverage verify
```

Sonar run with coverage:

```bash
./mvnw -Pcoverage verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.token=$SONAR_TOKEN
```
