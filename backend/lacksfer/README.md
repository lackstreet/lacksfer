# Lacksfer

Backend didattico per file transfer stile WeTransfer.

## Stack

- Java 21
- Quarkus
- Gradle
- PostgreSQL
- Flyway
- Hibernate ORM
- Azurite
- Docker Compose

## Requisiti

- JDK 21
- Docker Desktop
- Git Bash o terminale compatibile

## Avvio infrastruttura

Dalla root del repo:

```bash
docker compose up -d
docker compose ps
```

## Avvio backend

Da `backend/lacksfer`:

```bash
./gradlew quarkusDev
```

## Health check

```bash
curl http://localhost:8080/health
```

## Test

```bash
./gradlew test
```

## Upload manuale

```bash
echo "hello lacksfer" > test.txt

curl -X POST http://localhost:8080/transfers/upload \
  -F "file=@test.txt;type=text/plain" \
  -F "expiresAt=2026-05-20T12:00:00Z"
```

## Download manuale

Usa il `downloadToken` ricevuto dall'upload:

```bash
curl -L -o downloaded.txt http://localhost:8080/transfers/{downloadToken}/download
cat downloaded.txt
```

## Stato MVP

Funzionalità presenti:

- upload file piccoli via backend
- salvataggio metadata su PostgreSQL
- salvataggio file su Azurite
- download tramite token
- scadenza transfer
- migration DB con Flyway
- filename sanitization
- limite dimensione upload

## Roadmap breve

- Angular frontend
- upload diretto su Blob Storage con SAS token
- supporto file grandi
- crittografia
- password download
- invio email
