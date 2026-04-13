package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.model.Exam;
import com.smartedu.util.Json;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        List<Exam> result = new ArrayList<>(Database.exams);
        if (studentId != null) result = result.stream().filter(e -> e.studentId.equals(studentId)).collect(Collectors.toList());
        if (year      != null) result = result.stream().filter(e -> e.year.equals(year)).collect(Collectors.toList());
        if (branch    != null) result = result.stream().filter(e -> e.branch.equals(branch)).collect(Collectors.toList());
        sendJson(ex, 200, Json.arr(result.stream().map(ExamHandler::toJson).collect(Collectors.toList())));
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parse(readBody(ex));
        Exam e = new Exam();
        e.id = Database.examIdSeq.getAndIncrement();
        e.studentId = Json.get(body,"studentId"); e.studentName = Json.get(body,"studentName");
        e.name = Json.get(body,"name"); e.subject = Json.get(body,"subject");
        e.score = Json.getInt(body,"score"); e.maxScore = Json.getInt(body,"maxScore");
        e.date = Json.get(body,"date"); e.year = Json.get(body,"year"); e.branch = Json.get(body,"branch");
        Database.exams.add(e);
        sendJson(ex, 200, toJson(e));
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        boolean removed = Database.exams.removeIf(e -> e.id == id);
        sendJson(ex, 200, Json.obj("success", Json.bool(removed), "message", Json.str(removed ? "Deleted" : "Not found")));
    }

    public static String toJson(Exam e) {
        return Json.obj(
            "id",          Json.num(e.id),
            "studentId",   Json.str(e.studentId),
            "studentName", Json.str(e.studentName),
            "name",        Json.str(e.name),
            "subject",     Json.str(e.subject),
            "score",       Json.num(e.score),
            "maxScore",    Json.num(e.maxScore),
            "date",        Json.str(e.date),
            "year",        Json.str(e.year),
            "branch",      Json.str(e.branch)
        );
    }
}
