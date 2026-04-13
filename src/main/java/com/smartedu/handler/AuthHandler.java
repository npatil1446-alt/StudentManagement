package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.util.Json;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class AuthHandler extends BaseHandler {

    @Override
    protected void route(HttpExchange ex) throws IOException {
        if (!"POST".equals(method(ex))) {
            sendJson(ex, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> body = Json.parse(readBody(ex));
        String userId   = Json.get(body, "userId").trim().toLowerCase();
        String password = Json.get(body, "password");
        String role     = Json.get(body, "role");

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM users WHERE LOWER(user_id)=? AND password=? LIMIT 1")) {
            ps.setString(1, userId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    sendJson(ex, 200, Json.obj("success", Json.bool(false),
                        "message", Json.str("Invalid User ID or Password. Contact the Admin if you need access.")));
                    return;
                }
                String foundRole = rs.getString("role");
                if (!foundRole.equals(role)) {
                    sendJson(ex, 200, Json.obj("success", Json.bool(false),
                        "message", Json.str("This User ID belongs to a " + foundRole + " account. Please select the correct role.")));
                    return;
                }
                sendJson(ex, 200, Json.obj("success", Json.bool(true),
                    "message", Json.str("Login successful"),
                    "user", userJson(rs)));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    public static String userJson(ResultSet rs) throws SQLException {
        return Json.obj(
            "id",          Json.num(rs.getLong("id")),
            "userId",      Json.str(rs.getString("user_id")),
            "role",        Json.str(rs.getString("role")),
            "name",        Json.str(rs.getString("name")),
            "email",       Json.str(rs.getString("email")),
            "department",  Json.str(rs.getString("department")),
            "subjects",    Json.str(rs.getString("subjects")),
            "rollNo",      Json.str(rs.getString("roll_no")),
            "year",        Json.str(rs.getString("year")),
            "branch",      Json.str(rs.getString("branch")),
            "parentEmail", Json.str(rs.getString("parent_email")),
            "parentPhone", Json.str(rs.getString("parent_phone")),
            "createdAt",   Json.str(rs.getString("created_at"))
        );
    }
}
