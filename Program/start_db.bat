@echo off
echo =========================================================================
echo    Starting MariaDB Server (KEC R2024 Evaluation System)
echo =========================================================================

tasklist /FI "IMAGENAME eq mariadbd.exe" 2>NUL | find /I /N "mariadbd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MariaDB is already running on port 3306!
) else (
    start "" "C:\Users\suman\.mariadb\mariadb-10.11.8-winx64\bin\mariadbd.exe" --defaults-file="C:\Users\suman\.mariadb\data\my.ini"
    timeout /t 3 /nobreak >nul
    echo [SUCCESS] MariaDB server started successfully in the background.
)

echo You can now refresh MySQL Workbench and run the application!
pause
