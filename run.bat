@echo off
echo =========================================
echo   SmartEdu - Compiling Java Backend...
echo =========================================

if not exist "out" mkdir out

javac -d out src/main/java/com/smartedu/server/Main.java src/main/java/com/smartedu/db/Database.java src/main/java/com/smartedu/model/User.java src/main/java/com/smartedu/model/Attendance.java src/main/java/com/smartedu/model/Exam.java src/main/java/com/smartedu/model/Note.java src/main/java/com/smartedu/util/Json.java src/main/java/com/smartedu/handler/BaseHandler.java src/main/java/com/smartedu/handler/AuthHandler.java src/main/java/com/smartedu/handler/UserHandler.java src/main/java/com/smartedu/handler/AttendanceHandler.java src/main/java/com/smartedu/handler/ExamHandler.java src/main/java/com/smartedu/handler/NoteHandler.java

if %errorlevel% neq 0 (
    echo BUILD FAILED. Make sure Java 17 is installed.
    pause
    exit /b 1
)

echo Build successful! Starting server...
java -cp out com.smartedu.server.Main
pause
