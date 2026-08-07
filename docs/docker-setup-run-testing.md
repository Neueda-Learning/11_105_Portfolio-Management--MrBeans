# Docker Setup, Run, and Testing Guide (Linux VM)

This file is the single source for Docker setup and runtime testing on the Linux VM.

## 1. Prerequisites

On the Linux VM, make sure these are available:

- Docker Engine
- Docker Compose standalone command: `docker-compose`
- Git

Validate:

```bash
docker --version
docker-compose --version
```

## 2. Clone and checkout branch

```bash
git clone https://github.com/Neueda-Learning/11_105_Portfolio-Management--MrBeans.git
cd 11_105_Portfolio-Management--MrBeans
git checkout feature/setting-CICD
```

## 3. Environment variables

Create runtime env file:

```bash
cp .env.example .env
```

Update `.env`:

```env
DB_NAME=portfoliomanager
DB_USER=pm_user
DB_PASSWORD=pm_password
DB_ROOT_PASSWORD=root
CHATBOT_PROVIDER=groq
GROQ_API_KEY=<your_groq_key>
GROQ_MODEL=llama-3.3-70b-versatile
GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.0-flash
```

## 4. Start Docker stack (default mode)

Default ports from `docker-compose.yml`:

- Frontend: `80`
- Backend: `8080`
- MySQL: `3306`

Start:

```bash
docker-compose up -d --build
```

Check:

```bash
docker-compose ps
```

## 5. Start Docker stack (VM custom-port mode)

Use this mode if port `80` is blocked but `8080` is reachable.

- Frontend exposed on `8080`
- Backend exposed on `8081`
- Backend API proxied through frontend on `/api/*`

Start:

```bash
docker-compose down
BACKEND_PORT=8081 FRONTEND_PORT=8080 docker-compose up -d --build
docker-compose ps
```

## 6. Runtime tests on VM

### Default mode tests

```bash
curl -I http://localhost/
curl -I http://localhost:8080/swagger-ui/index.html
curl -s http://localhost:8080/api/investments
```

### VM custom-port mode tests

```bash
curl -I http://localhost:8080
curl -I http://localhost:8081/actuator/health
curl -I http://localhost:8080/swagger-ui/index.html
curl -s http://localhost:8080/api/investments
```

### Create investment API test (frontend proxy path)

```bash
curl -i -X POST "http://localhost:8080/api/investments" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"AAPL","name":"Apple Inc.","type":"STOCK","currency":"USD"}'
```

Expected: `HTTP/1.1 201`.

## 7. Optional dev data seeding

Seeder endpoint exists and is intended for development/testing data.

Endpoint:

- `POST /api/dev/seed`

Example:

```bash
curl -X POST "http://localhost:8080/api/dev/seed?investments=18&transactionsPerInvestment=36&snapshotDays=180&wipeExistingData=true"
```

Notes:

- Use seeder manually in dev/test environment when needed.
- Do not run this by default in production deployment pipelines.
- If `wipeExistingData=true`, it clears existing portfolio data before inserting dummy data.

## 8. Logs and troubleshooting

### Container status and logs

```bash
docker-compose ps
docker logs poma-backend --tail=200
docker logs poma-frontend --tail=200
docker logs poma-mysql --tail=200
```

### Common issues

1. `Access denied for user ...` from backend:
- DB user/password in `.env` do not match MySQL initialization.
- Fix `.env`, then recreate:

```bash
docker-compose down -v
docker-compose up -d --build
```

2. `Bind for 0.0.0.0:8080 failed: port is already allocated`:
- Another process/container is using 8080.
- Check owner:

```bash
sudo ss -ltnp | grep :8080 || true
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}" | grep 8080 || true
```

If backend is incorrectly occupying 8080, stop and rerun with explicit ports:

```bash
docker-compose down
BACKEND_PORT=8081 FRONTEND_PORT=8080 docker-compose up -d --build
```

3. Frontend opens but API call fails:
- Check backend health and proxy path.
- In single-port mode, API should be called via `http://<vm-ip>:8080/api/...`.

## 9. Stop/start commands

```bash
docker-compose stop
docker-compose start
docker-compose down
```
