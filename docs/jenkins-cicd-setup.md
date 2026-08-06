# Jenkins CI/CD Setup and Test Guide

This guide covers complete Jenkins setup for this repository on a Linux VM, including UI configuration and testing the full pipeline.

## 1. Preconditions on Linux VM

- Jenkins is installed and reachable at `http://<VM_IP>:<JENKINS_PORT>/`
- Docker daemon is running
- Jenkins user can run Docker commands
- Git is available on the VM

Verify on VM:

```bash
sudo systemctl status jenkins
docker --version
docker-compose --version || docker compose version
```

If Jenkins cannot run Docker, add user and restart:

```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

## 2. First Login to Jenkins UI

1. Open Jenkins URL in browser.
2. If asked for unlock key, get it from VM:

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

3. Install suggested plugins.
4. Create admin user.

## 3. Required Jenkins Plugins

From Manage Jenkins -> Plugins, ensure these are installed:

- Pipeline
- Git
- Credentials Binding
- Docker Pipeline

## 4. Add Jenkins Credentials

From Manage Jenkins -> Credentials -> (global):

1. Add "Username with password"
- ID: `dockerhub-credentials`
- Username: your Docker Hub username
- Password: Docker Hub access token

2. Add "Secret text"
- ID: `groq-api-key`
- Secret: your GROQ API key

## 5. Update Image Names in Jenkinsfile

In `Jenkinsfile`, set real Docker Hub image names:

- `BACKEND_IMAGE_NAME = '<dockerhub-user>/finora-backend'`
- `FRONTEND_IMAGE_NAME = '<dockerhub-user>/finora-frontend'`

Do not keep placeholder `YOUR_DOCKERHUB_USERNAME`.

## 6. Create the Jenkins Job

Recommended: Pipeline job (or Multibranch Pipeline).

### Option A: Pipeline from SCM

1. New Item -> Pipeline
2. Pipeline -> Definition: Pipeline script from SCM
3. SCM: Git
4. Repository URL: `https://github.com/Neueda-Learning/11_105_Portfolio-Management--MrBeans.git`
5. Branch Specifier:
- For branch testing: `*/feature/adding-jenkins`
- For production path: `*/main`
6. Script Path: `Jenkinsfile`
7. Save

## 7. Trigger Setup (Optional but recommended)

### GitHub webhook

- In Jenkins job: enable "GitHub hook trigger for GITScm polling"
- In GitHub repo -> Settings -> Webhooks:
  - Payload URL: `http://<JENKINS_HOST>:<JENKINS_PORT>/github-webhook/`
  - Content type: `application/json`
  - Events: push

## 8. Pipeline Behavior

Current pipeline stages:

1. Validate Pipeline Config
2. Checkout
3. Backend Build & Test (Dockerized Maven)
4. Frontend Build & Test (Dockerized Node)
5. Build Docker Images
6. Push Docker Images (main branch only)
7. Deploy To VM (main branch only)
8. Post-Deploy Smoke Test (main branch only)

Branch policy:

- Feature branches: run build/test/image-build
- Main branch: also push, deploy, and smoke test

## 9. How to Test CI/CD Properly

## Test A: Feature branch validation (no deploy)

1. Push branch:

```bash
git push -u origin feature/adding-jenkins
```

2. Run Jenkins job for `feature/adding-jenkins`
3. Expected:
- Build and tests run
- Push/deploy stages are skipped

## Test B: Main branch full deploy

1. Merge branch into `main`
2. Trigger Jenkins on `main`
3. Expected:
- Images pushed to Docker Hub
- Deployment updates running containers
- Smoke test passes

## Test C: Runtime verification on VM

After main deploy completes:

```bash
docker ps
docker logs poma-backend --tail=100
docker logs poma-frontend --tail=100
```

API check:

```bash
curl -I http://localhost:8080/api/investments
```

## 10. Known Environment Notes

- Some VMs provide `docker-compose` but not `docker compose` plugin form.
- Jenkinsfile handles both automatically during deploy.
- If direct VM port access is restricted by network policy, use SSH tunneling for browser checks.

## 11. Seeder and Jenkins

- Seeder endpoint (`POST /api/dev/seed`) is for development data.
- Do not execute seeder automatically in production deploy pipeline.
- Run seeder manually only when needed in a dev/demo environment.
