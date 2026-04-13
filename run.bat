@echo off
echo =========================================
echo   SmartEdu - Building with Maven...
echo =========================================

:: -----------------------------------------------
:: Railway MySQL credentials (Public URL)
:: These point your LOCAL run to the Railway cloud DB
:: so both local and deployed backend share one DB.
:: -----------------------------------------------
set DB_URL=jdbc:mysql://turntable.proxy.rlwy.net:16422/railway?allowPublicKeyRetrieval=true^&useSSL=false
set DB_USER=root
set DB_PASSWORD=HTmsspvYJQbsoqVSPFbQUb8rVoxEEyKV

:: Build fat JAR with Maven
call mvn -B -DskipTests clean package

if %errorlevel% neq 0 (
    echo BUILD FAILED. Make sure Maven and Java 17+ are installed.
    pause
    exit /b 1
)

echo Build successful! Starting server...
java -jar target\smartedu-backend-1.0.0.jar
pause
