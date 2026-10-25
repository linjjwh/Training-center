# TrainingCentre — Учебный центр

Информационная система учебного центра. Основная сущность — **заявка на обучение**.

- **КР1** — консольное приложение (`org.example.Main`) + PostgreSQL
- **КР2** — настольное JavaFX-приложение (`org.example.fx.TrainingCentreFxApplication`) с подключением к той же базе через JDBC

## Стек

Java 21 · JavaFX 21 · Maven · PostgreSQL 18 · JDBC · Docker Compose · Apache POI (экспорт в .xlsx)

## Структура

| Слой | Пакеты / каталоги |
|---|---|
| View (FXML + CSS) | `src/main/resources/org/example/fx/ui` (FXML-экраны), `src/main/resources/org/example/fx/app.css` |
| Controller / экраны JavaFX | `org.example.fx.ui` |
| Входная точка JavaFX | `org.example.fx.TrainingCentreFxApplication`, лаунчер — `org.example.fx.TrainingCentreLauncher` |
| Сборка сервисов для экранов | `org.example.fx.ServiceFactory` |
| Диалоги JavaFX (Ui: сообщения, подтверждения) | `org.example.fx.util` |
| Service (бизнес-правила, валидация, статистика) | `org.example.service` |
| Repository / DAO (SQL) | `org.example.repository` |
| Model | `org.example.model` |
| Утилиты (подключение к БД, экспорт) | `org.example.util` |
| SQL-скрипт схемы и тестовых данных | `src/main/java/org/example/sql/database.sql` |

## Порядок запуска

Нужны: Docker, Java 21+, Maven 3.9+.

1. **База данных** (Docker обязателен):

   ```bash
   docker compose up -d --wait
   ```

   При первом запуске на пустом volume таблицы и тестовые данные создаются
   автоматически из `database.sql`. Проверка: `docker compose ps` — статус
   у `postgres` должен быть `healthy`.

2. **JavaFX-приложение (КР2):**

   ```bash
   mvn javafx:run
   ```

3. **Консольное приложение (КР1):**

   ```bash
   mvn exec:java
   # или запустить org.example.Main из IDE
   ```

Остановка базы: `docker compose down` (данные сохраняются в volume;
не добавляйте `-v`, если данные нужны).

## Подключение к базе

Параметры контейнера PostgreSQL заданы напрямую в `docker-compose.yml`
(пользователь `postgres`, пароль `postgres`, база `trainingcentre`, порт 5432).

JavaFX-приложение стучится на `jdbc:postgresql://localhost:5432/trainingcentre` —
эта строка лежит в `src/main/resources/database.properties`
(`db.url`, `db.user`, `db.password`) и должна совпадать с настройками compose.

## Возможности JavaFX-приложения (КР2)

- вкладка «Заявки»: таблица (TableView + ObservableList), добавление/изменение/
  просмотр/удаление с подтверждением, поиск (слушатель, курс, комментарий),
  фильтры (статус, форма обучения, период дат), сортировка (6 вариантов +
  клик по заголовкам столбцов), экспорт текущего набора в .xlsx;
- вкладка «Слушатели»: CRUD связанной сущности, поиск, просмотр заявок
  выбранного слушателя;
- вкладка «Статистика»: 12 показателей (всего, по статусам, по формам обучения,
  доля одобренных, заявки за 30 дней, самый популярный курс, среднее число
  заявок на слушателя);
- валидация ввода в формах, понятные сообщения об ошибках, статус подключения
  к базе в нижней панели главного окна.
