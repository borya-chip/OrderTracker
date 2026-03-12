# Order Tracker

REST API для работы с заказами.

## Стек
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Docker
- Maven

## Запуск в Docker (БД + приложение)
1. Скопируй пример переменных:
   ```bash
   cp .env.example .env
   ```
2. Подними контейнеры:
   ```bash
   docker compose up --build -d
   ```
3. Проверь, что контейнеры живые:
   ```bash
   docker compose ps
   docker logs -f order-tracker-app
   docker logs -f postgres-order-tracker
   ```

## Запуск приложения локально
Чтобы приложение взяло те же переменные из `.env`, выполни:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

## Полезные команды
Остановить контейнеры:

```bash
docker compose down
```

Остановить и удалить volume с данными:

```bash
docker compose down -v
```
Sonar[https://sonarcloud.io/summary/overall?id=borya-chip_OrderTracker&branch=main]
