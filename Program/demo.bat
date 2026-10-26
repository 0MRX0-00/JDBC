@echo off
tasklist /FI "IMAGENAME eq mariadbd.exe" 2>NUL | find /I /N "mariadbd.exe">NUL
if not "%ERRORLEVEL%"=="0" (
    echo Starting MariaDB service...
    start "" "C:\Users\suman\.mariadb\mariadb-10.11.8-winx64\bin\mariadbd.exe" --defaults-file="C:\Users\suman\.mariadb\data\my.ini"
    timeout /t 3 /nobreak >nul
)
echo ========================================================
echo Running KEC R2024 Automated Verification Suite...
echo ========================================================
java -cp ".;lib\mysql-connector-j-8.3.0.jar" MainApp --demo
pause

