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

## API
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

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

## SonarQube Cloud

[Sonar Analysis](https://sonarcloud.io/summary/overall?id=borya-chip_OrderTracker&branch=main)

Для передачи покрытия тестов в SonarQube Cloud проект теперь генерирует JaCoCo XML-отчет в стандартный путь `target/site/jacoco/jacoco.xml`.

GitHub Actions workflow находится в `.github/workflows/ci.yml` и выполняет:

- сборку
- линтинг через `checkstyle`
- unit-тесты
- генерацию JaCoCo coverage report
- отправку анализа в SonarQube Cloud

Для работы workflow в GitHub repository settings нужно задать:

- secret `SONAR_TOKEN`
- variable `SONAR_ORGANIZATION`, если organization key в SonarQube Cloud отличается от owner репозитория
- variable `SONAR_PROJECT_KEY`, если нужно переопределить текущее значение по умолчанию `borya-chip_OrderTracker`

Локальный запуск с покрытием:

```bash
./mvnw -Pcoverage verify
```

Отправка анализа вместе с покрытием в SonarQube Cloud:

```bash
./mvnw -Pcoverage verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.token=$SONAR_TOKEN
```
