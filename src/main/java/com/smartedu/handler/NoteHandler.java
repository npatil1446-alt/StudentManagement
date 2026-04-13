package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.util.Json;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class NoteHandler extends BaseHandler {

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
        String year   = queryParam(ex, "year");
        String branch = queryParam(ex, "branch");

        StringBuilder sql = new StringBuilder("SELECT * FROM notes WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (year   != null) { sql.append(" AND year=?");   params.add(year); }
        if (branch != null) { sql.append(" AND branch=?"); params.add(branch); }

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
        String sql = "INSERT INTO notes (title,subject,year,branch,description,upload_date,uploaded_by,file_name,file_size,file_data) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  Json.get(body,"title"));
            ps.setString(2,  Json.get(body,"subject"));
            ps.setString(3,  Json.get(body,"year"));
            ps.setString(4,  Json.get(body,"branch"));
            ps.setString(5,  Json.get(body,"description"));
            ps.setString(6,  Json.get(body,"uploadDate"));
            ps.setString(7,  Json.get(body,"uploadedBy"));
            ps.setString(8,  Json.get(body,"fileName"));
            ps.setString(9,  Json.get(body,"fileSize"));
            ps.setString(10, Json.get(body,"fileData"));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                long newId = keys.getLong(1);
                try (PreparedStatement sel = c.prepareStatement("SELECT * FROM notes WHERE id=?")) {
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
             PreparedStatement ps = c.prepareStatement("DELETE FROM notes WHERE id=?")) {
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
            "title",       Json.str(rs.getString("title")),
            "subject",     Json.str(rs.getString("subject")),
            "year",        Json.str(rs.getString("year")),
            "branch",      Json.str(rs.getString("branch")),
            "description", Json.str(rs.getString("description")),
            "uploadDate",  Json.str(rs.getString("upload_date")),
            "uploadedBy",  Json.str(rs.getString("uploaded_by")),
            "fileName",    Json.str(rs.getString("file_name")),
            "fileSize",    Json.str(rs.getString("file_size")),
            "fileData",    Json.str(rs.getString("file_data"))
        );
    }
}
