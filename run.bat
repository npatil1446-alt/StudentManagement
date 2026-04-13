@echo off
echo =========================================
echo   SmartEdu - Starting Local Backend
echo =========================================

:: -----------------------------------------------
:: Railway MySQL (Public URL) - shared cloud DB
:: -----------------------------------------------
set DB_URL=jdbc:mysql://turntable.proxy.rlwy.net:16422/railway?allowPublicKeyRetrieval=true&useSSL=false
set DB_USER=root
set DB_PASSWORD=HTmsspvYJQbsoqVSPFbQUb8rVoxEEyKV

:: -----------------------------------------------
:: Auto-download Maven if not installed
:: -----------------------------------------------
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven not found. Downloading Maven 3.9.6 locally...
    if not exist ".mvn-local" mkdir .mvn-local
    if not exist ".mvn-local\apache-maven-3.9.6" (
        powershell -Command "Invoke-WebRequest -Uri 'https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '.mvn-local\maven.zip'"
        powershell -Command "Expand-Archive -Path '.mvn-local\maven.zip' -DestinationPath '.mvn-local' -Force"
        del .mvn-local\maven.zip
        echo Maven downloaded successfully!
    ) else (
        echo Using cached local Maven...
    )
    set MVN_CMD=.mvn-local\apache-maven-3.9.6\bin\mvn.cmd
) else (
    set MVN_CMD=mvn
)

:: -----------------------------------------------
:: Build fat JAR
:: -----------------------------------------------
echo Building project...
call %MVN_CMD% -B -DskipTests clean package -q

if %errorlevel% neq 0 (
    echo BUILD FAILED. Check the errors above.
    pause
    exit /b 1
)

echo Build successful! Starting server...
echo Backend will be available at: http://localhost:8080
echo Press Ctrl+C to stop the server.
echo =========================================
java -jar target\smartedu-backend-1.0.0.jar
pause
