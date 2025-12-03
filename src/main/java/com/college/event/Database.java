package com.college.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class Database {
    private static final String DB_FILE = "registrations.db";
    private static final String SQL_LOG = "registrations.sql";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    String sql = "CREATE TABLE IF NOT EXISTS registrations (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "student_name TEXT NOT NULL, " +
                            "event_name TEXT NOT NULL, " +
                            "tickets INTEGER NOT NULL, " +
                            "email TEXT, " +
                            "phone TEXT, " +
                            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                            ");";
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Ensure SQL log exists
        try {
            File f = new File(SQL_LOG);
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Extended DB initialization: events and users tables and schema updates
    static {
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            try (Statement stmt = conn.createStatement()) {
                // events table
                String ev = "CREATE TABLE IF NOT EXISTS events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "type TEXT, " +
                        "start_datetime TEXT, " +
                        "end_datetime TEXT, " +
                        "venue TEXT, " +
                        "description TEXT, " +
                        "rules TEXT, " +
                        "coordinators TEXT, " +
                        "prizes TEXT, " +
                        "fee TEXT, " +
                        "banner TEXT" +
                        ");";
                stmt.execute(ev);

                // users table (for admin)
                String us = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "role TEXT NOT NULL" +
                        ");";
                stmt.execute(us);

                // Add event_id to registrations if missing
                try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(registrations);")) {
                    boolean hasEventId = false;
                    while (rs.next()) {
                        String name = rs.getString("name");
                        if ("event_id".equalsIgnoreCase(name)) {
                            hasEventId = true;
                            break;
                        }
                    }
                    if (!hasEventId) {
                        stmt.execute("ALTER TABLE registrations ADD COLUMN event_id INTEGER DEFAULT NULL;");
                    }
                }
            }

            // ensure default admin exists
            createAdminIfNotExists("admin", "admin");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean insertRegistration(String studentName, String eventName, int tickets, String email, String phone) {
        String insertSql = "INSERT INTO registrations(student_name, event_name, tickets, email, phone) VALUES(?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(JDBC_URL);
            // ensure explicit transaction so commits are reliable across environments
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, studentName);
            pstmt.setString(2, eventName);
            pstmt.setInt(3, tickets);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            int updated = pstmt.executeUpdate();
            conn.commit();

            if (updated > 0) {
                // Append an equivalent INSERT into the SQL log file
                appendToSqlLog(studentName, eventName, tickets, email, phone);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    private static void appendToSqlLog(String studentName, String eventName, int tickets, String email, String phone) {
        // Escape single quotes by doubling them for SQL
        String sName = escape(studentName);
        String eName = escape(eventName);
        String sEmail = email == null ? "" : escape(email);
        String sPhone = phone == null ? "" : escape(phone);
        String sql = String.format("INSERT INTO registrations(student_name, event_name, tickets, email, phone) VALUES('%s','%s',%d,'%s','%s');%n",
                sName, eName, tickets, sEmail, sPhone);
        try (FileWriter fw = new FileWriter(SQL_LOG, true)) {
            fw.write(sql);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String escape(String input) {
        if (input == null) return "";
        return input.replace("'", "''");
    }

    public static String getDbFilePath() {
        try {
            return Paths.get(new File(DB_FILE).getAbsolutePath()).toString();
        } catch (Exception e) {
            return DB_FILE;
        }
    }

    public static String getSqlLogPath() {
        try {
            return Paths.get(new File(SQL_LOG).getAbsolutePath()).toString();
        } catch (Exception e) {
            return SQL_LOG;
        }
    }

    // Fetch recent registrations
    public static java.util.List<Registration> getRecentRegistrations(int limit) {
        java.util.List<Registration> list = new java.util.ArrayList<>();
        String sql = "SELECT id, student_name, event_name, tickets, email, phone, created_at FROM registrations ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Registration r = new Registration(
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("event_name"),
                            rs.getInt("tickets"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("created_at")
                    );
                    list.add(r);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Events CRUD
    public static int createEvent(Event e) {
        String sql = "INSERT INTO events(title,type,start_datetime,end_datetime,venue,description,rules,coordinators,prizes,fee,banner) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, e.title);
            pstmt.setString(2, e.type);
            pstmt.setString(3, e.startDatetime);
            pstmt.setString(4, e.endDatetime);
            pstmt.setString(5, e.venue);
            pstmt.setString(6, e.description);
            pstmt.setString(7, e.rules);
            pstmt.setString(8, e.coordinators);
            pstmt.setString(9, e.prizes);
            pstmt.setString(10, e.fee);
            pstmt.setString(11, e.banner);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public static java.util.List<Event> getAllEvents() {
        java.util.List<Event> list = new java.util.ArrayList<>();
        String sql = "SELECT id,title,type,start_datetime,end_datetime,venue,description,rules,coordinators,prizes,fee,banner FROM events ORDER BY start_datetime ASC";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Event ev = new Event(
                        rs.getInt("id"), rs.getString("title"), rs.getString("type"), rs.getString("start_datetime"), rs.getString("end_datetime"),
                        rs.getString("venue"), rs.getString("description"), rs.getString("rules"), rs.getString("coordinators"), rs.getString("prizes"), rs.getString("fee"), rs.getString("banner")
                );
                list.add(ev);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public static Event getEventById(int id) {
        String sql = "SELECT id,title,type,start_datetime,end_datetime,venue,description,rules,coordinators,prizes,fee,banner FROM events WHERE id = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Event(rs.getInt("id"), rs.getString("title"), rs.getString("type"), rs.getString("start_datetime"), rs.getString("end_datetime"), rs.getString("venue"), rs.getString("description"), rs.getString("rules"), rs.getString("coordinators"), rs.getString("prizes"), rs.getString("fee"), rs.getString("banner"));
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    public static boolean updateEvent(Event e) {
        String sql = "UPDATE events SET title=?,type=?,start_datetime=?,end_datetime=?,venue=?,description=?,rules=?,coordinators=?,prizes=?,fee=?,banner=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.title);
            pstmt.setString(2, e.type);
            pstmt.setString(3, e.startDatetime);
            pstmt.setString(4, e.endDatetime);
            pstmt.setString(5, e.venue);
            pstmt.setString(6, e.description);
            pstmt.setString(7, e.rules);
            pstmt.setString(8, e.coordinators);
            pstmt.setString(9, e.prizes);
            pstmt.setString(10, e.fee);
            pstmt.setString(11, e.banner);
            pstmt.setInt(12, e.id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }

    public static boolean deleteEvent(int id) {
        String sql = "DELETE FROM events WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }

    // Admin user
    public static void createAdminIfNotExists(String username, String password) {
        String check = "SELECT id FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement p = conn.prepareStatement(check)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return; // exists
            }
            String ins = "INSERT INTO users(username,password,role) VALUES(?,?,?)";
            try (PreparedStatement pi = conn.prepareStatement(ins)) {
                pi.setString(1, username);
                pi.setString(2, password);
                pi.setString(3, "admin");
                pi.executeUpdate();
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public static boolean validateAdmin(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ? AND role='admin' LIMIT 1";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, password);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }

    // Registration queries for admin/student
    public static java.util.List<Registration> getRegistrationsForEvent(int eventId) {
        java.util.List<Registration> list = new java.util.ArrayList<>();
        String sql = "SELECT id, student_name, event_name, tickets, email, phone, created_at FROM registrations WHERE event_id = ? ORDER BY created_at DESC";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, eventId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(rs.getInt("id"), rs.getString("student_name"), rs.getString("event_name"), rs.getInt("tickets"), rs.getString("email"), rs.getString("phone"), rs.getString("created_at")));
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public static java.util.List<Registration> getRegistrationsForEmail(String email) {
        java.util.List<Registration> list = new java.util.ArrayList<>();
        String sql = "SELECT id, student_name, event_name, tickets, email, phone, created_at FROM registrations WHERE email = ? ORDER BY created_at DESC";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, email);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(rs.getInt("id"), rs.getString("student_name"), rs.getString("event_name"), rs.getInt("tickets"), rs.getString("email"), rs.getString("phone"), rs.getString("created_at")));
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public static java.util.List<Registration> getAllRegistrations() {
        java.util.List<Registration> list = new java.util.ArrayList<>();
        String sql = "SELECT id, student_name, event_name, tickets, email, phone, created_at FROM registrations ORDER BY created_at DESC";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement p = conn.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(new Registration(rs.getInt("id"), rs.getString("student_name"), rs.getString("event_name"), rs.getInt("tickets"), rs.getString("email"), rs.getString("phone"), rs.getString("created_at")));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }
}