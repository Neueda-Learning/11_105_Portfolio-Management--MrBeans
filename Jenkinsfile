pipeline {
	agent any

	options {
		timestamps()
		disableConcurrentBuilds()
		buildDiscarder(logRotator(numToKeepStr: '20'))
	}

	environment {
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
						--user "$(id -u):$(id -g)" \
						--group-add "$(stat -c '%g' /var/run/docker.sock)" \
						-e HOME=/tmp \
						-e MAVEN_CONFIG=/tmp/.m2 \
						-v "$WORKSPACE/backend:/app" \
						-v "$WORKSPACE/.m2:/tmp/.m2" \
						-v /var/run/docker.sock:/var/run/docker.sock \
						-w /app \
						maven:3.9-eclipse-temurin-21 \
						sh -lc "mkdir -p /tmp/.m2 && mvn -B clean verify"
				'''
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
				}
			}
		}

		stage('Frontend Build & Test') {
			steps {
				sh '''
					docker run --rm \
						--user "$(id -u):$(id -g)" \
						-v "$WORKSPACE/frontend:/app" \
						-w /app \
						node:22.22.2-alpine \
						sh -lc "npm ci && npm run build && npm run test"
				'''
			}
		}

		stage('Build Docker Images') {
			steps {
				sh '''
					if command -v docker-compose >/dev/null 2>&1; then
						DC="docker-compose"
					else
						DC="docker compose"
					fi

					$DC build
				'''
			}
		}

		stage('Deploy To VM') {
			when {
				branch 'main'
			}
			steps {
				sh '''
					if command -v docker-compose >/dev/null 2>&1; then
						DC="docker-compose"
					else
						DC="docker compose"
					fi

					$DC up -d --build --remove-orphans
				'''
			}
		}

		stage('Post-Deploy Smoke Test') {
			when {
				branch 'main'
			}
			steps {
				sh 'curl -fsS http://localhost:8080/api/investments > /dev/null'
			}
		}
	}

	post {
		success {
			echo 'Pipeline completed successfully.'
		}
		failure {
			echo 'Pipeline failed. Check stage logs for details.'
		}
		always {
			sh 'docker image prune -f || true'
			cleanWs deleteDirs: true, notFailBuild: true
		}
	}
}
