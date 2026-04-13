package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.model.User;
import com.smartedu.util.Json;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UserHandler extends BaseHandler {

    @Override
    protected void route(HttpExchange ex) throws IOException {
        switch (method(ex)) {
            case "GET":
                handleGet(ex);
                break;
            case "POST":
                handleCreate(ex);
                break;
            case "PUT":
                handleUpdate(ex);
                break;
            case "DELETE":
                handleDelete(ex);
                break;
            default:
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleGet(HttpExchange ex) throws IOException {
        List<String> list = Database.users.stream().map(AuthHandler::userJson).collect(Collectors.toList());
        sendJson(ex, 200, Json.arr(list));
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parse(readBody(ex));
        String role = Json.get(body, "role");
        String name = Json.get(body, "name").trim();

        if (name.isEmpty()) {
            sendJson(ex, 400, Json.obj("success", Json.bool(false), "message", Json.str("Name is required")));
            return;
        }

        String prefix = role.equals("teacher") ? "TCH" : "STU";
        int year = LocalDateTime.now().getYear();
        long count = Database.users.stream().filter(u -> u.role.equals(role)).count() + 1;
        String userId = prefix + year + String.format("%03d", count);
        while (Database.users.stream().anyMatch(u -> u.userId.equals(userId))) {
            count++;
        }
        final String finalUserId = prefix + year + String.format("%03d", count);

        String password = generatePassword();

        User u = new User();
        u.id          = Database.userIdSeq.getAndIncrement();
        u.userId      = finalUserId;
        u.password    = password;
        u.role        = role;
        u.name        = name;
        u.email       = Json.get(body, "email");
        u.department  = Json.get(body, "department");
        u.subjects    = Json.get(body, "subjects");
        u.rollNo      = Json.get(body, "rollNo");
        u.year        = Json.get(body, "year");
        u.branch      = Json.get(body, "branch");
        u.parentEmail = Json.get(body, "parentEmail");
        u.parentPhone = Json.get(body, "parentPhone");
        u.createdAt   = LocalDateTime.now().toString();
        Database.users.add(u);

        sendJson(ex, 200, Json.obj(
            "success",  Json.bool(true),
            "message",  Json.str("User created successfully"),
            "userId",   Json.str(finalUserId),
            "password", Json.str(password),
            "user",     AuthHandler.userJson(u)
        ));
    }

    private void handleUpdate(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        Map<String, String> body = Json.parse(readBody(ex));
        Optional<User> opt = Database.users.stream().filter(u -> u.id == id).findFirst();
        if (opt.isEmpty()) { sendJson(ex, 404, Json.obj("success", Json.bool(false), "message", Json.str("User not found"))); return; }
        User u = opt.get();
        if (!Json.get(body,"name").isEmpty())        u.name        = Json.get(body,"name");
        if (!Json.get(body,"email").isEmpty())       u.email       = Json.get(body,"email");
        if (!Json.get(body,"password").isEmpty())    u.password    = Json.get(body,"password");
        if (!Json.get(body,"department").isEmpty())  u.department  = Json.get(body,"department");
        if (!Json.get(body,"subjects").isEmpty())    u.subjects    = Json.get(body,"subjects");
        if (!Json.get(body,"rollNo").isEmpty())      u.rollNo      = Json.get(body,"rollNo");
        if (!Json.get(body,"year").isEmpty())        u.year        = Json.get(body,"year");
        if (!Json.get(body,"branch").isEmpty())      u.branch      = Json.get(body,"branch");
        if (!Json.get(body,"parentEmail").isEmpty()) u.parentEmail = Json.get(body,"parentEmail");
        if (!Json.get(body,"parentPhone").isEmpty()) u.parentPhone = Json.get(body,"parentPhone");
        sendJson(ex, 200, Json.obj("success", Json.bool(true), "message", Json.str("User updated"), "user", AuthHandler.userJson(u)));
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        Optional<User> opt = Database.users.stream().filter(u -> u.id == id).findFirst();
        if (opt.isEmpty()) { sendJson(ex, 404, Json.obj("success", Json.bool(false), "message", Json.str("User not found"))); return; }
        String userId = opt.get().userId;
        Database.users.removeIf(u -> u.id == id);
        Database.attendance.removeIf(a -> a.studentId.equals(userId));
        Database.exams.removeIf(e -> e.studentId.equals(userId));
        sendJson(ex, 200, Json.obj("success", Json.bool(true), "message", Json.str("User deleted")));
    }

    private String generatePassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 9; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
