package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.model.Attendance;
import com.smartedu.util.Json;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceHandler extends BaseHandler {

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
        List<Attendance> result = new ArrayList<>(Database.attendance);
        if (studentId != null) result = result.stream().filter(a -> a.studentId.equals(studentId)).collect(Collectors.toList());
        if (year      != null) result = result.stream().filter(a -> a.year.equals(year)).collect(Collectors.toList());
        if (branch    != null) result = result.stream().filter(a -> a.branch.equals(branch)).collect(Collectors.toList());
        sendJson(ex, 200, Json.arr(result.stream().map(AttendanceHandler::toJson).collect(Collectors.toList())));
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parse(readBody(ex));
        Attendance a = new Attendance();
        a.id = Database.attendanceIdSeq.getAndIncrement();
        a.studentId = Json.get(body,"studentId"); a.studentName = Json.get(body,"studentName");
        a.date = Json.get(body,"date"); a.subject = Json.get(body,"subject");
        a.status = Json.get(body,"status"); a.year = Json.get(body,"year"); a.branch = Json.get(body,"branch");
        Database.attendance.add(a);
        sendJson(ex, 200, toJson(a));
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        boolean removed = Database.attendance.removeIf(a -> a.id == id);
        sendJson(ex, 200, Json.obj("success", Json.bool(removed), "message", Json.str(removed ? "Deleted" : "Not found")));
    }

    public static String toJson(Attendance a) {
        return Json.obj(
            "id",          Json.num(a.id),
            "studentId",   Json.str(a.studentId),
            "studentName", Json.str(a.studentName),
            "date",        Json.str(a.date),
            "subject",     Json.str(a.subject),
            "status",      Json.str(a.status),
            "year",        Json.str(a.year),
            "branch",      Json.str(a.branch)
        );
    }
}
