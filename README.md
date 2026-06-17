# Log Analyzer

Консольная утилита для анализа access-логов NGINX. Приложение читает локальные и удаленные log-файлы, разбирает записи стандартного формата NGINX, считает статистику по запросам и сохраняет отчет в выбранном формате.

Исходное техническое задание сохранено в [requirements.md](requirements.md).

## Возможности

- чтение одного файла, набора файлов по маске или удаленного файла по `http`/`https`;
- поддержка входных файлов с расширениями `.log` и `.txt`;
- фильтрация записей по датам через параметры `--from` и `--to` в формате ISO 8601;
- пропуск некорректных строк лога с записью предупреждений в лог;
- расчет общего количества запросов;
- расчет среднего, максимального и 95-го перцентиля размера ответа;
- подсчет частоты HTTP-кодов ответа;
- определение топ-10 самых популярных ресурсов;
- расчет распределения запросов по датам;
- сбор уникальных протоколов из запросов;
- сохранение отчета в форматах `json`, `markdown` и `adoc`;
- валидация аргументов, формата вывода, расширения выходного файла и доступности путей.

## Формат входных данных

Утилита ожидает строки access-лога NGINX в формате:

```text
$remote_addr - $remote_user [$time_local] "$request" $status $body_bytes_sent "$http_referer" "$http_user_agent"
```

Пример строки:

```text
93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
```

## Технический стек

- Java 24;
- Maven 3.9.11+;
- Picocli для CLI-интерфейса;
- Jackson для JSON/YAML и сериализации отчетов;
- Log4j 2 и SLF4J для логирования;
- JUnit 5, AssertJ, Awaitility и Instancio для тестов;
- JaCoCo для отчета о покрытии;
- Maven Shade Plugin для сборки исполняемого jar;
- Spotless, PMD, SpotBugs и Modernizer для контроля качества кода;
- Docker для запуска собранного приложения в контейнере.

## Параметры запуска

| Параметр | Обязательный | Описание |
| --- | --- | --- |
| `-p`, `--path` | да | Путь к локальному файлу, маска файлов или URL. Можно передать несколько значений. |
| `-f`, `--format` | да | Формат отчета: `json`, `markdown` или `adoc`. |
| `-o`, `--output` | да | Путь к выходному файлу. Файл не должен существовать заранее. |
| `--from` | нет | Начальная дата анализа в формате `YYYY-MM-DD`. |
| `--to` | нет | Конечная дата анализа в формате `YYYY-MM-DD`. |
| `-h`, `--help` | нет | Показать справку Picocli. |
| `-V`, `--version` | нет | Показать версию приложения. |

Расширение выходного файла должно соответствовать формату:

- `json` -> `.json`;
- `markdown` -> `.md`;
- `adoc` -> `.ad`.

## Запуск

### Требования

Установите JDK 24. Maven можно не устанавливать отдельно: в проекте есть Maven Wrapper.

Проверить версию Java:

```shell
java --version
```

### Сборка

Linux/macOS:

```shell
./mvnw clean package
```

Windows:

```shell
mvnw.cmd clean package
```

После сборки jar-файл будет находиться в директории `target`.

### Запуск jar-файла

```shell
java -jar target/hw3-logs-1.0.jar --path logs/access.log --format markdown --output report.md
```

Пример с маской файлов и фильтрацией по датам:

```shell
java -jar target/hw3-logs-1.0.jar --path "logs/*.log" --format json --output report.json --from 2015-05-17 --to 2015-05-18
```

Пример с удаленным файлом:

```shell
java -jar target/hw3-logs-1.0.jar --path https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs --format adoc --output report.ad
```

### Запуск через Maven

```shell
./mvnw exec:java -Dexec.mainClass=academy.Application -Dexec.args="--path logs/access.log --format markdown --output report.md"
```

Для Windows используйте `mvnw.cmd` вместо `./mvnw`.

### Запуск в Docker

Сначала соберите jar-файл:

```shell
./mvnw clean package
```

Затем соберите Docker-образ:

```shell
docker build -t log-analyzer .
```

Пример запуска с примонтированной директорией проекта:

```shell
docker run --rm -v "$(pwd):/app/data" log-analyzer --path /app/data/logs/access.log --format markdown --output /app/data/report.md
```

## Тесты и проверки

Запуск тестов:

```shell
./mvnw test
```

Запуск форматирования:

```shell
./mvnw spotless:apply
```

Запуск статического анализа:

```shell
./mvnw pmd:check spotbugs:check modernizer:modernizer
```

Acceptance-тесты также можно запустить скриптом:

```shell
./scripts/run_acceptance_tests.sh
```
