# SystemOTPJava

## Запуск с Docker (только БД)

### Требования
- Docker
- Docker Compose
- JDK 17+
- Gradle

### Шаги для запуска

1. Запустить базу данных PostgreSQL в Docker:
```bash
docker compose up -d
```

2. Запустить приложение локально:
```bash
./gradlew bootRun
```

Приложение будет доступно по адресу: http://localhost:8080

### Остановка базы данных
```bash
docker compose down
```

## Настройки подключения к базе данных

База данных PostgreSQL будет доступна по следующим параметрам:
- URL: jdbc:postgresql://localhost:5432/otp_db
- Username: postgres
- Password: postgres
- Schema: otp 