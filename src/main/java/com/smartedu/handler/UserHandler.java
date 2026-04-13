package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.util.Json;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserHandler extends BaseHandler {

    @Override
    protected void route(HttpExchange ex) throws IOException {
        switch (method(ex)) {
            case "GET"    -> handleGet(ex);
            case "POST"   -> handleCreate(ex);
            case "PUT"    -> handleUpdate(ex);
            case "DELETE" -> handleDelete(ex);
            default       -> sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleGet(HttpExchange ex) throws IOException {
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {
            List<String> list = new ArrayList<>();
            while (rs.next()) list.add(AuthHandler.userJson(rs));
            sendJson(ex, 200, Json.arr(list));
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parse(readBody(ex));
        String role = Json.get(body, "role");
        String name = Json.get(body, "name").trim();

        if (name.isEmpty()) {
            sendJson(ex, 400, Json.obj("success", Json.bool(false), "message", Json.str("Name is required")));
            return;
        }

        try (Connection c = Database.getConnection()) {
            // Count existing users of this role to build the ID
            String prefix = role.equals("teacher") ? "TCH" : "STU";
            int year = LocalDateTime.now().getYear();
            long count;
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role=?")) {
                ps.setString(1, role);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); count = rs.getLong(1) + 1; }
            }
            String userId;
            do {
                userId = prefix + year + String.format("%03d", count++);
                try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE user_id=?")) {
                    ps.setString(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) break;
                    }
                }
            } while (true);

            String password = generatePassword();
            String createdAt = LocalDateTime.now().toString();

            String sql = "INSERT INTO users (user_id,password,role,name,email,department,subjects," +
                         "roll_no,year,branch,parent_email,parent_phone,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            long newId;
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, userId);
                ps.setString(2, password);
                ps.setString(3, role);
                ps.setString(4, name);
                ps.setString(5, Json.get(body,"email"));
                ps.setString(6, Json.get(body,"department"));
                ps.setString(7, Json.get(body,"subjects"));
                ps.setString(8, Json.get(body,"rollNo"));
                ps.setString(9, Json.get(body,"year"));
                ps.setString(10, Json.get(body,"branch"));
                ps.setString(11, Json.get(body,"parentEmail"));
                ps.setString(12, Json.get(body,"parentPhone"));
                ps.setString(13, createdAt);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); newId = keys.getLong(1); }
            }

            // Fetch the newly created user to return it
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id=?")) {
                ps.setLong(1, newId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    sendJson(ex, 200, Json.obj(
                        "success",  Json.bool(true),
                        "message",  Json.str("User created successfully"),
                        "userId",   Json.str(userId),
                        "password", Json.str(password),
                        "user",     AuthHandler.userJson(rs)
                    ));
                }
            }
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    private void handleUpdate(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        Map<String, String> body = Json.parse(readBody(ex));

        try (Connection c = Database.getConnection()) {
            // Check if exists
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        sendJson(ex, 404, Json.obj("success", Json.bool(false), "message", Json.str("User not found")));
                        return;
                    }
                }
            }

            // Build dynamic SET clause
            List<String> sets = new ArrayList<>();
            List<String> vals = new ArrayList<>();
            String[][] fields = {{"name","name"},{"email","email"},{"password","password"},
                {"department","department"},{"subjects","subjects"},{"rollNo","roll_no"},
                {"year","year"},{"branch","branch"},{"parentEmail","parent_email"},{"parentPhone","parent_phone"}};
            for (String[] f : fields) {
                String v = Json.get(body, f[0]);
                if (!v.isEmpty()) { sets.add(f[1] + "=?"); vals.add(v); }
            }
            if (!sets.isEmpty()) {
                String sql = "UPDATE users SET " + String.join(",", sets) + " WHERE id=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    for (int i = 0; i < vals.size(); i++) ps.setString(i + 1, vals.get(i));
                    ps.setLong(vals.size() + 1, id);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    sendJson(ex, 200, Json.obj("success", Json.bool(true), "message", Json.str("User updated"), "user", AuthHandler.userJson(rs)));
                }
            }
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        try (Connection c = Database.getConnection()) {
            // Get userId first so we can cascade-delete attendance/exams
            String userId;
            try (PreparedStatement ps = c.prepareStatement("SELECT user_id FROM users WHERE id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        sendJson(ex, 404, Json.obj("success", Json.bool(false), "message", Json.str("User not found")));
                        return;
                    }
                    userId = rs.getString("user_id");
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM attendance WHERE student_id=?")) {
                ps.setString(1, userId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM exams WHERE student_id=?")) {
                ps.setString(1, userId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?")) {
                ps.setLong(1, id); ps.executeUpdate();
            }
            sendJson(ex, 200, Json.obj("success", Json.bool(true), "message", Json.str("User deleted")));
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    private String generatePassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 9; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
