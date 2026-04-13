@echo off
echo =========================================
echo   SmartEdu - Starting Local Backend
echo =========================================

:: -----------------------------------------------
:: Railway MySQL (Public URL) - shared cloud DB
:: -----------------------------------------------
set DB_URL=jdbc:mysql://turntable.proxy.rlwy.net:16422/railway?allowPublicKeyRetrieval=true^&useSSL=false
set DB_USER=root
set DB_PASSWORD=HTmsspvYJQbsoqVSPFbQUb8rVoxEEyKV

:: -----------------------------------------------
:: Download MySQL JDBC jar if not present
:: -----------------------------------------------
set LIB_DIR=lib
set MYSQL_JAR=%LIB_DIR%\mysql-connector-j-8.0.33.jar
set MYSQL_URL=https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

if not exist "%MYSQL_JAR%" (
    echo Downloading MySQL JDBC driver...
    powershell -Command "Invoke-WebRequest -Uri '%MYSQL_URL%' -OutFile '%MYSQL_JAR%'"
    if not exist "%MYSQL_JAR%" (
        echo ERROR: Download failed. Check your internet connection.
        pause
        exit /b 1
    )
    echo MySQL driver downloaded!
)

:: -----------------------------------------------
:: Compile
:: -----------------------------------------------
echo Compiling...
if not exist "out" mkdir out

javac -cp "%MYSQL_JAR%" -d out ^
  src/main/java/com/smartedu/model/User.java ^
  src/main/java/com/smartedu/model/Attendance.java ^
  src/main/java/com/smartedu/model/Exam.java ^
  src/main/java/com/smartedu/model/Note.java ^
  src/main/java/com/smartedu/util/Json.java ^
  src/main/java/com/smartedu/db/Database.java ^
  src/main/java/com/smartedu/handler/BaseHandler.java ^
  src/main/java/com/smartedu/handler/AuthHandler.java ^
  src/main/java/com/smartedu/handler/UserHandler.java ^
  src/main/java/com/smartedu/handler/AttendanceHandler.java ^
  src/main/java/com/smartedu/handler/ExamHandler.java ^
  src/main/java/com/smartedu/handler/NoteHandler.java ^
  src/main/java/com/smartedu/server/Main.java

if %errorlevel% neq 0 (
    echo BUILD FAILED.
    pause
    exit /b 1
)

:: -----------------------------------------------
:: Run
:: -----------------------------------------------
echo Build successful! Starting server...
echo Backend available at: http://localhost:8080
echo Press Ctrl+C to stop.
echo =========================================
java -cp "out;%MYSQL_JAR%" com.smartedu.server.Main
pause
