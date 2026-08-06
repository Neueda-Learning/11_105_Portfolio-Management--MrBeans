pipeline {
	agent any

	options {
		timestamps()
		disableConcurrentBuilds()
		buildDiscarder(logRotator(numToKeepStr: '20'))
	}

	environment {
		BACKEND_IMAGE_NAME = 'YOUR_DOCKERHUB_USERNAME/finora-backend'
		FRONTEND_IMAGE_NAME = 'YOUR_DOCKERHUB_USERNAME/finora-frontend'
		DOCKER_BUILDKIT = '1'
		COMPOSE_DOCKER_CLI_BUILD = '1'
	}

	stages {
		stage('Checkout') {
			steps {
				checkout scm
			}
		}

		stage('Backend Build & Test') {
			steps {
				sh '''
					docker run --rm \
						-v "$WORKSPACE/backend:/app" \
						-v /var/run/docker.sock:/var/run/docker.sock \
						-w /app \
						maven:3.9-eclipse-temurin-21 \
						mvn -B clean verify
				'''
			}
		}

		stage('Frontend Build & Test') {
			steps {
				sh '''
					docker run --rm \
						-v "$WORKSPACE/frontend:/app" \
						-w /app \
						node:22.22.2-alpine \
						sh -lc "npm ci && npm run build && npm run test"
				'''
			}
		}

		stage('Build Docker Images') {
			steps {
				script {
					env.SHORT_SHA = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim()
					env.BACKEND_IMAGE_SHA = "${BACKEND_IMAGE_NAME}:${SHORT_SHA}"
					env.FRONTEND_IMAGE_SHA = "${FRONTEND_IMAGE_NAME}:${SHORT_SHA}"
					env.BACKEND_IMAGE_LATEST = "${BACKEND_IMAGE_NAME}:latest"
					env.FRONTEND_IMAGE_LATEST = "${FRONTEND_IMAGE_NAME}:latest"
				}

				sh 'docker build -t ${BACKEND_IMAGE_SHA} -t ${BACKEND_IMAGE_LATEST} ./backend'
				sh 'docker build -t ${FRONTEND_IMAGE_SHA} -t ${FRONTEND_IMAGE_LATEST} ./frontend'
			}
		}

		stage('Push Docker Images') {
			when {
				branch 'main'
			}
			steps {
				withCredentials([
					usernamePassword(
						credentialsId: 'dockerhub-credentials',
						usernameVariable: 'DOCKERHUB_USERNAME',
						passwordVariable: 'DOCKERHUB_TOKEN'
					)
				]) {
					sh 'echo "${DOCKERHUB_TOKEN}" | docker login -u "${DOCKERHUB_USERNAME}" --password-stdin'
					sh 'docker push ${BACKEND_IMAGE_SHA}'
					sh 'docker push ${FRONTEND_IMAGE_SHA}'
					sh 'docker push ${BACKEND_IMAGE_LATEST}'
					sh 'docker push ${FRONTEND_IMAGE_LATEST}'
					sh 'docker logout || true'
				}
			}
		}

		stage('Deploy To VM') {
			when {
				branch 'main'
			}
			steps {
				withCredentials([
					string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')
				]) {
					sh '''
						cat > .env <<EOF
DB_USER=root
DB_PASSWORD=root
DB_ROOT_PASSWORD=root
CHATBOT_PROVIDER=groq
GROQ_API_KEY=${GROQ_API_KEY}
GROQ_MODEL=llama-3.3-70b-versatile
GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.0-flash
BACKEND_IMAGE=${BACKEND_IMAGE_SHA}
FRONTEND_IMAGE=${FRONTEND_IMAGE_SHA}
EOF
					'''

					sh 'docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml pull backend frontend mysql'
					sh 'docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up -d --remove-orphans'
				}
			}
		}
	}

	post {
		always {
			sh 'docker image prune -f || true'
			deleteDir()
		}
	}
}
