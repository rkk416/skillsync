# SkillSync – Career & Collaboration Intelligence Platform

SkillSync is a professional JavaFX desktop workstation designed for student profile management, AI-powered career recommendations, placement eligibility tracking, and capstone project team collaboration. 

Built using **Java 21**, **JavaFX**, and a relational **PostgreSQL** database, it provides real-time operational analytics and student-to-student networking inside a clean, modern dashboard interface.

---

## Key Feature Modules

### 1. Dashboard & Live Analytics
- **Live KPI Counters**: Instant metrics for total registered students, partner companies, unique skills tracked, AI recommendations generated, active capstone teams, total placement applications, eligibility rate, and placement success rate.
- **Operational Charts**: 
  - *Skill Distribution & Industry Vertical segmentations* (Pie charts with interactive tooltips and percentage shares).
  - *Placement Applications status* (Bar chart detailing APPLIED, SHORTLISTED, REJECTED, and SELECTED rates).
  - *Engagement Trends* (Daily and weekly timeline activity logs showing network activity).
- **Platform Insights**: Dynamically generated notes highlighting placement readiness, network health, and trending skills.

### 2. AI-Powered Placement Module
- **Resume Upload & Parsing**: Supports PDF, DOCX, and TXT files, extracting resume text and parsing it using the **Google Gemini API**.
- **ATS Analysis**: Computes compatibility scores, detects skills, identifies target hiring companies, and generates custom priority roadmaps.
- **Interactive Eligibility Checker**: Select any company to analyze your placement score against their minimum GPA/eligibility bar, review missing skill gaps, and apply directly.

### 3. Capstone Team Collaboration
- **Team Registry**: Create and join capstone project teams (capped at 5 members per team) with live capacity tracking and description tags.
- **Student Discovery**: Search and filter students by name, university, degree, or professional skills.
- **AI Teammate Suggestions**: Recommends peer matches using network distance (Breadth-First Search) and skill alignment (Jaccard similarity coefficient).
- **Recommendations**: Recommend peers for outstanding collaboration directly in the UI.

### 4. Interactive Navigation Shell
- **Alerts System**: Quick notification panel checking for pending connection invitations and active team alerts.
- **Profile Hub**: Manage technical skills (restricted via ComboBox category constraints), certificates, and personal capstone projects.
- **Secure Authentication**: Register and login with secure salted password hashing (`PBKDF2WithHmacSHA256`) and active session persistence.

---

## Technology Stack

- **Core**: Java 21 & JavaFX 21
- **AI Integration**: Google Gemini API client
- **Database**: PostgreSQL 15+ (HikariCP Connection Pool)
- **Security**: PBKDF2 Password Hashing
- **Build Tool**: Maven 3.9+

---

## Directory Structure

```text
src/main/java/skillsync/
├── auth/              User authentication MVC components
├── profile/           Student profile and portfolio MVC components
├── placement/         AI resume parsing, company eligibility, and application tracking
├── collaboration/     Project teams registry, student discovery, and peer recommendations
├── recommendation/    Recommendation engine (Jaccard similarity and BFS paths)
├── dashboard/         Operational dashboard and analytics chart components
├── model/             Domain entities (Student, Team, PlacementApplication, ActivityLog)
├── repository/        PostgreSQL DAO layer (BaseRepository, SkillRepository, etc.)
├── service/           Business logic and AI API orchestrations
├── utils/             Navigation, password hashing, and ViewFactory shell styles
└── main/              JavaFX entry point & lifecycle manager
```

---

## Getting Started

### 1. Prerequisites
Ensure you have the following installed on your system:
- **JDK 21**
- **Maven 3.9+**
- **PostgreSQL 15+**

### 2. Database Configuration
1. Start your local PostgreSQL server and create a database named `skillsync`.
2. Execute the schema definitions found in [src/main/resources/schema.sql](src/main/resources/schema.sql) to initialize the tables:
   ```bash
   psql -U postgres -d skillsync -f src/main/resources/schema.sql
   ```
3. Copy the configuration template:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
4. Edit the newly created `application.properties` and provide your database url, credentials, and optional Gemini API key:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/skillsync
   DB_USER=your_postgres_username
   DB_PASSWORD=your_postgres_password
   GEMINI_API_KEY=your_gemini_api_key
   ```

### 3. Compile and Build
Verify compilation, run database connectivity tests, and verify service interfaces:
```bash
mvn clean verify
```

### 4. Launching the Desktop Workstation
Run the following Maven command to launch the JavaFX application window:
```bash
mvn javafx:run
```