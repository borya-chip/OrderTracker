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
1. Подготовь `.env`:
   ```env
   POSTGRES_USER=order-tracker-user
   POSTGRES_PASSWORD=order-tracker-password
   POSTGRES_DB=order-tracker
   POSTGRES_PORT=5432
   POSTGRES_HOST=pg
   ORDER_TRACKER_PORT=8080
   ```
2. Подготовь папку для логов:
   ```bash
   mkdir -p logs
   ```
3. Подними контейнеры:
   ```bash
   docker compose up --build -d
   ```
4. Проверь, что контейнеры живые:
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
Логи:

- единый лог-файл приложения: `logs/order-tracker.log`
- ротация логов: при достижении `40KB` текущий лог архивируется в `logs/archive/*.log.gz`, запись продолжается в новом файле

Sonar https://sonarcloud.io/summary/overall?id=borya-chip_OrderTracker&branch=main
