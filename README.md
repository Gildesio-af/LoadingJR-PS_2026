# Chat System Loading 🚀

Mensageria instantânea (web + API) com autenticação JWT, WebSocket e relatório por IA.

## Tecnologias 🛠️
### **Backend**
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) 
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
* **WebSocket:** Comunicação bidirecional em tempo real.
* **JPA:** Persistência de dados simplificada.

### **Frontend**
![React](https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB)
![Vite](https://img.shields.io/badge/vite-%23646CFF.svg?style=for-the-badge&logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/typescript-%23007ACC.svg?style=for-the-badge&logo=typescript&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)

### **Infraestrutura & DevOps**
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/docker%20compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### **Testes**
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
* **Mockito:** Framework para criação de mocks no Java.
* **Vitest:** Testes unitários de próxima geração para o frontend.

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
