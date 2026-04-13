package com.smartedu.db;

import java.sql.*;

/**
 * Central place for obtaining JDBC connections.
 * Reads credentials from environment variables:
 *   DB_URL      e.g. jdbc:mysql://host:3306/smartedu
 *   DB_USER     e.g. root
 *   DB_PASSWORD e.g. secret
 */
public class Database {

    private static String url;
    private static String user;
    private static String password;

    /** Call once at startup to configure the driver and seed demo data. */
    public static void init() throws SQLException {
        url      = System.getenv("DB_URL");
        user     = System.getenv("DB_USER");
        password = System.getenv("DB_PASSWORD");

        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                "DB_URL environment variable is not set! " +
                "Set DB_URL, DB_USER and DB_PASSWORD before starting the server.");
        }

        // Verify we can actually connect
        try (Connection c = getConnection()) {
            System.out.println("✅  Database connected: " + url);
        }

        seedDemoData();
    }

    /** Returns a fresh connection from DriverManager each time. */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load driver — required in fat JARs where SPI files may be overwritten
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    // ----------------------------------------------------------------
    // Demo-data seeding (runs only when the tables are completely empty)
    // ----------------------------------------------------------------
    private static void seedDemoData() throws SQLException {
        try (Connection c = getConnection()) {
            // Only seed if there are no users yet
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("ℹ️  Database already has data – skipping seed.");
                    return;
                }
            }

            // Seed users
            String ins = "INSERT INTO users " +
                "(user_id,password,role,name,email,department,subjects," +
                " roll_no,year,branch,parent_email,parent_phone,created_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (PreparedStatement ps = c.prepareStatement(ins)) {
                // Admin
                setUser(ps, "ADMIN001","admin@123","admin","System Administrator",
                        "admin@smartedu.edu",null,null,null,null,null,null,null,"2026-01-01 00:00:00");
                ps.addBatch();

                // Teacher
                setUser(ps, "TCH2026001","teach@123","teacher","Dr. Rajesh Kumar",
                        "rajesh@smartedu.edu","Computer Science",
                        "Data Structures, Database Management",
                        null,null,null,null,null,"2026-01-01 00:00:00");
                ps.addBatch();

                // Student
                setUser(ps, "STU2026001","stud@123","student","Priya Sharma",
                        "priya@smartedu.edu",null,null,
                        "CS2024001","2nd Year","Computer Science",
                        "parent@gmail.com","+91 9876543210","2026-01-01 00:00:00");
                ps.addBatch();

                ps.executeBatch();
            }

            // Seed attendance
            String attSql = "INSERT INTO attendance (student_id,student_name,date,subject,status,year,branch) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(attSql)) {
                addAtt(ps,"STU2026001","Priya Sharma","2026-03-10","Data Structures","present","2nd Year","Computer Science");
                addAtt(ps,"STU2026001","Priya Sharma","2026-03-10","Database Management","absent","2nd Year","Computer Science");
                addAtt(ps,"STU2026001","Priya Sharma","2026-03-11","Data Structures","present","2nd Year","Computer Science");
                addAtt(ps,"STU2026001","Priya Sharma","2026-03-12","Operating Systems","present","2nd Year","Computer Science");
                ps.executeBatch();
            }

            // Seed exams
            String examSql = "INSERT INTO exams (student_id,student_name,name,subject,score,max_score,date,year,branch) VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(examSql)) {
                addExam(ps,"STU2026001","Priya Sharma","CAE-1","Data Structures",85,100,"2026-02-15","2nd Year","Computer Science");
                addExam(ps,"STU2026001","Priya Sharma","CAE-1","Database Management",78,100,"2026-02-16","2nd Year","Computer Science");
                addExam(ps,"STU2026001","Priya Sharma","TAE-1","Data Structures",42,50,"2026-03-01","2nd Year","Computer Science");
                ps.executeBatch();
            }

            // Seed notes
            String noteSql = "INSERT INTO notes (title,subject,year,branch,description,upload_date,uploaded_by) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(noteSql)) {
                addNote(ps,"Arrays and Linked Lists","Data Structures","2nd Year","Computer Science","Fundamental data structures with examples","2026-02-20","Teacher");
                addNote(ps,"SQL Joins & Queries","Database Management","2nd Year","Computer Science","Complete guide to SQL joins","2026-03-01","Teacher");
                ps.executeBatch();
            }

            System.out.println("✅  Demo data seeded successfully.");
        }
    }

    // ---------- helpers ----------

    private static void setUser(PreparedStatement ps,
            String userId, String pwd, String role, String name, String email,
            String dept, String subjects, String rollNo, String year, String branch,
            String parentEmail, String parentPhone, String createdAt) throws SQLException {
        ps.setString(1,  userId);
        ps.setString(2,  pwd);
        ps.setString(3,  role);
        ps.setString(4,  name);
        ps.setString(5,  email);
        ps.setString(6,  dept);
        ps.setString(7,  subjects);
        ps.setString(8,  rollNo);
        ps.setString(9,  year);
        ps.setString(10, branch);
        ps.setString(11, parentEmail);
        ps.setString(12, parentPhone);
        ps.setString(13, createdAt);
    }

    private static void addAtt(PreparedStatement ps,
            String sid, String sname, String date, String subject, String status,
            String year, String branch) throws SQLException {
        ps.setString(1, sid); ps.setString(2, sname); ps.setString(3, date);
        ps.setString(4, subject); ps.setString(5, status);
        ps.setString(6, year); ps.setString(7, branch);
        ps.addBatch();
    }

    private static void addExam(PreparedStatement ps,
            String sid, String sname, String name, String subject,
            int score, int maxScore, String date, String year, String branch) throws SQLException {
        ps.setString(1, sid); ps.setString(2, sname); ps.setString(3, name);
        ps.setString(4, subject); ps.setInt(5, score); ps.setInt(6, maxScore);
        ps.setString(7, date); ps.setString(8, year); ps.setString(9, branch);
        ps.addBatch();
    }

    private static void addNote(PreparedStatement ps,
            String title, String subject, String year, String branch,
            String desc, String date, String by) throws SQLException {
        ps.setString(1, title); ps.setString(2, subject);
        ps.setString(3, year); ps.setString(4, branch);
        ps.setString(5, desc); ps.setString(6, date); ps.setString(7, by);
        ps.addBatch();
    }
}
