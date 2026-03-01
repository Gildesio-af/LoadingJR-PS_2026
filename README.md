# Chat System Loading 🚀

Mensageria instantânea (web + API) com autenticação JWT, WebSocket e relatório por IA.

## Tecnologias 🛠️
- Backend: Spring Boot 4, JPA/Hibernate, SpringnSecurity, WebSocket, PostgreSQL
- Frontend: React + Vite, TypeScript, Tailwind/Nginx
- Infra: Docker/Docker Compose
- Testes: JUnit 5, Mockito, Vitest

## Pré-requisitos 📦
- Docker e Docker Compose
- Node.js 20+ (apenas se rodar o front localmente)

## Variáveis de ambiente 🔑
Crie um `.env` na raiz (mesmo nível de `chat-api` e `chat-front`):
```
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-me
GOOGLE_API_KEY=your-google-key
```
O back lê esses valores em `application.yaml`.

## Rodando tudo com Docker 🐳
```bash
cd chat-api
docker compose up --build -d

cd ../chat-front
docker compose up --build -d
```
- API: http://localhost:8080
- DB: localhost:5432 (`chat-db`)
- Front: http://localhost/
- Seed: `chat-api/src/main/resources/data.sql` executa ao subir a API.

## Rodando local (sem Docker) 💻
Backend:
```bash
cd chat-api
./mvnw spring-boot:run
```
(ou `mvn spring-boot:run`). Requer PostgreSQL local com DB `chatdb` e credenciais do `.env`.


Por padrão usa `http://localhost:8080`; para customizar, crie `.env` em `chat-front` com `VITE_API_BASE_URL=http://seu-host:8080` e reinicie.

## Testes ✅
Backend (JUnit 5):
```bash
cd chat-api
mvn test
```
Frontend (Vitest):
```bash
cd chat-front
npm test
```

## Build manual 📦
Backend JAR:
```bash
cd chat-api
mvn clean package -DskipTests
```
Frontend estático:
```bash
cd chat-front
npm install
npm run build
```

## Estrutura 🗂️
- `chat-api/`: Spring Boot API (PostgreSQL, WebSocket, JWT). Seed em `src/main/resources/data.sql`.
- `chat-front/`: React/Vite + Tailwind. Configs em `src/services/api.ts`.

## Dicas 🔧
- Cache Maven chato? `mvn -U clean test`.
- Resetar dados seedados no Docker: `docker volume rm chat-api_postgres_data`.
