# College Event Registration System
# College Event Registration System — Project Summary

This repository contains a small, full-stack Java application for managing college event registrations. It includes a Java backend (embedded Spark web server) with an SQLite database and a modern single-page web UI served as static assets. The project is designed as an MVP that is easy to run locally and extend for college needs (events, registrations, admin management).

This README is written for presentation at college — it explains what the project does, the technologies used, how it works, how to run and deploy it, and why it is valuable for a college environment.

## Goals

- Provide an easy-to-run registration system for campus events (technical, cultural, sports).
- Let students register for events via a responsive web UI.
- Allow admins to create events and view participant lists.
- Store registrations in a lightweight local database (SQLite) and keep an SQL log for portability.

## Key Features (implemented)

- Responsive web UI (single-page) with sections: Home, Events, Event Details, Registration, Student Dashboard, Admin Login & Admin Dashboard, Contact.
- Event CRUD (admin): add events (title, type, date/time, venue, description, rules, coordinators, prizes, fee, banner).
- Registration API and storage in SQLite (`registrations.db`), with an appended `registrations.sql` log that contains INSERT statements.
- Student lookup (dashboard) by email to view registered events.
- Admin session-based login (simple username/password) and registration listing.
- The UI includes theme selector, animated backgrounds, and an interactive layout optimized for presentations/demos.

## Architecture & Components

- Backend: Java 11+ using Spark Java (embedded web server) + Gson for JSON serialization.
- Database: SQLite (embedded file `registrations.db`) accessed via JDBC (sqlite-jdbc driver).
- Frontend: Static assets (HTML, CSS, JS, SVG) served from `src/main/resources/static`.
- Build: Maven for dependency management and packaging (assembly plugin produces a runnable "jar-with-dependencies").

High-level flow:

1. User opens the web UI (served by the Java app at http://localhost:4567).
2. The UI fetches events from `/api/events` and renders them.
3. When a student submits registration, the UI POSTs JSON to `/api/register`.
4. The backend validates and inserts into the `registrations` table and appends a line to `registrations.sql`.
5. Admins can log in at the Admin panel and create new events via `/api/admin/events` (session required).

## Database Schema (important tables)

- `events` — stores event metadata (id, title, type, start_datetime, end_datetime, venue, description, rules, coordinators, prizes, fee, banner)
- `registrations` — stores registrations (id, student_name, event_name, tickets, email, phone, created_at, event_id)
- `users` — simple user table for admin (id, username, password, role)

All tables are created automatically when the application starts if they do not exist.

## File / Folder Overview

- `pom.xml` — Maven build file (dependencies: sqlite-jdbc, spark-core, gson, assembly plugin).
- `src/main/java/com/college/event/` — backend Java source:
	- `Main.java` — starts Spark server, defines REST API endpoints.
	- `Database.java` — database initialization and helper methods (insert, query, event CRUD, admin validation).
	- `Event.java`, `Registration.java` — simple models used by API.
	- `RegistrationGUI.java` — legacy Swing GUI (kept for reference; app now runs as web UI).
- `src/main/resources/static/` — frontend static assets:
	- `index.html` — single-page UI (Home, Events, Register, Admin, etc.)
	- `styles.css` — themeable styles and background assets
	- `app.js` — client-side SPA logic: navigation, API calls, admin flow
	- `logo.svg`, `bg-blob.svg`, `bg-tiles.svg` — visual assets
- `registrations.db` — SQLite database file (created at runtime in working directory).
- `registrations.sql` — SQL log appended with INSERT statements for each registration.
- `REQUIRED.md` — installation & deployment notes.

## How the Project Works (step-by-step)

1. Start the backend (see "Run locally"). Spark serves static files and exposes API endpoints.
2. The browser loads `index.html` and executes `app.js` which calls `/api/events`.
3. Student selects an event and submits the registration form. The JS posts JSON to `/api/register`.
4. `Database.insertRegistration(...)` creates a JDBC connection to `registrations.db` and inserts the row within an explicit transaction (ensuring the data commits to disk).
5. The server appends an equivalent INSERT statement to `registrations.sql` (useful for importing into other DB systems).
6. Admins can log in (default admin created automatically: username `admin`, password `admin`) and view all registrations.

Note: The backend prints the path to the database in UI dialogs after registration. Verify that path if you inspect the DB file directly.

## How to Run (local development)

Prerequisites:

- Java JDK 11 or newer installed (check `java -version` and `javac -version`).
- Maven (recommended) or an IDE that understands Maven (IntelliJ IDEA, Eclipse).

Commands (PowerShell):

```powershell
cd 'E:\Languages\Projects\College Event Registration System\Code 1.0'
mvn clean package
java -jar target/event-registration-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Open a web browser to: `http://localhost:4567/`

Use the Admin panel to add events and view registrations.

## How to Deploy (summary)

- The easiest approach is to host the entire app on a Java-capable host (Render, Railway, Fly.io, Heroku, Azure Web App). Configure the service to run the `mvn package` build and start the jar with:

```
java -jar target/event-registration-1.0-SNAPSHOT-jar-with-dependencies.jar
```

- If you prefer static hosting for the frontend (Vercel), deploy the static `/static` contents separately and host the backend on Render/Railway; update `app.js` fetch URLs to point to the backend service and enable CORS on the backend.

## Security & Production Considerations

- Password storage: the current `users` table stores admin passwords in plaintext for the MVP. For production, migrate to hashed passwords (bcrypt) and implement secure password reset.
- Use HTTPS and run the server behind a reverse proxy (Nginx) or platform-managed TLS.
- Add input validation and rate-limiting to protect against spam and injection attacks.
- Add proper session management, CSRF protection and role-based access control for admin endpoints.

## Benefits to the College

- Centralized registration for events reduces manual data entry and errors.
- Quick participant list exports (CSV) can be added to help coordinators prepare name badges, attendance, and prizes.
- Easy event creation by admins reduces overhead of event management teams.
- Portable storage (SQLite + SQL log) makes backups and offline auditing simple.
- Lightweight and low-cost to host — can run on modest cloud instances or on-premise lab machines.

## Demo / Presentation Tips

1. Start the server locally and open the web UI.
2. Show the Home page, view Events, open an Event Details page.
3. Register a test student — the success dialog shows the DB path and SQL log path.
4. Open the Admin panel (login as `admin` / `admin`) and show the created event and registrations.
5. Optionally open `registrations.db` in DB Browser for SQLite to show persisted rows.

## Next Steps & Improvements

- Replace admin plaintext passwords with hashed passwords (bcrypt) and enable account management.
- Add CSV export and printable registration slips.
- Add student accounts and secure dashboard access.
- Improve validations and add server-side checks to ensure event capacity and fee payment status.
- Add unit and integration tests for API endpoints.

## Contact / Credits

Project created as part of a college assignment/demonstration. For code questions or contributions, open an issue or pull request in the repository.

---

Thank you — this project is a starting point you can expand into a full-featured event management platform for your college.
