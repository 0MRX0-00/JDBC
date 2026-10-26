@echo off
echo ========================================================
echo Compiling KEC R2024 Evaluation System (Java JDBC)...
echo ========================================================
javac -cp ".;lib\mysql-connector-j-8.3.0.jar" -d . src\*.java
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed successfully!
) else (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ is installed.
)
pause
