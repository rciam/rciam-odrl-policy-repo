# ODRL Policy Repository API

![Java](https://img.shields.io/badge/Java-17-orange)
![Quarkus](https://img.shields.io/badge/Quarkus-3.8-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)

**Reference Implementation of the [ODRL Policy Repository API Specification](https://github.com/RI-SCALE/odrl-policy-repository-api).**

A cloud-native API for storing, validating, and searching **ODRL (Open Digital Rights Language)** policies. Built with **Quarkus** and **PostgreSQL**, designed for high performance and deep JSON inspection.

## 🚀 Features

* **ODRL Compliance:** Stores policies as native JSON-LD documents.
* **Deep Search:** Uses PostgreSQL native `jsonb` operators to search for policies based on nested fields (e.g., `target`, `assignee`, `assigner`) without flattening the data.
* **Validation:** Built-in structural validation for ODRL integrity (checks for UIDs, Permissions/Prohibitions).
* **Security:** Ready for OpenID Connect (OIDC) integration.
* **Reactive Core:** Built on the Quarkus Reactive stack for low footprint and high throughput.

## 🛠️ Tech Stack

* **Language:** Java 17
* **Framework:** Quarkus (RESTEasy Reactive, Hibernate ORM with Panache)
* **Database:** PostgreSQL 12+ (Uses `jsonb` column types)
* **Testing:** JUnit 5, REST Assured, Testcontainers

## 📋 Prerequisites

* Docker & Docker Compose (for the database)
* Java 17+
* Maven 3.9+

## ⚡ Quick Start

### 1. Start the Database
The project includes a `docker-compose.yaml` to spin up a pre-configured PostgreSQL instance.

```bash
docker-compose up -d
```

### 2. Run the Application
Start the application in Quarkus Dev Mode (supports live coding/reload):

```bash
mvn quarkus:dev
```

### 3. Access the API
* **Swagger UI:** [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)
* **OpenAPI Spec:** [http://localhost:8080/q/openapi](http://localhost:8080/q/openapi)

## 🧪 Testing

The project uses **Testcontainers** (via Quarkus Dev Services) or the local Docker service to run integration tests against a real PostgreSQL instance. This ensures that the native JSON queries are tested correctly.

```bash
mvn test
```

## 🔌 Configuration

The application is configured via `src/main/resources/application.properties`.

| Property | Default | Description |
| :--- | :--- | :--- |
| `quarkus.datasource.jdbc.url` | `jdbc:postgresql://localhost:5432/odrl_db` | Database connection URL |
| `quarkus.datasource.username` | `odrl` | DB Username |
| `quarkus.datasource.password` | `odrl` | DB Password |
| `quarkus.oidc.auth-server-url` | `https://auth.example.org...` | OIDC Issuer URL (Mocked in Dev) |

## 🔎 API Usage Examples

### 1. Create a Policy
**POST** `/policies`

```json
{
  "name": "Climate Data Access",
  "policyType": "set",
  "status": "published",
  "odrlPolicy": {
    "@context": "http://www.w3.org/ns/odrl.jsonld",
    "uid": "urn:uuid:3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "permission": [{
      "target": "urn:dataset:climate:2024",
      "action": "use",
      "assignee": "urn:org:research-group-A"
    }]
  }
}
```

### 2. Search (Deep Filtering)
Find all policies where the `target` is a specific dataset, even though the field is nested inside the JSON structure:

**GET** `/policies?target=urn:dataset:climate:2024`

### 3. Validate a Policy
**POST** `/policies/{id}/validate`
*Returns `200 OK` with validation details if the policy structure adheres to the ODRL model.*

## 📦 CI/CD

This repository includes a **GitHub Actions** workflow (`.github/workflows/maven.yml`) that:
1.  Sets up Java 17.
2.  Spins up a PostgreSQL Service Container.
3.  Runs `mvn verify` to ensure all tests pass before merging.

## 📄 License

This project is licensed under the Apache 2.0 License.
