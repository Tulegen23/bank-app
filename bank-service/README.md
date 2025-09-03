<p align="center">
  <img src="images/NS-removebg-preview.png" alt="Bank System Logo" width="180"/>
</p>
<p align="center"><H1><b>🏦 Банковская система: bank-service & customer-service</b></H1></p>
<H3>Современное решение для управления банковскими счетами и клиентами, построенное на Spring Boot, PostgreSQL и REST API.</H3>

<H2><b>🌟 О проекте</b></H2>

Эта система состоит из двух микросервисов:

<b>🏧 bank-service:</b> Управляет счетами, переводами (внутренними/внешними) и JWT-аутентификацией.  
<b>👤 customer-service:</b> Отвечает за создание, обновление, удаление клиентов и логирование их активности.

🔗 Сервисы взаимодействуют через REST API, обеспечивая надежность, валидацию данных и логирование.

<H2><b>🚀 Быстрый старт</b></H2>
<H2><b>📋 Требования</b></H2>

<b>Описание</b>

ОС
Windows 11 Professional (или др.)

Java
JDK 17

Maven
Для сборки проекта

PostgreSQL
Версия 12+

Инструменты
Postman, cURL или браузер

<H2><b>🗄️ Настройка базы данных</b></H2>

Создайте базу в PostgreSQL:CREATE DATABASE bank_db;

Проверьте настройки в application.properties:spring.datasource.url=jdbc:postgresql://localhost:5432/bank_db
spring.datasource.username=postgres
spring.datasource.password=password01

<H2><b>🛠️ Установка и запуск</b></H2>

Клонируйте репозиторий:git clone <URL_репозитория>

Соберите сервисы:cd bank-service && mvn clean install
cd ../customer-service && mvn clean install

Запустите:cd customer-service && mvn spring-boot:run
cd ../bank-service && mvn spring-boot:run

<H2><b> Доступ:</b></H2>

customer-service: http://localhost:8081  
bank-service: http://localhost:8082  
Swagger UI:  
http://localhost:8081/swagger-ui.html  
http://localhost:8082/swagger-ui.html

<H2><b>🛠️ Эндпоинты API</b></H2>
👤 customer-service (http://localhost:8081)

Метод
Эндпоинт
Описание
Параметры/Тело


🟢 GET
/api/customers/{iin}
Получить клиента по ИИН
Путь: iin (12 цифр)


🟢 GET
/api/customers/exists/{id}
Проверить существование клиента
Путь: id (число)

🔵 POST
/api/customers
Создать клиента
JSON: {"fullName": "Иван Иванов", "iin": "010101123456", "phone": "87773590198"}

🟡 PUT
/api/customers/{id}
Обновить клиента
Путь: id, JSON: {"fullName": "Иван Петров", "phone": "87071234567"}

🔴 DELETE
/api/customers/{id}
Удалить клиента
Путь: id

🟢 GET
/api/customers/{id}/activities
Получить активности клиента
Путь: id

🔵 POST
/api/customers/{id}/log-activity
Логировать активность
Путь: id, JSON: {"actionType": "TRANSFER", "details": "Перевод 1000 KZT"}

🏧 bank-service (http://localhost:8082)

Метод
Эндпоинт
Описание
Параметры/Тело

🔵 POST
/api/login
Аутентификация (JWT токен)
JSON: {"username": "admin", "password": "admin123"}

🔵 POST
/api/accounts
Открыть счет
Параметры: clientId=1, currency=KZT

🟢 GET
/api/accounts/{clientId}
Получить счета клиента
Путь: clientId (число)

🔵 POST
/api/transfers
Выполнить перевод
JSON: {"fromAccountNumber": "KZ-1234123412341234", "toAccountNumber": "KZ-5678567856785678", "amount": 1000, "currency": "KZT", "isExternal": false}

🔒 Важно: Для всех эндпоинтов bank-service, кроме /api/login, нужен заголовок Authorization: Bearer <JWT_TOKEN>.

<H2><b>🧪 Тестирование</b></H2>
<H2><b>📚 Через Swagger UI</b></H2>
Откройте в браузере:

http://localhost:8081/swagger-ui.html (customer-service)

http://localhost:8082/swagger-ui.html (bank-service)

🖥️ Примеры cURL

1. Создание клиента:

curl -X POST http://localhost:8081/api/customers \
-H "Content-Type: application/json" \
-d '{"fullName":"Иван Иванов","iin":"010101123456","phone":"87773590198"}'

2. Аутентификация:

curl -X POST http://localhost:8082/api/login \
-H "Content-Type: application/json" \
-d '{"username":"admin","password":"admin123"}'

3. Открытие счета:

curl -X POST "http://localhost:8082/api/accounts?clientId=1&currency=KZT" \
-H "Authorization: Bearer <JWT_TOKEN>"

4. Перевод:

curl -X POST http://localhost:8082/api/transfers \
-H "Content-Type: application/json" \
-H "Authorization: Bearer <JWT_TOKEN>" \
-d '{"fromAccountNumber":"KZ-1234123412341234","toAccountNumber":"KZ-5678567856785678","amount":1000,"currency":"KZT","isExternal":false}'

📜 Логи:
Проверяйте операции в:

logs/audit.log (bank-service)

logs/customer-service-audit.log (customer-service)

📚 Технологии и зависимости

Зависимость

Версия

Назначение

Spring Boot

3.3.0

Основа для REST API и микросервисов

Spring Data JPA

3.3.0

Работа с PostgreSQL через Hibernate

PostgreSQL

runtime

Драйвер для подключения к базе

Lombok

1.18.32

Упрощение кода (геттеры, сеттеры)

Spring Validation

3.3.0

Валидация данных (@NotBlank, @Pattern)

Spring AOP

3.3.0

Логирование операций (@LogAction)

Springdoc OpenAPI

2.5.0

Документация API и Swagger UI

Spring Dotenv

2.3.0

Переменные окружения из .env

Spring Security

3.3.0

JWT-аутентификация (bank-service)

JJWT

0.11.5

Генерация и валидация JWT (bank-service)

Resilience4j

2.2.0

Повторные попытки (bank-service)

MapStruct

1.5.5.Final

Маппинг DTO ↔ сущности (customer-service)

TestNG

7.11.0

Тестирование (customer-service)

🏛️ Архитектура

Микросервисы: Разделение на bank-service и customer-service для модульности.

Логирование: Logback с ротацией и аспектами (@LogAction).

Безопасность: JWT в bank-service, расширяемо для customer-service.

Асинхронность: @Async и пул потоков для операций.

Надежность: Resilience4j для повторных попыток.

Обработка ошибок: Стандартизированные ответы (ApiResponse).

🔧 Идеи для улучшения

🧪 Добавить тесты (MockMvc, TestNG).

🔒 Использовать BCrypt для паролей и авторизацию в customer-service.

⚡ Кэшировать курсы валют и оптимизировать запросы к базе.

📖 Создать Postman-коллекцию и улучшить Swagger.

🐳 Настроить Docker для развертывания.

📬 Контакты

✉️ Email: example@nurbank.kz
🌐 GitHub: <вставьте ссылку на репозиторий>