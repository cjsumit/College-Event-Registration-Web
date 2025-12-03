package com.college.event;

import static spark.Spark.*;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // Initialize DB
        Database.initDatabase();

        // Configure Spark
        port(3000);
        staticFiles.location("/static"); // resources/static

        // Simple health
        get("/api/health", (req, res) -> {
            res.type("application/json");
            Map<String, Object> m = new HashMap<>();
            m.put("status", "ok");
            return gson.toJson(m);
        });

        // POST registration
        post("/api/register", (req, res) -> {
            res.type("application/json");
            try {
                Registration payload = gson.fromJson(req.body(), Registration.class);
                if (payload == null || payload.studentName == null || payload.studentName.trim().isEmpty()
                        || (payload.eventName == null && payload.id == null)) {
                    res.status(400);
                    return gson.toJson(Map.of("success", false, "message", "studentName and event selection are required"));
                }

                // If event id provided, fetch event
                Event ev = null;
                if (payload.id != null) ev = Database.getEventById(payload.id);
                String eventName = payload.eventName;
                if (ev != null) eventName = ev.title;

                boolean ok = Database.insertRegistration(payload.studentName.trim(), eventName, payload.tickets <= 0 ? 1 : payload.tickets,
                        payload.email, payload.phone);
                if (ok) {
                    return gson.toJson(Map.of("success", true, "message", "Registration saved"));
                } else {
                    res.status(500);
                    return gson.toJson(Map.of("success", false, "message", "Failed to save registration"));
                }
            } catch (Exception ex) {
                res.status(500);
                return gson.toJson(Map.of("success", false, "message", ex.getMessage()));
            }
        });

        // GET recent registrations
        get("/api/registrations", (req, res) -> {
            res.type("application/json");
            int limit = 50;
            try {
                String l = req.queryParams("limit");
                if (l != null) limit = Integer.parseInt(l);
            } catch (NumberFormatException ignored) {
            }
            List<Registration> list = Database.getRecentRegistrations(limit);
            return gson.toJson(list);
        });

        // Events endpoints
        get("/api/events", (req, res) -> {
            res.type("application/json");
            return gson.toJson(Database.getAllEvents());
        });

        get("/api/events/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Event e = Database.getEventById(id);
                if (e == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "not found"));
                }
                return gson.toJson(e);
            } catch (NumberFormatException ex) {
                res.status(400);
                return gson.toJson(Map.of("error", "invalid id"));
            }
        });

        // Admin login (simple)
        post("/api/admin/login", (req, res) -> {
            res.type("application/json");
            try {
                Map payload = gson.fromJson(req.body(), Map.class);
                String user = (String) payload.get("username");
                String pass = (String) payload.get("password");
                if (user == null || pass == null) {
                    res.status(400);
                    return gson.toJson(Map.of("success", false, "message", "username/password required"));
                }
                boolean ok = Database.validateAdmin(user, pass);
                if (ok) {
                    req.session(true).attribute("admin", user);
                    return gson.toJson(Map.of("success", true));
                } else {
                    res.status(401);
                    return gson.toJson(Map.of("success", false, "message", "invalid credentials"));
                }
            } catch (Exception ex) {
                res.status(500);
                return gson.toJson(Map.of("success", false, "message", ex.getMessage()));
            }
        });

        get("/api/admin/logout", (req, res) -> {
            req.session().removeAttribute("admin");
            return gson.toJson(Map.of("success", true));
        });

        // Admin: add event
        post("/api/admin/events", (req, res) -> {
            String admin = req.session().attribute("admin");
            if (admin == null) { res.status(403); return gson.toJson(Map.of("success", false, "message", "forbidden")); }
            Event ev = gson.fromJson(req.body(), Event.class);
            int id = Database.createEvent(ev);
            if (id > 0) return gson.toJson(Map.of("success", true, "id", id));
            res.status(500); return gson.toJson(Map.of("success", false));
        });

        // Admin: view registrations
        get("/api/admin/registrations", (req, res) -> {
            String admin = req.session().attribute("admin");
            if (admin == null) { res.status(403); return gson.toJson(Map.of("success", false, "message", "forbidden")); }
            return gson.toJson(Database.getAllRegistrations());
        });

        // Root serves index.html from static
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });
    }
}