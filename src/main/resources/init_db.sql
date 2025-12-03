-- init_db.sql
-- Creates the registrations table used by the application

CREATE TABLE IF NOT EXISTS registrations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  student_name TEXT NOT NULL,
  event_name TEXT NOT NULL,
  tickets INTEGER NOT NULL,
  email TEXT,
  phone TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
