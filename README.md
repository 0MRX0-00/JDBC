# Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
## Stdent Academic & Continuous Evaluation System (Java JDBC)

A comprehensive, database-driven academic continuous assessment and grading engine developed in **Java** utilizing **JDBC** and **MySQL**. It strictly adheres to the official **Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)** academic regulations.

---

## 1. Project Structure

```
c:\Users\suman\OneDrive\Documents\JDBC\
│
├── src/                           # Java Source Code
│   ├── DBConnection.java          # JDBC Database Connection Manager
│   ├── StudentDAO.java            # Student, Course & Enrollment CRUD Operations
│   ├── AcademicDAO.java           # Evaluation Engine & Reports Generator
│   ├── MainApp.java               # Menu-driven Application & Automated Demo
│   ├── Student.java               # Student Model Entity
│   ├── Course.java                # Course Model Entity (Weights & Thresholds)
│   ├── Enrollment.java            # Course Enrollment & Attendance Tracking
│   └── AssessmentMarks.java       # Phased Marks & Grade Assessment Model
│
├── Document/                      # Assignment & Project Documentation
│   └── sumanths.assignment.docx
│
├── Output/                        # Execution & Verification Screenshots
│
├── kec_schema.sql                 # MySQL Database Creation & Seed Data Script
├── build.bat                      # One-click Windows build script
├── run.bat                        # One-click interactive app launcher
├── demo.bat                       # One-click automated demonstration runner
├── start_db.bat                   # Background MariaDB Service starter
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar  # Official MySQL JDBC Driver
```

---

## 2. Evaluation Rules & Weightage Engine (KEC R2024)

| Course Type | CIA Weightage | ESE Weightage | ESE Minimum Threshold | Overall Pass Minimum |
| :--- | :---: | :---: | :---: | :---: |
| **THEORY** | **40%** | **60%** | **45% (27.0 / 60)** | **50.0 Marks** |
| **PRACTICAL** | **60%** | **40%** | **50% (20.0 / 40)** | **50.0 Marks** |
| **EMBEDDED** | **50%** | **50%** | **45% (22.5 / 50)** | **50.0 Marks** |

### Continuous Internal Assessment (CIA) Normalization
$$\text{Test Average} = \frac{\text{CIA}_1 + \text{CIA}_2}{2}$$
$$\text{CIA}_{\text{Total (100)}} = (\text{Test Average} \times 0.70) + (\text{Assignment / Quiz / Lab} \times 0.30)$$

### Attendance Filter & Shortage of Attendance (SA) Rule
- **$\ge 75.0\%$**: Eligible to sit for End Semester Examination (ESE).
- **$65.0\% \le \text{Attendance} < 75.0\%$**: Conditionally eligible (Requires Medical Condonation approval).
- **$< 65.0\%$**: Award **SA (Shortage of Attendance)** grade (0 Grade Points). Barred from ESE.

### 10-Point Letter Grading System
- **O (91 - 100)**: Grade Point 10
- **A+ (81 - 90)**: Grade Point 9
- **A (71 - 80)**: Grade Point 8
- **B+ (61 - 70)**: Grade Point 7
- **B (50 - 60)**: Grade Point 6
- **RA (Re-Appearance)**: Grade Point 0 (Aggregate $< 50$ or failed ESE minimum)
- **SA (Shortage of Attendance)**: Grade Point 0
- **AB (Absent in ESE)**: Grade Point 0

### SGPA & Equivalent Percentage Formulas
$$\text{SGPA} = \frac{\sum (\text{Course Credits} \times \text{Grade Points Earned})}{\sum (\text{Total Enrolled Credits})}$$
$$\text{Equivalent Percentage} = \text{SGPA} \times 10$$

---

## 3. How to Run the Application

### Prerequisites
- MySQL Server running on `localhost:3306` with user `root` and password `1234`.
- Java JDK 17+ installed.

### Option 1: Quick Batch Execution (Windows)
Double-click or run from PowerShell/CMD:
- `build.bat` - Compiles the application.
- `run.bat` - Launches the interactive terminal menu.
- `demo.bat` - Runs the comprehensive verification demonstration.

### Option 2: Command Line
```powershell
# 1. Compile all Java source files
javac -cp ".;lib\mysql-connector-j-8.3.0.jar" -d . src\*.java

# 2. Run Interactive Menu Application
java -cp ".;lib\mysql-connector-j-8.3.0.jar" MainApp

# 3. Run Automated Demonstration / Verification
java -cp ".;lib\mysql-connector-j-8.3.0.jar" MainApp --demo
```
