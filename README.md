# 🪺 CareerNest - Enterprise Full-Stack Job Portal Platform

An enterprise full-stack Job Portal platform connecting employers and job seekers with a reactive modern UI, powered by **Java 21 (Spring Boot 4)**, **MongoDB Atlas**, **Twilio Cloud SMS**, and containerized with **Docker & Docker Compose**.

---

## 🌐 Live Cloud Deployment & Interactive API Docs

| Service | Access URL | Status |
| :--- | :--- | :--- |
| **Live Backend API** | `https://careernest-employers-jobseeker-project.onrender.com` | 🟢 Live (Render Cloud) |
| **Interactive Swagger UI** | `https://careernest-employers-jobseeker-project.onrender.com/swagger-ui/index.html` | 📘 Active (OpenAPI 3.0) |
| **OpenAPI 3 JSON Spec** | `https://careernest-employers-jobseeker-project.onrender.com/v3/api-docs` | 📄 Raw JSON Specification |
| **Cloud Health Probe** | `https://careernest-employers-jobseeker-project.onrender.com/actuator/health` | 🟢 UP (MongoDB Atlas Connected) |
| **GitHub Repository** | `https://github.com/SIVAPRASANNA2812/CareerNest---Employers-and-Job-Seeker-Project` | 🐙 Public Repo |

---

## 📑 Table of Contents
1. [Core Features](#-core-features)
2. [Full-Stack Architecture](#-full-stack-architecture)
3. [Project Directory Structure](#-project-directory-structure)
4. [Database Design & Index Optimization](#-database-design--index-optimization)
5. [Security & Refresh Token Rotation](#-security--refresh-token-rotation)
6. [Twilio Cloud SMS Notifications](#-twilio-cloud-sms-notifications)
7. [Automated Test Suite (100% Green)](#-automated-test-suite)
8. [How to Run CareerNest (3 Options)](#-how-to-run-careernest)
9. [Complete API Endpoints Reference](#-complete-api-endpoints-reference)
10. [Environment Variables Matrix](#-environment-variables-matrix)

---

## 🚀 Core Features

### 👤 Job Seeker Experience
* **Live Job Catalog**: Real-time paginated job search with instant keyword and location filtering.
* **Streamlined Easy Apply**: 1-click modal application submission with resume URL and cover letter.
* **Candidate Tracker**: Dedicated "My Applications" dashboard tracking status changes (`APPLIED`, `UNDER_REVIEW`, `ACCEPTED`, `REJECTED`).
* **SMS Notifications**: Automated SMS updates dispatched directly to the candidate's phone upon status changes.

### 🏢 Employer Experience
* **KPI Metrics Dashboard**: Instant overview of total active jobs, received applications, and hiring funnel status.
* **Job Management**: Complete CRUD operations for posting, updating, and removing job listings.
* **Applicant Review Board**: Dedicated candidate review interface with full applicant details, contact numbers, and resume links.
* **Real-time Status Updates**: Quick status dropdown updating applicant status and immediately triggering SMS alerts.

### 🛡️ Enterprise Engineering Highlights
* **Stateless JWT with Token Rotation**: 15-minute access tokens + 7-day single-use refresh tokens stored in MongoDB Atlas.
* **Zero-Build Vanilla JS Frontend**: Clean, modular CSS/JS architecture requiring zero Node.js/npm dependencies to run.
* **Multi-Stage Docker & Compose**: Orchestrated frontend (Nginx Alpine) and backend (Temurin JDK 21) with one command.
* **Dual-Mode Twilio Engine**: Graceful fallback to console simulator when Twilio credentials are not provided.
* **High-Performance MongoDB Indexing**: Compound indexes for sub-millisecond filtering and duplicate application prevention.

---

## 🏗️ Full-Stack Architecture

```mermaid
flowchart TD
    subgraph ClientTier["Frontend Tier (Port 3000)"]
        UI["Modern Vanilla JS Client\n(HTML5 / CSS3 / ES6 Modules)"]
        Nginx["Nginx Alpine Web Server"]
        UI --- Nginx
    end

    subgraph BackendTier["Spring Boot 4 Backend Tier (Port 8081)"]
        CORS["CORS Filter & Security Chain"]
        JWT["JwtAuthenticationFilter"]

        subgraph Controllers["REST Controllers"]
            AuthCtrl["AuthController\n(/api/auth/**)"]
            JobCtrl["JobController\n(/api/jobs/**)"]
            AppCtrl["ApplicationController\n(/api/applications/**)"]
            ActuatorCtrl["Actuator Health\n(/actuator/health)"]
        end

        subgraph Services["Domain Service Layer"]
            AuthSvc["AuthService + RefreshTokenService"]
            JobSvc["JobService"]
            AppSvc["ApplicationService"]
            TwilioSvc["TwilioService (Dual-Mode)"]
        end

        ExHandler["GlobalExceptionHandler\n(@RestControllerAdvice)"]
    end

    subgraph CloudTier["Cloud Infrastructure"]
        Atlas[("MongoDB Atlas Cloud\n(users, jobs, applications, tokens)")]
        TwilioGateway["Twilio Cloud SMS Gateway"]
    end

    UI -->|"HTTP / REST API (Bearer JWT)"| CORS
    CORS --> JWT
    JWT --> Controllers
    Controllers --> Services
    Services --> Atlas
    Services --> TwilioGateway
    Controllers -.-> ExHandler
```

---

## 📁 Project Directory Structure

```text
CareerNest_Employers_JobSeeker_Project/
├── careernest-backend/                 # Spring Boot 4 REST API
│   ├── src/
│   │   ├── main/java/com/guvi/careernestbackend/
│   │   │   ├── Config/                 # SecurityConfig, CorsConfig, SwaggerConfig
│   │   │   ├── Controller/             # AuthController, JobController, ApplicationController
│   │   │   ├── DTO/                    # Request & Response DTOs
│   │   │   ├── Entity/                 # User, Job, Application, RefreshToken
│   │   │   ├── Exception/              # GlobalExceptionHandler & Domain Exceptions
│   │   │   ├── Repository/             # Spring Data Mongo Repositories
│   │   │   ├── Security/               # JwtService, JwtFilter, CustomUserDetailsService
│   │   │   └── Service/                # AuthService, JobService, ApplicationService, TwilioService
│   │   └── test/java/com/guvi/careernestbackend/ # 100% Automated Test Suite
│   ├── pom.xml                         # Maven Dependencies (Java 21, Spring Boot 4.1.1)
│   └── Dockerfile                      # Backend Multi-Stage JDK 21 Dockerfile
├── careernest-frontend/                # Modern Decoupled Frontend
│   ├── index.html                      # Seeker Job Discovery & Tracking Portal
│   ├── employer-dashboard.html         # Employer KPI & Applicant Review Board
│   ├── auth.html                       # Universal Login & Register Portal
│   ├── style.css                       # Modern Design System (Vars, Grid, Modals, Badges)
│   ├── api.js                          # Centralized Fetch Client & Token Management
│   ├── app.js                          # Job Seeker Catalog & Application Logic
│   ├── dashboard.js                    # Employer KPI, Job Post & Review Logic
│   ├── auth.js                         # Login, Register & Session State
│   └── Dockerfile                      # Frontend Nginx Alpine Dockerfile
├── .github/workflows/
│   └── ci.yml                          # GitHub Actions Automated CI Pipeline
├── docker-compose.yml                  # Full-Stack Orchestration (Frontend + Backend)
└── README.md                           # Complete Documentation
```

---

## 📊 Database Design & Index Optimization

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
        string phoneNumber "E.164 Standard"
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
        string jobId FK "Compound Unique Index"
        string seekerId FK "Compound Unique Index"
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

### Strategic MongoDB Indexes:
1. **`status_location_idx`** on `jobs`: Optimizes compound seeker catalog queries (`status = 'OPEN' AND location = 'Bangalore'`).
2. **`job_seeker_unique_idx`** on `applications`: Compound unique index on `(jobId, seekerId)` enforcing single submissions at the database level.
3. **`email_idx`** on `users`: Enforces unique account credentials.
4. **`expiryDate_ttl`** on `refresh_tokens`: Automatically removes expired tokens.

---

## 🛡️ Security & Refresh Token Rotation

CareerNest implements stateless authentication with **Single-Use Refresh Token Rotation**:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend / User
    participant Auth as AuthController
    participant RTS as RefreshTokenService
    participant DB as MongoDB Atlas

    Client->>Auth: POST /api/auth/login (email, password)
    Auth->>DB: Validate credentials & retrieve user
    Auth->>RTS: Generate Access Token (15m) & Refresh Token (7d)
    RTS->>DB: Save RefreshToken UUID
    Auth-->>Client: Returns { token, refreshToken, role, id, fullName }

    Note over Client, Auth: Access Token expires after 15 minutes

    Client->>Auth: POST /api/auth/refresh { refreshToken }
    Auth->>RTS: Validate and invalidate used token
    RTS->>DB: Delete old RefreshToken (Rotation Defense)
    RTS->>DB: Persist newly generated RefreshToken
    RTS-->>Client: Returns { accessToken, refreshToken, type: "Bearer" }

    Note over Client, Auth: Replay attack with expired/used token is immediately rejected (401/404)
```

---

## 📱 Twilio Cloud SMS Notifications

When an employer updates a candidate application status to `UNDER_REVIEW`, `ACCEPTED`, or `REJECTED`:
1. `ApplicationService` triggers `TwilioService.sendApplicationStatusSms(...)`.
2. **Production Mode**: If `TWILIO_ACCOUNT_SID` and `TWILIO_AUTH_TOKEN` are configured, a real SMS is dispatched to the candidate's phone number.
3. **Simulator Mode**: If credentials are unset or placeholders, the service falls back to a clean console log simulator without throwing exceptions or blocking the transaction.

---

## 🧪 Automated Test Suite

CareerNest features a comprehensive **10/10 automated test suite** with 100% pass rate:

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

---

## 💻 How to Run CareerNest

### Option A: Full-Stack Docker Compose (Recommended)
Make sure Docker Desktop is running on your machine:
```bash
# Clone the repository
git clone https://github.com/SIVAPRASANNA2812/CareerNest---Employers-and-Job-Seeker-Project.git
cd CareerNest---Employers-and-Job-Seeker-Project

# Build and launch both Frontend & Backend
docker compose up --build
```
* **Frontend Portal**: Open `http://localhost:3000` in your browser.
* **Backend REST API**: Running on `http://localhost:8081`.
* **Swagger API UI**: Open `http://localhost:8081/swagger-ui/index.html`.

---

### Option B: Local Development (Without Docker)

#### Step 1: Start the Spring Boot Backend
```bash
cd careernest-backend
mvn clean spring-boot:run
```
The backend starts on `http://localhost:8081`.

#### Step 2: Open the Frontend
Since the frontend uses pure Vanilla JS and ES6 modules, open `careernest-frontend/index.html` directly in your browser, or serve it using any lightweight server:
```bash
cd careernest-frontend
# Using Python
python -m http.server 3000
# Or using Node.js npx
npx serve -l 3000
```
Open `http://localhost:3000` or double-click `careernest-frontend/index.html`.

---

### Option C: Connect Local Frontend to Live Render Cloud Backend
If you want to run only the frontend and connect to the live cloud backend without running Java or MongoDB locally:
1. Open `careernest-frontend/api.js`.
2. Update the `API_BASE_URL`:
   ```javascript
   const API_BASE_URL = 'https://careernest-employers-jobseeker-project.onrender.com/api';
   ```
3. Open `index.html` in your browser. All data will sync with the live cloud database.

---

## 📡 Complete API Endpoints Reference

### 1. Authentication (`/api/auth`)

| Endpoint | Method | Role | Description |
| :--- | :---: | :---: | :--- |
| `/api/auth/register` | `POST` | Public | Register new `JOB_SEEKER` or `EMPLOYER` |
| `/api/auth/login` | `POST` | Public | Authenticate user, receive JWT & Refresh Token |
| `/api/auth/refresh` | `POST` | Public | Exchange valid Refresh Token for fresh Access Token |

#### Sample Login Payload & Response:
```json
// POST /api/auth/login
{
  "email": "employer@techcorp.com",
  "password": "Password123"
}

// Response (200 OK)
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "4d13e2bb-85f2-4919-9ce7-76cf6b14dcb3",
  "type": "Bearer",
  "email": "employer@techcorp.com",
  "role": "EMPLOYER",
  "id": "6ac8df5044b89787f09b945e",
  "fullName": "TechCorp Recruiter"
}
```

---

### 2. Job Management (`/api/jobs`)

| Endpoint | Method | Role | Description |
| :--- | :---: | :---: | :--- |
| `/api/jobs` | `GET` | Public | Paginated open jobs (`?page=0&size=10&sortBy=createdAt&sortDir=desc`) |
| `/api/jobs/{id}` | `GET` | Public | Get single job details by ID |
| `/api/jobs/search?title={query}` | `GET` | Public | Case-insensitive title keyword search |
| `/api/jobs/location/{location}` | `GET` | Public | Filter jobs by location |
| `/api/jobs/employer/{employerId}`| `GET` | Public | All jobs posted by a specific employer |
| `/api/jobs` | `POST` | `ROLE_EMPLOYER` | Post a new job opportunity |
| `/api/jobs/{id}` | `PUT` | `ROLE_EMPLOYER` | Update existing job details |
| `/api/jobs/{id}` | `DELETE` | `ROLE_EMPLOYER` | Delete job posting |

---

### 3. Applications Lifecycle (`/api/applications`)

| Endpoint | Method | Role | Description |
| :--- | :---: | :---: | :--- |
| `/api/applications/apply/{jobId}` | `POST` | `ROLE_JOB_SEEKER` | Submit job application with resume & cover letter |
| `/api/applications/my-applications/{seekerId}` | `GET` | `ROLE_JOB_SEEKER` | Retrieve candidate's active application history |
| `/api/applications/job/{jobId}` | `GET` | `ROLE_EMPLOYER` | Retrieve all applicants for an employer's job posting |
| `/api/applications/{id}/status` | `PATCH` | `ROLE_EMPLOYER` | Update status (`ACCEPTED`, `REJECTED`, etc.) & send SMS |

---

## 🔐 Environment Variables Matrix

| Variable | Description | Sample Local Value | Sample Production Value |
| :--- | :--- | :--- | :--- |
| `PORT` | Web Server Port | `8081` | `10000` (Render default) |
| `MONGODB_URI` | MongoDB Connection URI | `mongodb://localhost:27017/careernestDb` | `mongodb+srv://user:pass@cluster.mongodb.net/careernestDb` |
| `JWT_SECRET` | 256-bit Cryptographic Key | `mySecretKeyForTestingCareerNestApplication2026SecureKey123` | Secure Random 256-bit Secret |
| `JWT_EXPIRATION` | Access Token Expiry (ms) | `900000` *(15 min)* | `900000` |
| `JWT_REFRESH_EXPIRATION` | Refresh Token Expiry (ms) | `604800000` *(7 days)* | `604800000` |
| `TWILIO_ACCOUNT_SID` | Twilio Cloud Account SID | `dummy_sid` | `ACxxxxxxxxxxxxxxxxxxxxxxxxxxxx` |
| `TWILIO_AUTH_TOKEN` | Twilio Cloud Auth Token | `dummy_token` | `xxxxxxxxxxxxxxxxxxxxxxxxxxxx` |
| `TWILIO_PHONE_NUMBER` | Twilio Outbound Phone Number | `+1234567890` | `+15551234567` |
| `CORS_ALLOWED_ORIGINS` | Permitted Frontend Origins | `http://localhost:3000,http://localhost:5173` | `https://*.onrender.com,https://*.vercel.app` |

---

## 📄 License & Credits
Developed as part of the GUVI Java Full Stack Development Program by **Sivaprasanna**. Built with Java 21, Spring Boot 4, MongoDB Atlas, Twilio, and Docker.
