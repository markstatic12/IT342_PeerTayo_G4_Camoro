# PeerTayo Evaluation System

## Project Description

PeerTayo Evaluation System is a web-based peer evaluation platform designed to facilitate structured and standardized performance assessment among registered users. The system enables users to create evaluation forms, assign evaluators and evaluatees, collect rating-based feedback, and view summarized results through an intuitive dashboard.

The platform focuses on simplicity, usability, and measurable evaluation using a fixed set of rating-scale questions. All evaluation activities are handled within the system through dashboards and in-app notifications, making it suitable for academic, organizational, or team-based environments.

## Objectives

- **Secure Authentication:** Provide a functional peer evaluation platform with secure user authentication.
- **Form Management:** Allow users to create and distribute evaluation forms.
- **Assignments:** Enable assignment of evaluators and evaluatees.
- **Standardized Ratings:** Implement a standardized rating-based evaluation process (fixed questionnaire).
- **Results Visualization:** Display summarized results with simple statistics and basic charts.
- **Notifications:** Provide in-app notifications for pending evaluations.
- **Usability:** Deliver a clean and intuitive user experience feasible for solo development.

## Key Features

- **User Authentication:** User registration, login, secure sessions, and logout.
- **Evaluation Management:** Create evaluation forms, assign evaluators/evaluatees, set deadlines, and use a fixed 10-question rating-scale (1–5).
- **Evaluation Process:** Dashboard for pending evaluations, modal-based submission, and optional feedback comments.
- **Results & Analytics:** View personal results, per-question average ratings, overall summaries, and simple graphical statistics.
- **Notifications:** In-app alerts for pending tasks and tracking of evaluation progress.
- **Tracking System:** View created evaluations and monitor submission/completion progress.

## Excluded Features

To keep the system simple and maintainable, the following features are intentionally excluded:

- Customizable questionnaires
- Email or SMS notifications
- Real-time push updates
- Role-based administration
- Group/team management
- External system integration
- Advanced analytics

## Technology Stack

- **Frontend:** React + Vite
- **Backend:** Spring Boot (Java)
- **Mobile:** Android (Kotlin) - MVVM + Clean Architecture
- **Database:** MySQL
- **Authentication:** JWT
- **Version Control:** Git & GitHub

## Project Structure

```
IT342_PeerTayo_G4_Camoro/
├── backend/          # Spring Boot REST API
├── web/              # React frontend application
├── mobile/           # Android app (Kotlin)
│   ├── app/
│   │   └── src/main/java/com/example/peertayo_mobile/
│   │       ├── core/         # Base classes
│   │       ├── data/         # Data layer (API, Database, Repository)
│   │       ├── domain/       # Business logic (Models, Use Cases)
│   │       ├── ui/           # UI layer (Activities, ViewModels)
│   │       ├── di/           # Dependency Injection (Hilt)
│   │       └── utils/        # Utilities
│   ├── README.md             # Mobile app documentation
│   ├── TESTING.md            # Testing guide
│   └── setup_folders.bat     # Folder structure setup script
└── docs/             # Project documentation and SDD
```

## Getting Started

### Backend Setup

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Configure database in `application.properties`

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run    # or mvnw.cmd on Windows
   ```

4. Backend will be available at: `http://localhost:8080`

### Frontend Setup

1. Navigate to web directory:
   ```bash
   cd web
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run development server:
   ```bash
   npm run dev
   ```

4. Frontend will be available at the URL shown in terminal

### Mobile Setup (Android)

1. **Prerequisites:**
   - Android Studio installed
   - Android SDK configured
   - JDK 17 or higher

2. **Configure Backend URL:**
   - Open `mobile/app/src/main/java/com/example/peertayo_mobile/utils/Constants.kt`
   - For emulator: `BASE_URL = "http://10.0.2.2:8080/"`
   - For physical device: `BASE_URL = "http://YOUR_IP:8080/"`

3. **Build and Run:**
   - Open `mobile/` folder in Android Studio
   - Sync Gradle files
   - Run on emulator or device

4. **Detailed Instructions:**
   - See `mobile/README.md` for complete setup guide
   - See `mobile/TESTING.md` for testing instructions

## Mobile App Features

✅ **Implemented:**
- User Registration with validation
- User Login with JWT authentication
- Secure token storage (EncryptedSharedPreferences)
- Main dashboard with user info
- Logout functionality
- Clean Architecture (MVVM + Clean Architecture)
- Dependency Injection with Hilt
- Offline caching with Room
- Reactive UI with Kotlin Flow

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `GET /api/v1/auth/me` - Get current user info
- `POST /api/v1/auth/logout` - Logout user


## Contributing

- Fork the repo, create a feature branch, and open a pull request.
- Keep changes focused and add tests where appropriate.

## License

Specify your license here (e.g., MIT) or remove this section if not applicable.

---

If you'd like, I can update the Technology Stack to match the actual project files, add run scripts, or include database migration examples. See [README.md](README.md) for this file.
