package com.smartedu.db;

import com.smartedu.model.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

public class Database {

    public static final List<User>       users      = new CopyOnWriteArrayList<>();
    public static final List<Attendance> attendance = new CopyOnWriteArrayList<>();
    public static final List<Exam>       exams      = new CopyOnWriteArrayList<>();
    public static final List<Note>       notes      = new CopyOnWriteArrayList<>();

    public static final AtomicLong userIdSeq       = new AtomicLong(1);
    public static final AtomicLong attendanceIdSeq = new AtomicLong(1);
    public static final AtomicLong examIdSeq       = new AtomicLong(1);
    public static final AtomicLong noteIdSeq       = new AtomicLong(1);

    public static void init() {
        final String demoStudentId = "STU2026001";
        final String demoTeacherId = "TCH2026001";

        User admin = new User();
        admin.id        = userIdSeq.getAndIncrement();
        admin.userId    = "ADMIN001";
        admin.password  = "admin@123";
        admin.role      = "admin";
        admin.name      = "System Administrator";
        admin.email     = "admin@smartedu.edu";
        admin.createdAt = "2026-01-01T00:00:00";
        users.add(admin);

        User teacher = new User();
        teacher.id         = userIdSeq.getAndIncrement();
        teacher.userId     = demoTeacherId;
        teacher.password   = "teach@123";
        teacher.role       = "teacher";
        teacher.name       = "Dr. Rajesh Kumar";
        teacher.email      = "rajesh@smartedu.edu";
        teacher.department = "Computer Science";
        teacher.subjects   = "Data Structures, Database Management";
        teacher.createdAt  = "2026-01-01T00:00:00";
        users.add(teacher);

        User student = new User();
        student.id          = userIdSeq.getAndIncrement();
        student.userId      = demoStudentId;
        student.password    = "stud@123";
        student.role        = "student";
        student.name        = "Priya Sharma";
        student.email       = "priya@smartedu.edu";
        student.rollNo      = "CS2024001";
        student.year        = "2nd Year";
        student.branch      = "Computer Science";
        student.parentEmail = "parent@gmail.com";
        student.parentPhone = "+91 9876543210";
        student.createdAt   = "2026-01-01T00:00:00";
        users.add(student);

        attendance.add(makeAtt(demoStudentId,"Priya Sharma","2026-03-10","Data Structures","present","2nd Year","Computer Science"));
        attendance.add(makeAtt(demoStudentId,"Priya Sharma","2026-03-10","Database Management","absent","2nd Year","Computer Science"));
        attendance.add(makeAtt(demoStudentId,"Priya Sharma","2026-03-11","Data Structures","present","2nd Year","Computer Science"));
        attendance.add(makeAtt(demoStudentId,"Priya Sharma","2026-03-12","Operating Systems","present","2nd Year","Computer Science"));

        exams.add(makeExam(demoStudentId,"Priya Sharma","CAE-1","Data Structures",85,100,"2026-02-15","2nd Year","Computer Science"));
        exams.add(makeExam(demoStudentId,"Priya Sharma","CAE-1","Database Management",78,100,"2026-02-16","2nd Year","Computer Science"));
        exams.add(makeExam(demoStudentId,"Priya Sharma","TAE-1","Data Structures",42,50,"2026-03-01","2nd Year","Computer Science"));

        notes.add(makeNote("Data Structures","Arrays and Linked Lists","2nd Year","Computer Science","Fundamental data structures with examples","2026-02-20","Teacher"));
        notes.add(makeNote("Database Management","SQL Joins & Queries","2nd Year","Computer Science","Complete guide to SQL joins","2026-03-01","Teacher"));

        System.out.println("Database initialized with demo data.");
    }

    private static Attendance makeAtt(String sid, String sname, String date, String subject, String status, String year, String branch) {
        Attendance a = new Attendance();
        a.id = attendanceIdSeq.getAndIncrement();
        a.studentId = sid; a.studentName = sname; a.date = date;
        a.subject = subject; a.status = status; a.year = year; a.branch = branch;
        return a;
    }

    private static Exam makeExam(String sid, String sname, String name, String subject, int score, int maxScore, String date, String year, String branch) {
        Exam e = new Exam();
        e.id = examIdSeq.getAndIncrement();
        e.studentId = sid; e.studentName = sname; e.name = name;
        e.subject = subject; e.score = score; e.maxScore = maxScore;
        e.date = date; e.year = year; e.branch = branch;
        return e;
    }

    private static Note makeNote(String subject, String title, String year, String branch, String desc, String uploadDate, String uploadedBy) {
        Note n = new Note();
        n.id = noteIdSeq.getAndIncrement();
        n.subject = subject; n.title = title; n.year = year;
        n.branch = branch; n.description = desc;
        n.uploadDate = uploadDate; n.uploadedBy = uploadedBy;
        return n;
    }
}
