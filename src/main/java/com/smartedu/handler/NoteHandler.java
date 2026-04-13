package com.smartedu.handler;

import com.sun.net.httpserver.HttpExchange;
import com.smartedu.db.Database;
import com.smartedu.model.Note;
import com.smartedu.util.Json;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        List<Note> result = new ArrayList<>(Database.notes);
        if (year   != null) result = result.stream().filter(n -> n.year.equals(year)).collect(Collectors.toList());
        if (branch != null) result = result.stream().filter(n -> n.branch.equals(branch)).collect(Collectors.toList());
        sendJson(ex, 200, Json.arr(result.stream().map(NoteHandler::toJson).collect(Collectors.toList())));
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parse(readBody(ex));
        Note n = new Note();
        n.id = Database.noteIdSeq.getAndIncrement();
        n.title = Json.get(body,"title"); n.subject = Json.get(body,"subject");
        n.year = Json.get(body,"year"); n.branch = Json.get(body,"branch");
        n.description = Json.get(body,"description"); n.uploadDate = Json.get(body,"uploadDate");
        n.uploadedBy = Json.get(body,"uploadedBy"); n.fileName = Json.get(body,"fileName");
        n.fileSize = Json.get(body,"fileSize"); n.fileData = Json.get(body,"fileData");
        Database.notes.add(n);
        sendJson(ex, 200, toJson(n));
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        long id = pathId(ex);
        boolean removed = Database.notes.removeIf(n -> n.id == id);
        sendJson(ex, 200, Json.obj("success", Json.bool(removed), "message", Json.str(removed ? "Deleted" : "Not found")));
    }

    public static String toJson(Note n) {
        return Json.obj(
            "id",          Json.num(n.id),
            "title",       Json.str(n.title),
            "subject",     Json.str(n.subject),
            "year",        Json.str(n.year),
            "branch",      Json.str(n.branch),
            "description", Json.str(n.description),
            "uploadDate",  Json.str(n.uploadDate),
            "uploadedBy",  Json.str(n.uploadedBy),
            "fileName",    Json.str(n.fileName),
            "fileSize",    Json.str(n.fileSize),
            "fileData",    Json.str(n.fileData)
        );
    }
}
