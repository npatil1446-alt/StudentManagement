package com.smartedu.server;

import com.sun.net.httpserver.HttpServer;
import com.smartedu.handler.*;
import com.smartedu.db.Database;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        // Railway injects PORT; fall back to 8080 for local dev
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            port = Integer.parseInt(envPort.trim());
        }

        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/auth/login",  new AuthHandler());
        server.createContext("/api/users",       new UserHandler());
        server.createContext("/api/attendance",  new AttendanceHandler());
        server.createContext("/api/exams",       new ExamHandler());
        server.createContext("/api/notes",       new NoteHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("=========================================");
        System.out.println("  SmartEdu Backend running on port " + port);
        System.out.println("=========================================");
        System.out.println("  Admin:   ADMIN001   / admin@123");
        System.out.println("  Teacher: TCH2026001 / teach@123");
        System.out.println("  Student: STU2026001 / stud@123");
        System.out.println("=========================================");
    }
}
