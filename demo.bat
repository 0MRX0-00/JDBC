@echo off
echo ========================================================
echo Running KEC R2024 Automated Verification Suite...
echo ========================================================
java -cp ".;lib\mysql-connector-j-8.3.0.jar" MainApp --demo
pause
