# SystemOTPJava

Система двухфакторной аутентификации с поддержкой различных методов доставки OTP-кодов.

## Настройка SMTP для отправки OTP-кодов по email

Система поддерживает отправку OTP-кодов по электронной почте. Для настройки этой функциональности необходимо указать параметры подключения к SMTP-серверу в файле конфигурации.

### Настройка для разработки (локально)

1. Создайте файл `.env` в корне проекта и укажите в нем следующие параметры:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

Для Gmail необходимо использовать пароль приложения, а не обычный пароль аккаунта. [Инструкция по созданию пароля приложения](https://support.google.com/accounts/answer/185833).

### Настройка для production

В production-окружении эти же переменные должны быть заданы в переменных окружения системы:

```shell
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### Тестирование без SMTP-сервера

Если SMTP-сервер не настроен (параметры не заданы), система будет работать в режиме "логирования" - коды OTP будут выводиться в лог приложения вместо отправки на email. Это удобно для разработки и тестирования.

Вы увидите сообщение в логе:
```
SMTP not configured! OTP code for user@example.com would be: 123456
Configure spring.mail.* properties to enable actual email sending
```

## Другие методы доставки OTP-кодов

Система также поддерживает отправку OTP-кодов через:
- SMS (необходима интеграция с SMS-провайдером)
- Telegram (необходима интеграция с Telegram Bot API)

В текущей реализации эти методы симулируются с выводом кодов в лог приложения.

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

## Конфигурация окружения

Для защиты конфиденциальных данных, все секретные параметры вынесены в переменные окружения. Перед запуском приложения установите следующие переменные:

### Обязательные переменные окружения

```
# База данных
DB_USERNAME=ваш_пользователь_бд
DB_PASSWORD=ваш_пароль_бд
DB_URL=jdbc:postgresql://localhost:5432/otp_db

# Почтовый сервер
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=ваша_почта@gmail.com
MAIL_PASSWORD=ваш_пароль_приложения

# SMS API (Exolve)
SMS_API_KEY=ваш_api_ключ_exolve
SMS_SENDER_NUMBER=имя_отправителя_или_номер

# Сервер
SERVER_PORT=8081
```

### Способы настройки окружения

1. **Для разработки:**
   - Создайте файл `src/main/resources/application-dev.yml` с вашими настройками
   - Запускайте с профилем `dev`: `-Dspring.profiles.active=dev`
   - Файл уже добавлен в .gitignore и не будет коммититься в репозиторий

2. **Для продакшена:**
   - Используйте переменные окружения сервера
   - Или создайте файл `.env` в корне проекта с переменными окружения

3. **Для Docker:**
   - Используйте переменные окружения в docker-compose.yml или при запуске контейнера 