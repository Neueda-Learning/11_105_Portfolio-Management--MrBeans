# Linux VM Docker Runbook (Frontend + Backend + MySQL)

This guide starts the full stack on a Linux VM and verifies it by VM IP and ports.

## 1) Prerequisites on the VM

- Docker Engine installed
- Docker Compose plugin installed (`docker compose` command)
- Git installed
- Ports open on VM firewall/security group:
  - 80 (frontend)
  - 8080 (backend API)
  - 3306 (MySQL, only if remote DB access is needed)

### Ubuntu example

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg git
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo $VERSION_CODENAME) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
```

## 2) Clone repository on VM

```bash
git clone <YOUR_REPO_URL> portfolio-manager
cd portfolio-manager
```

## 3) Create runtime env file

```bash
cp .env.example .env
```

Edit `.env` with at least these values:

```env
DB_NAME=portfoliomanager
DB_USER=pm_user
DB_PASSWORD=pm_password
DB_ROOT_PASSWORD=root
CHATBOT_PROVIDER=groq
GROQ_API_KEY=<YOUR_GROQ_KEY>
GROQ_MODEL=llama-3.3-70b-versatile
GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.0-flash
```

## 4) Start the stack

From repository root:

```bash
docker compose --env-file .env up -d --build
```

## 5) Verify containers and health

```bash
docker compose ps
docker compose logs -f --tail=100
```

Expected containers:
- poma-mysql
- poma-backend
- poma-frontend

## 6) Verify from VM and from your laptop

Find VM IP:

```bash
hostname -I | awk '{print $1}'
```

Then test:

- Frontend: `http://<VM_IP>/`
- Backend Swagger: `http://<VM_IP>:8080/swagger-ui.html`
- Backend API sample: `http://<VM_IP>:8080/api/investments`

If frontend loads but API fails, check backend logs:

```bash
docker logs poma-backend --tail=200
```

## 7) Verify MySQL is running correctly

From VM shell:

```bash
docker exec -it poma-mysql mysql -u"$DB_USER" -p"$DB_PASSWORD" -e "SHOW DATABASES;"
docker exec -it poma-mysql mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "SHOW TABLES;"
```

If tables are missing, check backend migration logs:

```bash
docker logs poma-backend | grep -i flyway
```

## 8) Common fixes

1. Port already in use:
   - Stop local service using that port, then restart compose.

2. MySQL auth errors after changing credentials:
   - Remove old DB volume and recreate stack:

```bash
docker compose down -v
docker compose --env-file .env up -d --build
```

3. Firewall blocks access from outside:
   - Open ports 80/8080 (and 3306 only if needed).

4. Backend starts before DB is ready:
   - Current compose waits on MySQL healthcheck; inspect MySQL logs if backend keeps restarting.

## 9) Stop/start commands

```bash
docker compose stop
docker compose start
docker compose down
```
