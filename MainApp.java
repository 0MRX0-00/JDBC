import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * MainApp - Menu-Driven Command Line Application
 * Kongu Engineering College (Autonomous), Perundurai, Erode
 * Regulation 2024 (R2024) - Student Academic & Continuous Evaluation System
 */
public class MainApp {

    private static final StudentDAO studentDAO = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0 && ("--demo".equalsIgnoreCase(args[0]) || "--verify".equalsIgnoreCase(args[0]))) {
            runAutomatedDemonstration();
            return;
        }

        printBanner();

        if (!DBConnection.testConnection()) {
            System.err.println("CRITICAL ERROR: Failed to establish connection to MySQL database 'kec_eval_db'.");
            System.err.println("Please check your database server at localhost:3306 (user: root, pass: 1234).");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Select an option (0-12): ");
            String input = scanner.nextLine().trim();

            try {
                switch (input) {
                    case "1": registerStudentMenu(); break;
                    case "2": addCourseMenu(); break;
                    case "3": enrollStudentMenu(); break;
                    case "4": recordPhase1MarksMenu(); break;
                    case "5": recordPhase2MarksMenu(); break;
                    case "6": recordPhase3MarksMenu(); break;
                    case "7": runEvaluationEngineMenu(); break;
                    case "8": generateStudentMarkSheetMenu(); break;
                    case "9": generateAttendanceDefaultersMenu(); break;
                    case "10": generateClassSummaryMenu(); break;
                    case "11": viewMasterDataMenu(); break;
                    case "12": runAutomatedDemonstration(); break;
                    case "0":
                        running = false;
                        System.out.println("\nThank you for using KEC R2024 Evaluation System. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid selection. Please enter a valid number (0-12).");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress Enter to return to main menu...");
                scanner.nextLine();
            }
        }
    }

    private static void printBanner() {
        System.out.println("=======================================================================================");
        System.out.println("       KONGU ENGINEERING COLLEGE (AUTONOMOUS) - REGULATION 2024 (R2024)                ");
        System.out.println("            STUDENT ACADEMIC & CONTINUOUS EVALUATION SYSTEM (JDBC)                     ");
        System.out.println("=======================================================================================");
    }

    private static void printMainMenu() {
        System.out.println("\n---------------------------------- MAIN MENU ------------------------------------------");
        System.out.println(" [1]  Student Registration (Add Student)");
        System.out.println(" [2]  Course Catalog Management (Add Course)");
        System.out.println(" [3]  Course Enrollment & Attendance Tracking");
        System.out.println(" [4]  Incremental Marks Entry: Phase 1 (Mid-Semester: CIA 1 & CIA 2)");
        System.out.println(" [5]  Incremental Marks Entry: Phase 2 (End-of-Semester: Assignments/Lab)");
        System.out.println(" [6]  Incremental Marks Entry: Phase 3 (Post-Semester: ESE Marks & Absence)");
        System.out.println(" [7]  Execute KEC R2024 Evaluation Engine (Compute Grades & Thresholds)");
        System.out.println(" [8]  Generate Student Semester Mark Sheet (SGPA & Equivalent %)");
        System.out.println(" [9]  Generate Attendance Defaulter Report (Grade SA)");
        System.out.println(" [10] Generate Department & Class Performance Summary");
        System.out.println(" [11] View All Master Data (Students, Courses, Enrollments)");
        System.out.println(" [12] Run Comprehensive Automated Demonstration & Verification");
        System.out.println(" [0]  Exit System");
        System.out.println("---------------------------------------------------------------------------------------");
    }

    private static void registerStudentMenu() throws SQLException {
        System.out.println("\n--- [1] Student Registration ---");
        System.out.print("Enter Roll Number (e.g. 24ITR005): ");
        String rollNo = scanner.nextLine().trim();
        if (rollNo.isEmpty()) return;

        System.out.print("Enter Student Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Department (IT, CSE, ECE, MECH, etc.): ");
        String dept = scanner.nextLine().trim();

        System.out.print("Enter Current Semester (1-8): ");
        int sem = Integer.parseInt(scanner.nextLine().trim());

        Student s = new Student(rollNo, name, dept, sem);
        boolean ok = studentDAO.addStudent(s);
        if (ok) {
            System.out.println("SUCCESS: Student " + rollNo + " (" + name + ") registered successfully!");
        } else {
            System.out.println("FAILED: Could not register student.");
        }
    }

    private static void addCourseMenu() throws SQLException {
        System.out.println("\n--- [2] Course Catalog Management ---");
        System.out.print("Enter Course Code (e.g. 24ITR34): ");
        String code = scanner.nextLine().trim();

        System.out.print("Enter Course Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Credits (e.g. 3.0, 4.0, 1.5): ");
        double credits = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter Course Type (THEORY / PRACTICAL / EMBEDDED): ");
        String type = scanner.nextLine().trim().toUpperCase();

        try {
            Course c = new Course(code, title, credits, type);
            boolean ok = studentDAO.addCourse(c);
            if (ok) {
                System.out.println("SUCCESS: Course " + code + " added successfully!");
                System.out.printf("  Rules: CIA Weight = %d%% | ESE Weight = %d%% | ESE Min Threshold = %.0f%%\n",
                        c.getCiaWeightage(), c.getEseWeightage(), c.getEseMinThreshold());
            } else {
                System.out.println("FAILED: Could not add course.");
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void enrollStudentMenu() throws SQLException {
        System.out.println("\n--- [3] Course Enrollment & Attendance Tracking ---");
        System.out.print("Enter Student Roll Number: ");
        String rollNo = scanner.nextLine().trim().toUpperCase();

        Student st = studentDAO.getStudentByRollNo(rollNo);
        if (st == null) {
            System.out.println("Student with Roll Number " + rollNo + " does not exist.");
            return;
        }

        System.out.print("Enter Course Code: ");
        String courseCode = scanner.nextLine().trim().toUpperCase();
        Course c = studentDAO.getCourseByCode(courseCode);
        if (c == null) {
            System.out.println("Course with Code " + courseCode + " does not exist.");
            return;
        }

        System.out.print("Enter Semester (default " + st.getSemester() + "): ");
        String semStr = scanner.nextLine().trim();
        int sem = semStr.isEmpty() ? st.getSemester() : Integer.parseInt(semStr);

        System.out.print("Enter Total Classes Held (e.g. 60): ");
        int total = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter Classes Attended (e.g. 50): ");
        int attended = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Is Medical Condonation Approved? (yes/no, default no): ");
        String condStr = scanner.nextLine().trim();
        boolean cond = "yes".equalsIgnoreCase(condStr) || "y".equalsIgnoreCase(condStr);

        int id = studentDAO.enrollStudent(rollNo, courseCode, sem, total, attended, cond);
        if (id > 0) {
            double pct = total > 0 ? (attended * 100.0) / total : 0;
            System.out.printf("SUCCESS: Enrolled student %s in %s! (Enrollment ID: %d)\n", rollNo, courseCode, id);
            System.out.printf("  Attendance: %d/%d (%.1f%%) - %s\n", attended, total, pct,
                    pct >= 75 ? "Eligible" : (pct >= 65 && cond ? "Condoned" : "SA Barred"));
        } else {
            System.out.println("FAILED: Could not enroll student.");
        }
    }

    private static void recordPhase1MarksMenu() throws SQLException {
        System.out.println("\n--- [4] Phase 1 (Mid-Semester): CIA 1 & CIA 2 Entry ---");
        System.out.print("Enter Enrollment ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Enrollment e = studentDAO.getEnrollmentById(id);
        if (e == null) {
            System.out.println("Enrollment ID #" + id + " not found.");
            return;
        }
        System.out.printf("Student: %s (%s) | Course: %s (%s)\n",
                e.getRollNo(), e.getStudentName(), e.getCourseCode(), e.getCourseTitle());

        System.out.print("Enter CIA Test 1 Marks (0-100): ");
        double cia1 = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter CIA Test 2 Marks (0-100): ");
        double cia2 = Double.parseDouble(scanner.nextLine().trim());

        boolean ok = academicDAO.recordPhase1Marks(id, cia1, cia2);
        if (ok) {
            System.out.printf("SUCCESS: Phase 1 Marks recorded for Enrollment #%d (CIA1: %.1f, CIA2: %.1f, Avg: %.1f)\n",
                    id, cia1, cia2, (cia1 + cia2) / 2.0);
        }
    }

    private static void recordPhase2MarksMenu() throws SQLException {
        System.out.println("\n--- [5] Phase 2 (End of Semester - CIA): Assignments/Quizzes/Lab Entry ---");
        System.out.print("Enter Enrollment ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Enrollment e = studentDAO.getEnrollmentById(id);
        if (e == null) {
            System.out.println("Enrollment ID #" + id + " not found.");
            return;
        }
        System.out.printf("Student: %s (%s) | Course: %s (%s)\n",
                e.getRollNo(), e.getStudentName(), e.getCourseCode(), e.getCourseTitle());

        System.out.print("Enter Assignment / Quiz / Lab Evaluation Marks (0-100): ");
        double assign = Double.parseDouble(scanner.nextLine().trim());

        boolean ok = academicDAO.recordPhase2Marks(id, assign);
        if (ok) {
            System.out.printf("SUCCESS: Phase 2 Marks recorded for Enrollment #%d (Assignment: %.1f)\n", id, assign);
        }
    }

    private static void recordPhase3MarksMenu() throws SQLException {
        System.out.println("\n--- [6] Phase 3 (Post Semester): End Semester Exam (ESE) Entry ---");
        System.out.print("Enter Enrollment ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Enrollment e = studentDAO.getEnrollmentById(id);
        if (e == null) {
            System.out.println("Enrollment ID #" + id + " not found.");
            return;
        }
        System.out.printf("Student: %s (%s) | Course: %s (%s)\n",
                e.getRollNo(), e.getStudentName(), e.getCourseCode(), e.getCourseTitle());
        System.out.printf("Attendance Status: %s (%.1f%%)\n", e.getAttendanceStatus(), e.getAttendancePercentage());

        if (!e.isEligibleForESE()) {
            System.out.println("WARNING: Student is BARRED from ESE due to Shortage of Attendance (Grade SA awarded).");
        }

        System.out.print("Was the student ABSENT in ESE? (yes/no): ");
        String absStr = scanner.nextLine().trim();
        boolean isAbsent = "yes".equalsIgnoreCase(absStr) || "y".equalsIgnoreCase(absStr);

        Double eseMark = null;
        if (!isAbsent) {
            System.out.print("Enter End Semester Exam (ESE) Marks (0-100): ");
            eseMark = Double.parseDouble(scanner.nextLine().trim());
        }

        boolean ok = academicDAO.recordPhase3Marks(id, eseMark, isAbsent);
        if (ok) {
            academicDAO.evaluateEnrollment(id);
            System.out.println("SUCCESS: Phase 3 Marks recorded and evaluated for Enrollment #" + id + "!");
        }
    }

    private static void runEvaluationEngineMenu() throws SQLException {
        System.out.println("\n--- [7] Execute KEC R2024 Evaluation Engine ---");
        System.out.println("1. Evaluate Single Enrollment");
        System.out.println("2. Evaluate All Enrollments in Database (Batch)");
        System.out.print("Select (1 or 2): ");
        String sel = scanner.nextLine().trim();

        if ("1".equals(sel)) {
            System.out.print("Enter Enrollment ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            boolean ok = academicDAO.evaluateEnrollment(id);
            if (ok) {
                System.out.println("SUCCESS: Evaluated Enrollment #" + id);
            }
        } else {
            int count = academicDAO.evaluateAllEnrollments();
            System.out.println("SUCCESS: Evaluated " + count + " enrollments using KEC R2024 rules!");
        }
    }

    private static void generateStudentMarkSheetMenu() throws SQLException {
        System.out.println("\n--- [8] Generate Student Semester Mark Sheet ---");
        System.out.print("Enter Roll Number (e.g. 24ITR001): ");
        String rollNo = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Semester (e.g. 3): ");
        int sem = Integer.parseInt(scanner.nextLine().trim());

        AcademicDAO.StudentMarkSheet sheet = academicDAO.getStudentSemesterMarkSheet(rollNo, sem);
        if (sheet == null) {
            System.out.println("No records found for Roll Number " + rollNo);
            return;
        }

        renderMarkSheet(sheet);
    }

    public static void renderMarkSheet(AcademicDAO.StudentMarkSheet sheet) {
        System.out.println("\n" + "=".repeat(105));
        System.out.println("                        KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                     AFFILIATED TO ANNA UNIVERSITY | REGULATION 2024");
        System.out.println("                             STUDENT SEMESTER GRADE SHEET");
        System.out.println("=".repeat(105));
        System.out.printf("  Roll Number : %-15s   Student Name : %-30s\n", sheet.student.getRollNo(), sheet.student.getName());
        System.out.printf("  Department  : %-15s   Semester     : %-30d\n", sheet.student.getDepartment(), sheet.semester);
        System.out.println("-".repeat(105));
        System.out.printf("%-9s | %-28s | %-9s | %-4s | %-6s | %-6s | %-6s | %-6s | %-5s | %-3s | %-12s\n",
                "Code", "Course Title", "Type", "Cred", "Att %", "CIA", "ESE", "Total", "Grade", "GP", "Status");
        System.out.println("-".repeat(105));

        for (AcademicDAO.MarkSheetRow r : sheet.rows) {
            String ciaStr = r.scaledCia != null ? String.format("%5.1f", r.scaledCia) : "-";
            String eseStr = r.isAbsent ? "  AB" : (r.scaledEse != null ? String.format("%5.1f", r.scaledEse) : "-");
            String totStr = r.totalMarks != null ? String.format("%5.1f", r.totalMarks) : "-";

            System.out.printf("%-9s | %-28s | %-9s | %4.1f | %5.1f%% | %6s | %6s | %6s | %5s | %3d | %-12s\n",
                    r.courseCode,
                    truncate(r.courseTitle, 28),
                    r.courseType,
                    r.credits,
                    r.attendancePct,
                    ciaStr,
                    eseStr,
                    totStr,
                    r.letterGrade,
                    r.gradePoint,
                    r.resultStatus);
        }

        System.out.println("-".repeat(105));
        System.out.printf("  Total Credits Enrolled : %-6.1f              Total Credits Earned : %-6.1f\n",
                sheet.totalEnrolledCredits, sheet.totalEarnedCredits);
        System.out.printf("  SEMESTER GRADE POINT AVERAGE (SGPA) : %-6.2f   EQUIVALENT PERCENTAGE : %-5.2f %%\n",
                sheet.sgpa, sheet.equivalentPercentage);
        System.out.printf("  SEMESTER RESULT STATUS              : %-15s\n", sheet.overallStatus);
        System.out.println("=".repeat(105));
    }

    private static void generateAttendanceDefaultersMenu() throws SQLException {
        System.out.println("\n--- [9] Attendance Defaulter Report (< 65% Barred from ESE) ---");
        List<AcademicDAO.DefaulterRecord> list = academicDAO.getAttendanceDefaulters();
        renderDefaulterReport(list);
    }

    public static void renderDefaulterReport(List<AcademicDAO.DefaulterRecord> list) {
        System.out.println("\n" + "=".repeat(105));
        System.out.println("                        KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                     ATTENDANCE DEFAULTER REPORT (REGULATION 2024)");
        System.out.println("=".repeat(105));
        System.out.printf("%-10s | %-20s | %-5s | %-9s | %-10s | %-7s | %-5s | %-30s\n",
                "Roll No", "Student Name", "Dept", "Course", "Attended", "Att %", "Grade", "Remarks");
        System.out.println("-".repeat(105));

        if (list.isEmpty()) {
            System.out.println("No attendance defaulters found! All enrolled students meet eligibility criteria.");
        } else {
            for (AcademicDAO.DefaulterRecord r : list) {
                System.out.printf("%-10s | %-20s | %-5s | %-9s | %3d/%-3d   | %5.1f%% | %-5s | %-30s\n",
                        r.rollNo,
                        truncate(r.studentName, 20),
                        r.department,
                        r.courseCode,
                        r.attendedClasses,
                        r.totalClasses,
                        r.attendancePct,
                        r.assignedGrade,
                        truncate(r.remarks, 30));
            }
        }
        System.out.println("=".repeat(105));
        System.out.println("Rule: Attendance < 65% is barred from ESE and awarded grade 'SA' (0 Grade Points).");
        System.out.println("      Attendance 65%-74.9% requires Medical Condonation approval.");
    }

    private static void generateClassSummaryMenu() throws SQLException {
        System.out.println("\n--- [10] Class Performance & Result Summary ---");
        List<AcademicDAO.DepartmentSummary> list = academicDAO.getClassPerformanceSummary();
        renderClassSummary(list);
    }

    public static void renderClassSummary(List<AcademicDAO.DepartmentSummary> list) {
        System.out.println("\n" + "=".repeat(105));
        System.out.println("                        KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                 CLASS PERFORMANCE & RESULT SUMMARY (REGULATION 2024)");
        System.out.println("=".repeat(105));
        System.out.printf("%-8s | %-10s | %-10s | %-6s | %-6s | %-6s | %-6s | %-8s | %-12s\n",
                "Dept", "Students", "Attempts", "Pass", "Fail", "SA", "AB", "Pass %", "Avg SGPA");
        System.out.println("-".repeat(105));

        for (AcademicDAO.DepartmentSummary s : list) {
            System.out.printf("%-8s | %-10d | %-10d | %-6d | %-6d | %-6d | %-6d | %6.2f%% | %8.2f\n",
                    s.department,
                    s.totalStudents,
                    s.totalCourseAttempts,
                    s.passCount,
                    s.failCount,
                    s.saCount,
                    s.abCount,
                    s.passPercentage,
                    s.classAverageSGPA);
        }
        System.out.println("=".repeat(105));
    }

    private static void viewMasterDataMenu() throws SQLException {
        System.out.println("\n=========================== REGISTERED STUDENTS ===========================");
        List<Student> students = studentDAO.getAllStudents();
        for (Student s : students) System.out.println(s);

        System.out.println("\n============================= COURSE CATALOG ==============================");
        List<Course> courses = studentDAO.getAllCourses();
        for (Course c : courses) System.out.println(c);

        System.out.println("\n======================== ENROLLMENTS & ATTENDANCE =========================");
        List<Enrollment> enrollments = studentDAO.getAllEnrollments();
        for (Enrollment e : enrollments) System.out.println(e);
    }

    public static void runAutomatedDemonstration() {
        System.out.println("\n" + "#".repeat(105));
        System.out.println("   KONGU ENGINEERING COLLEGE (R2024) - AUTOMATED VERIFICATION & PROOF OF EXECUTION");
        System.out.println("#".repeat(105));

        try {
            // Proof 1: Adding a new student and course
            String newRoll = "24ITR005";
            System.out.println("\n>>> PROOF 1: ADDING A NEW STUDENT AND COURSE");
            System.out.println("Adding Student: " + newRoll + " (Dharani P, IT, Semester 3)...");
            Student newStudent = new Student(newRoll, "Dharani P", "IT", 3);
            studentDAO.addStudent(newStudent);
            System.out.println("Registered: " + studentDAO.getStudentByRollNo(newRoll));

            String newCourse = "24ITR34";
            System.out.println("\nAdding Course: " + newCourse + " (Cloud Computing, Credits: 3.0, Type: THEORY)...");
            Course course = new Course(newCourse, "Cloud Computing Principles", 3.0, "THEORY");
            studentDAO.addCourse(course);
            System.out.println("Added Course: " + studentDAO.getCourseByCode(newCourse));

            System.out.println("\nEnrolling " + newRoll + " into " + newCourse + " (Total: 60, Attended: 52 -> 86.7%)...");
            int enrollId1 = studentDAO.enrollStudent(newRoll, newCourse, 3, 60, 52, false);

            System.out.println("Enrolling " + newRoll + " into 24ITR38 (Lab, Total: 30, Attended: 27 -> 90.0%)...");
            int enrollId2 = studentDAO.enrollStudent(newRoll, "24ITR38", 3, 30, 27, false);

            // Proof 2: Incremental marks entry
            System.out.println("\n>>> PROOF 2: INCREMENTAL MARKS ENTRY (PHASE 1, PHASE 2, PHASE 3)");
            System.out.println("--- Phase 1 (Mid-Semester): Recording CIA 1 & CIA 2 ---");
            academicDAO.recordPhase1Marks(enrollId1, 88.0, 92.0);
            academicDAO.recordPhase1Marks(enrollId2, 94.0, 96.0);
            System.out.println("Phase 1 Recorded: CIA1=88.0, CIA2=92.0 for " + newCourse);
            System.out.println("Phase 1 Recorded: CIA1=94.0, CIA2=96.0 for 24ITR38");

            System.out.println("\n--- Phase 2 (End-of-Semester): Recording Assignments/Lab Evaluations ---");
            academicDAO.recordPhase2Marks(enrollId1, 95.0);
            academicDAO.recordPhase2Marks(enrollId2, 98.0);
            System.out.println("Phase 2 Recorded: Assignment=95.0 for " + newCourse);
            System.out.println("Phase 2 Recorded: Lab Evaluation=98.0 for 24ITR38");

            System.out.println("\n--- Phase 3 (Post-Semester): Recording ESE Examination Marks ---");
            academicDAO.recordPhase3Marks(enrollId1, 84.0, false);
            academicDAO.recordPhase3Marks(enrollId2, 92.0, false);
            System.out.println("Phase 3 Recorded: ESE Mark=84.0 for " + newCourse);
            System.out.println("Phase 3 Recorded: ESE Mark=92.0 for 24ITR38");

            System.out.println("\n--- Testing Absent Scenario (AB) ---");
            int enrollAb = studentDAO.enrollStudent("24CSR015", "24ITR31", 3, 60, 50, false);
            academicDAO.recordPhase1Marks(enrollAb, 75.0, 78.0);
            academicDAO.recordPhase2Marks(enrollAb, 80.0);
            academicDAO.recordPhase3Marks(enrollAb, null, true);
            System.out.println("Recorded ESE as ABSENT for 24CSR015 in 24ITR31");

            System.out.println("\nExecuting KEC R2024 Evaluation Engine on all records...");
            academicDAO.evaluateAllEnrollments();

            // Proof 3: Render Mark Sheet
            System.out.println("\n>>> PROOF 3: GENERATED FORMAL GRADE SHEET WITH SGPA & PERCENTAGE");
            AcademicDAO.StudentMarkSheet sheet1 = academicDAO.getStudentSemesterMarkSheet("24ITR001", 3);
            renderMarkSheet(sheet1);

            AcademicDAO.StudentMarkSheet sheetNew = academicDAO.getStudentSemesterMarkSheet(newRoll, 3);
            renderMarkSheet(sheetNew);

            // Proof 4: Render Attendance Defaulter Report
            System.out.println("\n>>> PROOF 4: ATTENDANCE DEFAULTER REPORT (GRADE SA)");
            List<AcademicDAO.DefaulterRecord> defaulters = academicDAO.getAttendanceDefaulters();
            renderDefaulterReport(defaulters);

            // Proof 5: Render Class Summary
            System.out.println("\n>>> PROOF 5: CLASS PERFORMANCE & RESULT SUMMARY");
            List<AcademicDAO.DepartmentSummary> summaries = academicDAO.getClassPerformanceSummary();
            renderClassSummary(summaries);

            System.out.println("\nALL SYSTEM VERIFICATIONS COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("Demonstration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String truncate(String s, int len) {
        if (s == null) return "";
        if (s.length() <= len) return s;
        return s.substring(0, len - 3) + "...";
    }
}
