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

Replace these with your actual technologies if different:

- **Frontend:** JavaScript / Framework (React)
- **Backend:** Spring Boot
- **Database:** MySQL
- **Authentication:** JWT / Session-based
- **Version Control:** Git & GitHub

## Getting Started

These are example steps — adjust to your actual project setup.

1. Clone the repository:

   ```bash
   git clone <your-repo-url>
   cd <repo-folder>
   ```

2. Backend (Spring Boot / Maven):

   ```bash
   cd backend
   ./mvnw spring-boot:run    # or mvnw.cmd on Windows
   ```

3. Frontend (if React / Vite):

   ```bash
   cd web
   npm install
   npm run dev
   ```

4. Database:

   - Configure the connection in `backend/src/main/resources/application.properties`.
   - Run migrations or allow JPA/Hibernate to create schema (if configured).

## Project Structure (high-level)

- `backend/` — Spring Boot application and API
- `web/` — Frontend app (React / Vite)
- `mobile/` — Mobile app (optional)
- `docs/` — Project documentation and help files

## Contributing

- Fork the repo, create a feature branch, and open a pull request.
- Keep changes focused and add tests where appropriate.

## License

Specify your license here (e.g., MIT) or remove this section if not applicable.

---

If you'd like, I can update the Technology Stack to match the actual project files, add run scripts, or include database migration examples. See [README.md](README.md) for this file.
