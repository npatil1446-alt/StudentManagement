package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.model.User;
import com.smartedu.util.Json;
import java.io.IOException;
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

        User found = Database.users.stream()
                .filter(u -> u.userId.trim().toLowerCase().equals(userId) && u.password.equals(password))
                .findFirst().orElse(null);

        if (found == null) {
            sendJson(ex, 200, Json.obj("success", Json.bool(false),
                "message", Json.str("Invalid User ID or Password. Contact the Admin if you need access.")));
            return;
        }
        if (!found.role.equals(role)) {
            sendJson(ex, 200, Json.obj("success", Json.bool(false),
                "message", Json.str("This User ID belongs to a " + found.role + " account. Please select the correct role.")));
            return;
        }
        sendJson(ex, 200, Json.obj("success", Json.bool(true), "message", Json.str("Login successful"), "user", userJson(found)));
    }

    public static String userJson(User u) {
        return Json.obj(
            "id",          Json.num(u.id),
            "userId",      Json.str(u.userId),
            "role",        Json.str(u.role),
            "name",        Json.str(u.name),
            "email",       Json.str(u.email),
            "department",  Json.str(u.department),
            "subjects",    Json.str(u.subjects),
            "rollNo",      Json.str(u.rollNo),
            "year",        Json.str(u.year),
            "branch",      Json.str(u.branch),
            "parentEmail", Json.str(u.parentEmail),
            "parentPhone", Json.str(u.parentPhone),
            "createdAt",   Json.str(u.createdAt)
        );
    }
}
