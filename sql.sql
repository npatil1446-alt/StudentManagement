-- ============================================================
--  SmartEdu Manager - MySQL Database Setup
--  Run this file in MySQL Workbench
--  Steps:
--    1. Open MySQL Workbench
--    2. Click File > Open SQL Script > select this file
--    3. Click the lightning bolt (Execute) button
--    4. Done! Database is ready.
-- ============================================================

-- Step 1: Create and select the database
CREATE DATABASE IF NOT EXISTS smartedu;
USE smartedu;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    role         ENUM('admin','teacher','student') NOT NULL,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100),
    department   VARCHAR(100),
    subjects     VARCHAR(255),
    roll_no      VARCHAR(50),
    year         VARCHAR(20),
    branch       VARCHAR(100),
    parent_email VARCHAR(100),
    parent_phone VARCHAR(20),
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: attendance
-- ============================================================
CREATE TABLE IF NOT EXISTS attendance (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   VARCHAR(50)  NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    date         VARCHAR(20)  NOT NULL,
    subject      VARCHAR(100) NOT NULL,
    status       ENUM('present','absent') NOT NULL,
    year         VARCHAR(20),
    branch       VARCHAR(100)
);

-- ============================================================
-- TABLE: exams
-- ============================================================
CREATE TABLE IF NOT EXISTS exams (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   VARCHAR(50)  NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    subject      VARCHAR(100) NOT NULL,
    score        INT          NOT NULL DEFAULT 0,
    max_score    INT          NOT NULL DEFAULT 100,
    date         VARCHAR(20)  NOT NULL,
    year         VARCHAR(20),
    branch       VARCHAR(100)
);

-- ============================================================
-- TABLE: notes
-- ============================================================
CREATE TABLE IF NOT EXISTS notes (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    subject      VARCHAR(100) NOT NULL,
    year         VARCHAR(20),
    branch       VARCHAR(100),
    description  TEXT,
    upload_date  VARCHAR(20),
    uploaded_by  VARCHAR(100),
    file_name    VARCHAR(200),
    file_size    VARCHAR(50),
    file_data    LONGTEXT
);

-- ============================================================
-- DEMO DATA - Same as original project
-- ============================================================

-- Clear existing demo data (safe to re-run)
DELETE FROM notes      WHERE id <= 2;
DELETE FROM exams      WHERE id <= 3;
DELETE FROM attendance WHERE id <= 4;
DELETE FROM users      WHERE user_id IN ('ADMIN001','TCH2026001','STU2026001');

-- Reset auto increment
ALTER TABLE users      AUTO_INCREMENT = 1;
ALTER TABLE attendance AUTO_INCREMENT = 1;
ALTER TABLE exams      AUTO_INCREMENT = 1;
ALTER TABLE notes      AUTO_INCREMENT = 1;

-- Insert demo users
INSERT INTO users (user_id, password, role, name, email, department, subjects, roll_no, year, branch, parent_email, parent_phone) VALUES
('ADMIN001',   'admin@123', 'admin',   'System Administrator', 'admin@smartedu.edu',  NULL,               NULL,                                  NULL,        NULL,       NULL,               NULL,               NULL),
('TCH2026001', 'teach@123', 'teacher', 'Dr. Rajesh Kumar',     'rajesh@smartedu.edu', 'Computer Science', 'Data Structures, Database Management', NULL,        NULL,       NULL,               NULL,               NULL),
('STU2026001', 'stud@123',  'student', 'Priya Sharma',         'priya@smartedu.edu',  NULL,               NULL,                                  'CS2024001', '2nd Year', 'Computer Science', 'parent@gmail.com', '+91 9876543210');

-- Insert demo attendance
INSERT INTO attendance (student_id, student_name, date, subject, status, year, branch) VALUES
('STU2026001', 'Priya Sharma', '2026-03-10', 'Data Structures',     'present', '2nd Year', 'Computer Science'),
('STU2026001', 'Priya Sharma', '2026-03-10', 'Database Management', 'absent',  '2nd Year', 'Computer Science'),
('STU2026001', 'Priya Sharma', '2026-03-11', 'Data Structures',     'present', '2nd Year', 'Computer Science'),
('STU2026001', 'Priya Sharma', '2026-03-12', 'Operating Systems',   'present', '2nd Year', 'Computer Science');

-- Insert demo exams
INSERT INTO exams (student_id, student_name, name, subject, score, max_score, date, year, branch) VALUES
('STU2026001', 'Priya Sharma', 'CAE-1', 'Data Structures',     85, 100, '2026-02-15', '2nd Year', 'Computer Science'),
('STU2026001', 'Priya Sharma', 'CAE-1', 'Database Management', 78, 100, '2026-02-16', '2nd Year', 'Computer Science'),
('STU2026001', 'Priya Sharma', 'TAE-1', 'Data Structures',     42,  50, '2026-03-01', '2nd Year', 'Computer Science');

-- Insert demo notes
INSERT INTO notes (title, subject, year, branch, description, upload_date, uploaded_by) VALUES
('Arrays and Linked Lists', 'Data Structures',     '2nd Year', 'Computer Science', 'Fundamental data structures with examples', '2026-02-20', 'Teacher'),
('SQL Joins & Queries',     'Database Management', '2nd Year', 'Computer Science', 'Complete guide to SQL joins',               '2026-03-01', 'Teacher');

-- ============================================================
-- VERIFY: Check all tables have data
-- ============================================================
SELECT 'users'      AS table_name, COUNT(*) AS total_rows FROM users
UNION ALL
SELECT 'attendance' AS table_name, COUNT(*) AS total_rows FROM attendance
UNION ALL
SELECT 'exams'      AS table_name, COUNT(*) AS total_rows FROM exams
UNION ALL
SELECT 'notes'      AS table_name, COUNT(*) AS total_rows FROM notes;

-- ============================================================
-- Done! Your SmartEdu database is ready.
-- Login credentials:
--   Admin:   ADMIN001   / admin@123
--   Teacher: TCH2026001 / teach@123
--   Student: STU2026001 / stud@123
-- ============================================================