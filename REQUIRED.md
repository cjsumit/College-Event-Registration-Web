# REQUIRED - prerequisites and run instructions

This file lists required software and step-by-step commands to build and run the College Event Registration web UI.

## Required software

- Java 17+ (JDK) — required to run and build the project. Verify with:

  ```powershell
  java -version
  javac -version
  ```

- Apache Maven (optional if you prefer running from your IDE). Install from https://maven.apache.org/. Verify with:

  ```powershell
  mvn -v
  ```

Notes:
- The project uses an embedded SQLite database (no external DB required).
- The application binds to port 4567 by default.

## Build and run (recommended using Maven)

1. Open PowerShell and change to project directory:

```powershell
cd 'E:\Languages\Projects\College Event Registration System\Code 1.0'
```

2. Build the project (this will produce a jar in `target/`). If you have Maven installed:

```powershell
mvn clean package
```

3. Run the packaged jar (assembly produces a jar-with-dependencies):

```powershell
java -jar target/event-registration-1.0-SNAPSHOT-jar-with-dependencies.jar
```

4. Open a browser and go to:

```
http://localhost:4567/
```

## Run without Maven (if you don't have Maven but have Java)

1. Compile .java files (ensure `sqlite-jdbc` and `gson` and spark-core jars are on the classpath) — easiest is to download the assembled jar (`event-registration-...-jar-with-dependencies.jar`) or install Maven.

If you prefer, open the project in an IDE (IntelliJ IDEA or Eclipse), import as a Maven project, let the IDE download dependencies, then run the `com.college.event.Main` class.

## Files created/used at runtime

- `registrations.db` — SQLite database file created next to the working directory.
- `registrations.sql` — appended SQL INSERT statements log.

## Ports and firewall

- The server listens on port 4567. If you have a firewall, allow traffic to this port for local testing.

## Troubleshooting

- If `mvn` is not recognized, install Maven or run from an IDE.
- If `java -jar` reports a missing class, ensure you used the `-jar-with-dependencies` jar or run via the IDE which sets classpath with dependencies.

## Next steps / optional

- Convert to a reverse-proxied production server (use Jetty/Tomcat or package into a container).
- Add authentication, export CSV, and server-side validation for production readiness.

## Deploying the project (overview)

This project contains a Java backend (Spark) and a static frontend (served by the backend). Vercel is optimized for static sites / serverless functions (Node) and does not directly host a long-running Java Spark server. Recommended deployment options:

- Option A — Deploy both frontend and backend together (recommended for simplicity): Use Render, Railway, Fly.io, or Heroku to host the Java jar (they support Java/Docker). Steps (Render example):
  1. Push code to GitHub repo.
  2. Create a new Web Service on Render and connect to the GitHub repo.
  3. Choose `jar` or `Docker` and set build command `mvn clean package` and start command `java -jar target/event-registration-1.0-SNAPSHOT-jar-with-dependencies.jar`.
  4. Set environment variables or ports if needed; the service will expose a public URL.

- Option B — Deploy frontend on Vercel (static) and backend on Render/Railway:
  - Frontend: move `src/main/resources/static` contents into a separate repo or the `public/` folder for a static site, then deploy to Vercel. Vercel will serve the static pages.
  - Backend: deploy the Java backend to Render/Railway and configure CORS or reverse proxy so the frontend calls the backend's URL (update fetch URLs in `app.js` if needed).

## Quick notes for GitHub + Vercel (frontend only)

1. Create a repo with just the `static/` folder (or configure Vercel to use `src/main/resources/static` as project root).
2. On Vercel, set the build output directory to the folder containing `index.html` and deploy.

If you want, I can prepare a small `vercel.json` for the static frontend and a Dockerfile for the Java backend so you can deploy the frontend to Vercel and the backend to a container host.
