# 🪺 CareerNest - Employers & Job Seeker Platform API

A scalable, production-ready Job Portal Backend RESTful API built with **Java 21 (Spring Boot 4)** and **MongoDB Atlas**, featuring JWT stateless authentication with **Refresh Token Rotation**, Role-Based Access Control (`ROLE_EMPLOYER` & `ROLE_JOB_SEEKER`), paginated job catalog search, candidate application lifecycle, Twilio Cloud SMS notifications, 100% automated test coverage, and automated GitHub Actions CI/CD.

---

## 🌐 Live Deployment & Interactive API Docs

| Resource | URL | Status |
| :--- | :--- | :--- |
| **Live API Base URL** | `https://careernest-employers-jobseeker-project.onrender.com` | `🟢 Live (Render Cloud)` |
| **Interactive Swagger UI** | `https://careernest-employers-jobseeker-project.onrender.com/swagger-ui/index.html` | `📘 Active (OpenAPI 3.0)` |
| **OpenAPI 3 JSON Spec** | `https://careernest-employers-jobseeker-project.onrender.com/v3/api-docs` | `📄 Specification` |
| **Cloud Health Probe** | `https://careernest-employers-jobseeker-project.onrender.com/actuator/health` | `🟢 UP (MongoDB Connected)` |
| **Local Base URL** | `http://localhost:8081` | Local Development |

---

## 📑 Table of Contents
1. [Key Production Features](#-key-production-features)
2. [Tech Stack & Architecture](#-tech-stack--architecture)
3. [Database Design & Indexing Optimization](#-database-design--indexing-optimization)
4. [Security & Refresh Token Rotation](#-security--refresh-token-rotation)
5. [Automated Testing Suite (100% Green)](#-automated-testing-suite)
6. [CI/CD Pipeline (GitHub Actions)](#-cicd-pipeline-github-actions)
7. [Environment Variables Matrix](#-environment-variables-matrix)
8. [API Endpoints Reference & Samples](#-api-endpoints-reference)
9. [Local Setup & Docker Execution](#-local-setup--running-locally)

---

## 🚀 Key Production Features

* **Stateless JWT Security with Refresh Token Rotation**:
  * Short-lived Access Tokens (15 minutes) for strict security boundaries.
  * Long-lived Refresh Tokens (7 days) with cryptographic UUIDs stored in MongoDB.
  * **Automatic Token Rotation**: Old refresh tokens are invalidated upon use to prevent token replay attacks.
* **Role-Based Access Control (RBAC)**:
  * `ROLE_EMPLOYER`: Post jobs, edit/delete job postings, review applicants, and update status.
  * `ROLE_JOB_SEEKER`: Browse job catalog, search by title/location, submit applications, view "My Applications".
* **Standardized Global Exception Handling**:
  * Unified `@RestControllerAdvice` returning RFC-compliant `ErrorResponseDTO` structures (`status`, `error`, `message`, `timestamp`, `validationErrors`).
  * Custom domain exceptions: `ResourceNotFoundException` (404), `UserAlreadyExistsException` (409), `UnauthorizedActionException` (403), `DuplicateApplicationException` (409).
* **High-Performance MongoDB Indexing**:
  * Compound index `status_location_idx` on `(status, location)` for sub-millisecond job searches.
  * Compound unique index on `(jobId, seekerId)` strictly preventing duplicate submissions.
* **Dual-Mode Twilio Cloud SMS Notification Engine**:
  * Automated SMS dispatch to candidate phones upon status updates (`ACCEPTED`, `UNDER_REVIEW`).
  * Built-in console fallback simulator ensuring business logic never crashes if credentials are omitted.
* **Automated CI/CD**:
  * Multi-stage Docker container build on Render.
  * GitHub Actions running unit & integration tests on an ephemeral MongoDB test service container.

---

## 🏗️ Tech Stack & Architecture

```mermaid
flowchart TD
    Client["Client (Browser / React Frontend / Postman)"]
    
    subgraph SpringBootBackend["CareerNest Spring Boot 4 Application"]
        CORS["CORS & Security Filter Chain"]
        JWTFilter["JwtAuthenticationFilter"]
        
        subgraph Controllers["REST Controllers"]
            AuthController["AuthController\n(/api/auth/**)"]
            JobController["JobController\n(/api/jobs/**)"]
            AppController["ApplicationController\n(/api/applications/**)"]
            Actuator["Actuator Health\n(/actuator/health)"]
        end
        
        subgraph Services["Service Layer & Business Rules"]
            AuthService["AuthService\n+ RefreshTokenService"]
            JobService["JobService"]
            AppService["ApplicationService"]
            TwilioService["TwilioService (Dual-Mode)"]
        end
        
        ExceptionHandler["GlobalExceptionHandler\n(@RestControllerAdvice)"]
    end
    
    subgraph Storage["Cloud Infrastructure"]
        MongoAtlas[("MongoDB Atlas Database\n(users, jobs, applications, refresh_tokens)")]
        TwilioCloud["Twilio Cloud SMS Gateway"]
    end
    
    Client --> CORS
    CORS --> JWTFilter
    JWTFilter --> Controllers
    Controllers --> Services
    Services --> MongoAtlas
    Services --> TwilioCloud
    Controllers -.-> ExceptionHandler
```

| Component | Technology | Version / Specification |
| :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | Java 21 (LTS) |
| **Framework** | Spring Boot | 4.1.1 / Spring Framework 7 |
| **Database** | MongoDB Atlas | Cloud NoSQL Database |
| **Security** | Spring Security + JJWT | JJWT `0.12.6` (HMAC-SHA256) |
| **API Docs** | Springdoc OpenAPI 3 | Swagger UI `2.8.5` |
| **Cloud Hosting** | Render | Docker Containerized Web Service |
| **Cloud SMS** | Twilio SDK | Twilio `10.6.0` (with Simulator Fallback) |
| **CI/CD** | GitHub Actions | Ubuntu Latest + MongoDB Container |

---

## 📊 Database Design & Indexing Optimization

### Collections & Index Strategy

```mermaid
erDiagram
    USER ||--o{ JOB : posts
    USER ||--o{ APPLICATION : submits
    USER ||--o{ REFRESH_TOKEN : owns
    JOB ||--o{ APPLICATION : receives

    USER {
        string id PK
        string email UK "Indexed, Unique"
        string password "BCrypt Hash"
        string fullName
        string phoneNumber "E.164 Indexed"
        string role "EMPLOYER | JOB_SEEKER"
        boolean isActive
        datetime createdAt
    }

    JOB {
        string id PK
        string employerId FK "Indexed"
        string title "Indexed"
        string location "Indexed"
        string salary
        date deadline
        string status "Indexed (OPEN | CLOSED)"
        datetime createdAt "Indexed (Sort)"
    }

    APPLICATION {
        string id PK
        string jobId FK "Compound Index (jobId, seekerId) UNIQUE"
        string seekerId FK "Compound Index (jobId, seekerId) UNIQUE"
        string seekerName
        string seekerEmail
        string seekerPhone
        string resumeUrl
        string status "Indexed (APPLIED | UNDER_REVIEW | ACCEPTED | REJECTED)"
        datetime appliedAt
    }

    REFRESH_TOKEN {
        string id PK
        string token UK "UUID Unique Index"
        string userId FK "Indexed"
        instant expiryDate "TTL Index"
    }
```

* **`status_location_idx`** on `jobs`: Optimizes compound seeker queries (`status = 'OPEN' AND location = 'Bangalore'`).
* **`job_seeker_unique_idx`** on `applications`: Prevents accidental duplicate submissions at the database engine level.

---

## 🛡️ Security & Refresh Token Rotation

### Token Rotation Flow:
```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend / Postman
    participant Auth as AuthController
    participant RTS as RefreshTokenService
    participant DB as MongoDB Atlas

    Client->>Auth: POST /api/auth/login (email, password)
    Auth->>DB: Validate credentials & Fetch User
    Auth->>RTS: Generate Access Token (15m) & Refresh Token (7d)
    RTS->>DB: Store RefreshToken UUID
    Auth-->>Client: Returns { token, refreshToken, role, ... }

    Note over Client, Auth: After 15 minutes (Access Token expires)

    Client->>Auth: POST /api/auth/refresh { refreshToken }
    Auth->>RTS: Verify & Invalidate used token
    RTS->>DB: Delete old RefreshToken (Rotation Defense)
    RTS->>DB: Save brand new RefreshToken UUID
    RTS-->>Client: Returns { accessToken, refreshToken, type: "Bearer" }

    Note over Client, Auth: If attacker tries reusing old token -> 401/404 Rejected!
```

---

## 🧪 Automated Testing Suite

All tests execute with **100% success rate (0 errors, 0 failures)** via MockMvc and Mockito:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.guvi.careernestbackend.Service.JobServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.25 s
[INFO] Running com.guvi.careernestbackend.Controller.AuthControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.42 s
[INFO] Running com.guvi.careernestbackend.CareernestBackendApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.10 s
[INFO] 
[INFO] Results:
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Test Coverage Highlights:
1. **Validation Rejection (`400 Bad Request`)**: Verifies that short passwords, missing roles, and invalid phone numbers return field-specific error maps.
2. **Duplicate Conflict (`409 Conflict`)**: Verifies duplicate user emails trigger `UserAlreadyExistsException`.
3. **Authentication Contract (`200 OK`)**: Verifies login generates both JWT access token and refresh token.
4. **Token Rotation (`200 OK`)**: Verifies `/api/auth/refresh` rotates tokens.
5. **Authorization Security (`403 Forbidden`)**: Verifies job seekers cannot post jobs.
6. **Entity Lookup (`404 Not Found`)**: Verifies non-existent employers or jobs throw `ResourceNotFoundException`.

---

## ⚙️ CI/CD Pipeline (GitHub Actions)

Located at `.github/workflows/ci.yml`. On every `push` and `pull_request` to `main`:

1. **Environment Setup**: Provisions Ubuntu runner with JDK 21 Temurin.
2. **Database Provisioning**: Spins up an ephemeral, healthy `mongo:7.0` container on port 27017.
3. **Automated Verification**: Executes `mvn clean test`.
4. **Artifact Packaging**: Builds `careernest-backend-0.0.1-SNAPSHOT.jar` and uploads build artifacts.

---

## 🔐 Environment Variables Matrix

| Variable | Description | Sample Production Value |
| :--- | :--- | :--- |
| `PORT` | Web server port (Render injects automatically) | `8081` / `10000` |
| `MONGODB_URI` | MongoDB Atlas Connection String | `mongodb+srv://user:pass@cluster0.xxx.mongodb.net/careernestDb` |
| `JWT_SECRET` | 256-bit Cryptographic HMAC Key | `mySecretKeyForTestingCareerNestApplication2026SecureKey123` |
| `JWT_EXPIRATION` | Access Token Lifetime (ms) | `900000` *(15 minutes)* |
| `JWT_REFRESH_EXPIRATION` | Refresh Token Lifetime (ms) | `604800000` *(7 days)* |
| `TWILIO_ACCOUNT_SID` | Twilio Cloud Account SID | `AC3f22e...` *(or dummy_sid for simulator)* |
| `TWILIO_AUTH_TOKEN` | Twilio Cloud Auth Token | `9d163cf...` *(or dummy_token)* |
| `TWILIO_PHONE_NUMBER` | Twilio Outbound Virtual Number | `+1234567890` |
| `CORS_ALLOWED_ORIGINS` | Permitted Frontend Origins | `http://localhost:5173,https://*.vercel.app` |

---

## 📡 API Endpoints Reference

### 1. Authentication Endpoints

#### Register User
* **Method**: `POST`
* **URL**: `/api/auth/register`
* **Payload**:
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "securePassword123",
  "phoneNumber": "+919876543210",
  "role": "JOB_SEEKER"
}
```
* **Response (`200 OK`)**: `"User registered successfully as JOB_SEEKER"`

#### Login User
* **Method**: `POST`
* **URL**: `/api/auth/login`
* **Payload**:
```json
{
  "email": "jane@example.com",
  "password": "securePassword123"
}
```
* **Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "4d13e2bb-85f2-4919-9ce7-76cf6b14dcb3",
  "type": "Bearer",
  "email": "jane@example.com",
  "role": "JOB_SEEKER",
  "id": "6ac8df5044b89787f09b945e",
  "fullName": "Jane Doe"
}
```

#### Rotate Refresh Token
* **Method**: `POST`
* **URL**: `/api/auth/refresh`
* **Payload**:
```json
{
  "refreshToken": "4d13e2bb-85f2-4919-9ce7-76cf6b14dcb3"
}
```
* **Response (`200 OK`)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "8f31b2bb-12a4-4731-8ca2-55cf6b14dae1",
  "type": "Bearer"
}
```

---

### 2. Job Catalog Endpoints

| Endpoint | Method | Role | Description |
| :--- | :---: | :---: | :--- |
| `/api/jobs` | `GET` | Public | Paginated list of all open jobs (`?page=0&size=10&sortBy=createdAt&sortDir=desc`) |
| `/api/jobs/{id}` | `GET` | Public | Retrieve single job by ID |
| `/api/jobs/search?title={title}` | `GET` | Public | Substring title search (case-insensitive) |
| `/api/jobs/location/{location}` | `GET` | Public | Location filter |
| `/api/jobs/employer/{employerId}`| `GET` | Public | All postings by an employer |
| `/api/jobs` | `POST` | `ROLE_EMPLOYER` | Create job posting |
| `/api/jobs/{id}` | `PUT` | `ROLE_EMPLOYER` | Update job posting |
| `/api/jobs/{id}` | `DELETE` | `ROLE_EMPLOYER` | Delete job posting |

---

### 3. Application Lifecycle Endpoints

| Endpoint | Method | Role | Description |
| :--- | :---: | :---: | :--- |
| `/api/applications/apply/{jobId}` | `POST` | `ROLE_JOB_SEEKER` | Apply for job with resume link & cover note |
| `/api/applications/my-applications/{seekerId}` | `GET` | `ROLE_JOB_SEEKER` | Candidate dashboard: all applied jobs & statuses |
| `/api/applications/job/{jobId}` | `GET` | `ROLE_EMPLOYER` | Employer dashboard: all applicants for posting |
| `/api/applications/{id}/status` | `PATCH` | `ROLE_EMPLOYER` | Update status (`ACCEPTED`, `REJECTED`) & dispatch SMS |

---

## 💻 Local Setup & Running Locally

### Prerequisites
* JDK 21 (Temurin / Oracle)
* Maven 3.9+
* Local MongoDB running on `localhost:27017` (or MongoDB Atlas account)

### Steps:
```powershell
# 1. Clone repository
git clone https://github.com/sivaprabu/CareerNest_Employers_JobSeeker_Project.git
cd CareerNest_Employers_JobSeeker_Project/careernest-backend

# 2. Run automated tests
mvn clean test

# 3. Start local development server
mvn spring-boot:run
```

The application will start on `http://localhost:8081`. Access the local Swagger UI at:
👉 `http://localhost:8081/swagger-ui/index.html`
