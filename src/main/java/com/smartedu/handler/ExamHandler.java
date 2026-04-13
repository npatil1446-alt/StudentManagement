package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.util.Json;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ExamHandler extends BaseHandler {

    @Override
    protected void route(HttpExchange ex) throws IOException {
        switch (method(ex)) {
            case "GET"    -> handleGet(ex);
            case "POST"   -> handleCreate(ex);
            case "DELETE" -> handleDelete(ex);
            default       -> sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleGet(HttpExchange ex) throws IOException {
        String studentId = queryParam(ex, "studentId");
        String year      = queryParam(ex, "year");
        String branch    = queryParam(ex, "branch");

        StringBuilder sql = new StringBuilder("SELECT * FROM exams WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (studentId != null) { sql.append(" AND student_id=?"); params.add(studentId); }
        if (year      != null) { sql.append(" AND year=?");       params.add(year); }
        if (branch    != null) { sql.append(" AND branch=?");     params.add(branch); }

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) rows.add(toJson(rs));
                sendJson(ex, 200, Json.arr(rows));
            }
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parse(readBody(ex));
        String sql = "INSERT INTO exams (student_id,student_name,name,subject,score,max_score,date,year,branch) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, Json.get(body,"studentId"));
            ps.setString(2, Json.get(body,"studentName"));
            ps.setString(3, Json.get(body,"name"));
            ps.setString(4, Json.get(body,"subject"));
            ps.setInt(5,    Json.getInt(body,"score"));
            ps.setInt(6,    Json.getInt(body,"maxScore"));
            ps.setString(7, Json.get(body,"date"));
            ps.setString(8, Json.get(body,"year"));
            ps.setString(9, Json.get(body,"branch"));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                long newId = keys.getLong(1);
                try (PreparedStatement sel = c.prepareStatement("SELECT * FROM exams WHERE id=?")) {
                    sel.setLong(1, newId);
                    try (ResultSet rs = sel.executeQuery()) { rs.next(); sendJson(ex, 200, toJson(rs)); }
                }
            }
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM exams WHERE id=?")) {
            ps.setLong(1, id);
            boolean removed = ps.executeUpdate() > 0;
            sendJson(ex, 200, Json.obj("success", Json.bool(removed), "message", Json.str(removed ? "Deleted" : "Not found")));
        } catch (SQLException e) {
            sendJson(ex, 500, Json.obj("success", Json.bool(false), "message", Json.str("DB error: " + e.getMessage())));
        }
    }

    public static String toJson(ResultSet rs) throws SQLException {
        return Json.obj(
            "id",          Json.num(rs.getLong("id")),
            "studentId",   Json.str(rs.getString("student_id")),
            "studentName", Json.str(rs.getString("student_name")),
            "name",        Json.str(rs.getString("name")),
            "subject",     Json.str(rs.getString("subject")),
            "score",       Json.num(rs.getInt("score")),
            "maxScore",    Json.num(rs.getInt("max_score")),
            "date",        Json.str(rs.getString("date")),
            "year",        Json.str(rs.getString("year")),
            "branch",      Json.str(rs.getString("branch"))
        );
    }
}
