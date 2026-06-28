# 🎯 Flakewatch: CI/CD Flaky Test Analytics Engine

![Java 21](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-green.svg)
![Kafka](https://img.shields.io/badge/Apache-Kafka-black.svg)
![Redis](https://img.shields.io/badge/Redis-7-red.svg)

Flakewatch is an event-driven, production-grade microservice (Modular Monolith) designed to detect and quarantine non-deterministic ("flaky") tests in CI/CD pipelines.

## 🛑 The Problem
CI/CD pipelines frequently fail due to tests that pass and fail intermittently without any underlying code changes. This blocks deployments, wastes compute resources, and degrades developer trust in the CI pipeline.

## 💡 The Solution
Flakewatch acts as a webhook receiver for CI pipelines (like GitHub Actions or Jenkins). It ingests massive streams of test results using **Apache Kafka**, uses **Redis** for $O(1)$ state lookups to detect flip-rates (Pass -> Fail on the exact same commit hash), and uses **PostgreSQL** to store historical telemetry. Once a test crosses a "Flake Threshold," it is automatically quarantined so developers can skip it in future CI runs until it is fixed.

---

## 🏗️ Architecture

```mermaid
graph TD
    CI[CI/CD Runner] -->|Webhook: JSON Payloads| API[Ingestion API]
    API -->|Produces| Kafka[[Kafka: test-results-topic]]
    Kafka -->|Consumes| Analytics[Flake Analytics Engine]
    Analytics <-->|O(1) State Check| Redis[(Redis)]
    Analytics -->|Save Telemetry| DB[(PostgreSQL)]
```

### Key Technical Decisions
*   **Why Kafka?** CI pipelines push thousands of test results in a single webhook payload. Processing this synchronously would cause HTTP timeouts. Kafka decouples ingestion from heavy analytics.
*   **Why Redis?** To detect a "flake", the engine must compare the current test status against its *previous* status on the same commit. Querying a relational database thousands of times per second for this check is highly inefficient. Redis caches the `last_known_status` for sub-millisecond validation.
*   **Why a Modular Monolith?** To demonstrate strict domain boundaries (Ingestion, Analytics, Quarantine) without the excessive DevOps overhead of deploying 5 physical microservices for a single bounded context.

---

## 🚀 Running Locally

### Prerequisites
*   Docker & Docker Compose
*   Java 21
*   Maven

### 1. Start Infrastructure
Spin up PostgreSQL, Redis, and Kafka:
```bash
docker compose up -d
```

### 2. Run the Application
The application uses Flyway to automatically apply database schemas on startup.
```bash
mvn spring-boot:run
```

### 3. API Endpoints
*   **Swagger UI:** `http://localhost:8080/swagger-ui.html`
*   **Prometheus Metrics:** `http://localhost:8080/actuator/prometheus`

---

## ☁️ Cloud Deployment (AWS)
This project includes Terraform configurations (`/terraform/main.tf`) for deploying to AWS:
*   **Compute:** Amazon ECS (Fargate)
*   **Database:** Amazon RDS (PostgreSQL)
*   **Cache:** Amazon ElastiCache (Redis)
*   **Streaming:** Amazon MSK (Kafka)

## 🧪 Testing Strategy
The project utilizes **Testcontainers** for true integration testing, spinning up ephemeral Docker containers for PostgreSQL, Kafka, and Redis during the `mvn verify` phase.
