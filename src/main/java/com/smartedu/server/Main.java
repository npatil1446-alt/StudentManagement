package com.smartedu.server;

import com.sun.net.httpserver.HttpServer;
import com.smartedu.handler.*;
import com.smartedu.db.Database;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        Database.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/auth/login",  new AuthHandler());
        server.createContext("/api/users",       new UserHandler());
        server.createContext("/api/attendance",  new AttendanceHandler());
        server.createContext("/api/exams",       new ExamHandler());
        server.createContext("/api/notes",       new NoteHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("=========================================");
        System.out.println("  SmartEdu Backend running on port 8080 ");
        System.out.println("  Open smartedu-frontend-java.html       ");
        System.out.println("=========================================");
        System.out.println("  Admin:   ADMIN001   / admin@123");
        System.out.println("  Teacher: TCH2026001 / teach@123");
        System.out.println("  Student: STU2026001 / stud@123");
        System.out.println("=========================================");
    }
}
