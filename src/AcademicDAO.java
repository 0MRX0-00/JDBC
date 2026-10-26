import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AcademicDAO - Data Access Object and Evaluation Rules Engine.
 * Implements Kongu Engineering College (Autonomous) - Regulation 2024 (R2024).
 */
public class AcademicDAO {

    private final StudentDAO studentDAO = new StudentDAO();

    // =========================================================================
    // 1. Phased / Incremental Marks Entry
    // =========================================================================

    public boolean recordPhase1Marks(int enrollmentId, double cia1, double cia2) throws SQLException {
        if (cia1 < 0 || cia1 > 100 || cia2 < 0 || cia2 > 100) {
            throw new IllegalArgumentException("CIA marks must be within range 0.0 to 100.0");
        }
        String sql = "INSERT INTO assessment_marks (enrollment_id, cia1, cia2) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE cia1 = VALUES(cia1), cia2 = VALUES(cia2)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.setDouble(2, cia1);
            stmt.setDouble(3, cia2);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean recordPhase2Marks(int enrollmentId, double assignment) throws SQLException {
        if (assignment < 0 || assignment > 100) {
            throw new IllegalArgumentException("Assignment mark must be within range 0.0 to 100.0");
        }

        AssessmentMarks existing = getAssessmentMarksByEnrollment(enrollmentId);
        Double ciaTotal = null;
        if (existing != null && existing.getCia1() != null && existing.getCia2() != null) {
            double testAvg = (existing.getCia1() + existing.getCia2()) / 2.0;
            ciaTotal = Math.round(((testAvg * 0.70) + (assignment * 0.30)) * 100.0) / 100.0;
        } else {
            ciaTotal = assignment;
        }

        String sql = "INSERT INTO assessment_marks (enrollment_id, assignment, cia_total) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE assignment = VALUES(assignment), cia_total = VALUES(cia_total)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.setDouble(2, assignment);
            stmt.setDouble(3, ciaTotal);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean recordPhase3Marks(int enrollmentId, Double eseMark, boolean isAbsent) throws SQLException {
        if (!isAbsent && (eseMark == null || eseMark < 0 || eseMark > 100)) {
            throw new IllegalArgumentException("ESE mark must be within range 0.0 to 100.0 when student is not absent");
        }

        String sql = "INSERT INTO assessment_marks (enrollment_id, ese_mark, is_absent) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE ese_mark = VALUES(ese_mark), is_absent = VALUES(is_absent)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            if (isAbsent || eseMark == null) {
                stmt.setNull(2, Types.DECIMAL);
            } else {
                stmt.setDouble(2, eseMark);
            }
            stmt.setBoolean(3, isAbsent);
            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 2. KEC R2024 Evaluation Engine
    // =========================================================================

    public boolean evaluateEnrollment(int enrollmentId) throws SQLException {
        Enrollment enrollment = studentDAO.getEnrollmentById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment ID #" + enrollmentId + " not found.");
        }

        Course course = studentDAO.getCourseByCode(enrollment.getCourseCode());
        if (course == null) {
            throw new IllegalArgumentException("Course " + enrollment.getCourseCode() + " not found.");
        }

        AssessmentMarks marks = getAssessmentMarksByEnrollment(enrollmentId);
        if (marks == null) {
            marks = new AssessmentMarks();
            marks.setEnrollmentId(enrollmentId);
        }

        double attendancePct = enrollment.getAttendancePercentage();
        boolean condoned = enrollment.isCondonationApproved();

        // 1. Attendance Filter
        if (attendancePct < 65.0) {
            marks.setScaledCia(0.0);
            marks.setScaledEse(0.0);
            marks.setTotalMarks(0.0);
            marks.setLetterGrade("SA");
            marks.setGradePoint(0);
            marks.setResultStatus("SA (Shortage <65%)");
            saveEvaluationResults(marks);
            return true;
        } else if (attendancePct < 75.0 && !condoned) {
            marks.setScaledCia(0.0);
            marks.setScaledEse(0.0);
            marks.setTotalMarks(0.0);
            marks.setLetterGrade("SA");
            marks.setGradePoint(0);
            marks.setResultStatus("SA (Condonation Req)");
            saveEvaluationResults(marks);
            return true;
        }

        // 2. Absence in ESE
        if (marks.isAbsent()) {
            marks.setScaledEse(0.0);
            marks.setTotalMarks(marks.getScaledCia() != null ? marks.getScaledCia() : 0.0);
            marks.setLetterGrade("AB");
            marks.setGradePoint(0);
            marks.setResultStatus("AB (Absent)");
            saveEvaluationResults(marks);
            return true;
        }

        // 3. Compute Internal Score
        double cia1 = marks.getCia1() != null ? marks.getCia1() : 0.0;
        double cia2 = marks.getCia2() != null ? marks.getCia2() : 0.0;
        double assignment = marks.getAssignment() != null ? marks.getAssignment() : 0.0;

        double testAvg = (cia1 + cia2) / 2.0;
        double ciaTotal = Math.round(((testAvg * 0.70) + (assignment * 0.30)) * 100.0) / 100.0;
        marks.setCiaTotal(ciaTotal);

        double scaledCia = Math.round((ciaTotal * (course.getCiaWeightage() / 100.0)) * 100.0) / 100.0;
        marks.setScaledCia(scaledCia);

        if (marks.getEseMark() == null) {
            marks.setScaledEse(null);
            marks.setTotalMarks(null);
            marks.setLetterGrade("PEND");
            marks.setGradePoint(0);
            marks.setResultStatus("PENDING (ESE Awaited)");
            saveEvaluationResults(marks);
            return true;
        }

        // 4. Scaled ESE and Total
        double rawEse = marks.getEseMark();
        double scaledEse = Math.round((rawEse * (course.getEseWeightage() / 100.0)) * 100.0) / 100.0;
        marks.setScaledEse(scaledEse);

        double totalMarks = Math.round((scaledCia + scaledEse) * 100.0) / 100.0;
        marks.setTotalMarks(totalMarks);

        // 5. Enforce KEC R2024 Thresholds
        double eseMinThreshold = course.getEseMinThreshold();
        double overallPassMin = course.getOverallPassMin();

        if (rawEse < eseMinThreshold) {
            marks.setLetterGrade("RA");
            marks.setGradePoint(0);
            marks.setResultStatus("FAIL (ESE < " + String.format("%.0f", eseMinThreshold) + "%)");
        } else if (totalMarks < overallPassMin) {
            marks.setLetterGrade("RA");
            marks.setGradePoint(0);
            marks.setResultStatus("FAIL (Total < 50)");
        } else {
            marks.setResultStatus("PASS");
            if (totalMarks >= 91.0) {
                marks.setLetterGrade("O");
                marks.setGradePoint(10);
            } else if (totalMarks >= 81.0) {
                marks.setLetterGrade("A+");
                marks.setGradePoint(9);
            } else if (totalMarks >= 71.0) {
                marks.setLetterGrade("A");
                marks.setGradePoint(8);
            } else if (totalMarks >= 61.0) {
                marks.setLetterGrade("B+");
                marks.setGradePoint(7);
            } else {
                marks.setLetterGrade("B");
                marks.setGradePoint(6);
            }
        }

        saveEvaluationResults(marks);
        return true;
    }

    public int evaluateAllEnrollments() throws SQLException {
        List<Enrollment> list = studentDAO.getAllEnrollments();
        int count = 0;
        for (Enrollment e : list) {
            evaluateEnrollment(e.getEnrollmentId());
            count++;
        }
        return count;
    }

    private void saveEvaluationResults(AssessmentMarks m) throws SQLException {
        String sql = "UPDATE assessment_marks SET " +
                     "cia_total = ?, scaled_cia = ?, scaled_ese = ?, total_marks = ?, " +
                     "letter_grade = ?, grade_point = ?, result_status = ? " +
                     "WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setNullableDouble(stmt, 1, m.getCiaTotal());
            setNullableDouble(stmt, 2, m.getScaledCia());
            setNullableDouble(stmt, 3, m.getScaledEse());
            setNullableDouble(stmt, 4, m.getTotalMarks());
            stmt.setString(5, m.getLetterGrade());
            if (m.getGradePoint() != null) {
                stmt.setInt(6, m.getGradePoint());
            } else {
                stmt.setInt(6, 0);
            }
            stmt.setString(7, m.getResultStatus());
            stmt.setInt(8, m.getEnrollmentId());
            stmt.executeUpdate();
        }
    }

    private void setNullableDouble(PreparedStatement stmt, int index, Double val) throws SQLException {
        if (val != null) {
            stmt.setDouble(index, val);
        } else {
            stmt.setNull(index, Types.DECIMAL);
        }
    }

    public AssessmentMarks getAssessmentMarksByEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT mark_id, enrollment_id, cia1, cia2, assignment, cia_total, " +
                     "ese_mark, is_absent, scaled_cia, scaled_ese, total_marks, letter_grade, grade_point, result_status " +
                     "FROM assessment_marks WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AssessmentMarks m = new AssessmentMarks(rs.getInt("mark_id"), rs.getInt("enrollment_id"));
                    m.setCia1(getOptionalDouble(rs, "cia1"));
                    m.setCia2(getOptionalDouble(rs, "cia2"));
                    m.setAssignment(getOptionalDouble(rs, "assignment"));
                    m.setCiaTotal(getOptionalDouble(rs, "cia_total"));
                    m.setEseMark(getOptionalDouble(rs, "ese_mark"));
                    m.setAbsent(rs.getBoolean("is_absent"));
                    m.setScaledCia(getOptionalDouble(rs, "scaled_cia"));
                    m.setScaledEse(getOptionalDouble(rs, "scaled_ese"));
                    m.setTotalMarks(getOptionalDouble(rs, "total_marks"));
                    m.setLetterGrade(rs.getString("letter_grade"));
                    int gp = rs.getInt("grade_point");
                    m.setGradePoint(rs.wasNull() ? null : gp);
                    m.setResultStatus(rs.getString("result_status"));
                    return m;
                }
            }
        }
        return null;
    }

    private Double getOptionalDouble(ResultSet rs, String col) throws SQLException {
        double d = rs.getDouble(col);
        return rs.wasNull() ? null : d;
    }

    // =========================================================================
    // 3. Reporting Data Classes
    // =========================================================================

    public static class MarkSheetRow {
        public String courseCode;
        public String courseTitle;
        public String courseType;
        public double credits;
        public double attendancePct;
        public String attendanceStatus;
        public Double ciaTotal;
        public Double scaledCia;
        public Double rawEse;
        public Double scaledEse;
        public Double totalMarks;
        public String letterGrade;
        public int gradePoint;
        public String resultStatus;
        public boolean isAbsent;
    }

    public static class StudentMarkSheet {
        public Student student;
        public int semester;
        public List<MarkSheetRow> rows = new ArrayList<>();
        public double totalEnrolledCredits = 0.0;
        public double totalEarnedCredits = 0.0;
        public double sumCreditPoints = 0.0;
        public double sgpa = 0.0;
        public double equivalentPercentage = 0.0;
        public String overallStatus = "PASS";
    }

    public StudentMarkSheet getStudentSemesterMarkSheet(String rollNo, int semester) throws SQLException {
        Student student = studentDAO.getStudentByRollNo(rollNo);
        if (student == null) {
            return null;
        }

        List<Enrollment> enrollments = studentDAO.getEnrollmentsByStudent(rollNo, semester);
        for (Enrollment e : enrollments) {
            evaluateEnrollment(e.getEnrollmentId());
        }

        StudentMarkSheet sheet = new StudentMarkSheet();
        sheet.student = student;
        sheet.semester = semester;

        String sql = "SELECT e.enrollment_id, e.course_code, c.title, c.course_type, c.credits, " +
                     "e.total_classes, e.attended_classes, e.condonation_approved, " +
                     "m.cia_total, m.scaled_cia, m.ese_mark, m.is_absent, m.scaled_ese, " +
                     "m.total_marks, m.letter_grade, m.grade_point, m.result_status " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "LEFT JOIN assessment_marks m ON e.enrollment_id = m.enrollment_id " +
                     "WHERE e.roll_no = ? AND e.semester = ? " +
                     "ORDER BY e.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo.trim().toUpperCase());
            stmt.setInt(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MarkSheetRow row = new MarkSheetRow();
                    row.courseCode = rs.getString("course_code");
                    row.courseTitle = rs.getString("title");
                    row.courseType = rs.getString("course_type");
                    row.credits = rs.getDouble("credits");

                    int totalCls = rs.getInt("total_classes");
                    int attCls = rs.getInt("attended_classes");
                    boolean cond = rs.getBoolean("condonation_approved");
                    row.attendancePct = totalCls > 0 ? (attCls * 100.0) / totalCls : 0.0;
                    if (row.attendancePct >= 75.0) {
                        row.attendanceStatus = "Eligible";
                    } else if (row.attendancePct >= 65.0) {
                        row.attendanceStatus = cond ? "Condoned" : "Barred (Condonation Req)";
                    } else {
                        row.attendanceStatus = "Barred (<65% Shortage)";
                    }

                    row.ciaTotal = getOptionalDouble(rs, "cia_total");
                    row.scaledCia = getOptionalDouble(rs, "scaled_cia");
                    row.rawEse = getOptionalDouble(rs, "ese_mark");
                    row.isAbsent = rs.getBoolean("is_absent");
                    row.scaledEse = getOptionalDouble(rs, "scaled_ese");
                    row.totalMarks = getOptionalDouble(rs, "total_marks");
                    row.letterGrade = rs.getString("letter_grade") != null ? rs.getString("letter_grade") : "-";
                    row.gradePoint = rs.getInt("grade_point");
                    row.resultStatus = rs.getString("result_status") != null ? rs.getString("result_status") : "PENDING";

                    sheet.rows.add(row);

                    sheet.totalEnrolledCredits += row.credits;
                    if (row.gradePoint > 0) {
                        sheet.totalEarnedCredits += row.credits;
                    } else {
                        sheet.overallStatus = "RE-APPEARANCE";
                    }
                    sheet.sumCreditPoints += (row.credits * row.gradePoint);
                }
            }
        }

        if (sheet.totalEnrolledCredits > 0) {
            sheet.sgpa = Math.round((sheet.sumCreditPoints / sheet.totalEnrolledCredits) * 100.0) / 100.0;
            sheet.equivalentPercentage = Math.round((sheet.sgpa * 10.0) * 100.0) / 100.0;
        }

        return sheet;
    }

    public static class DefaulterRecord {
        public String rollNo;
        public String studentName;
        public String department;
        public int semester;
        public String courseCode;
        public String courseTitle;
        public int totalClasses;
        public int attendedClasses;
        public double attendancePct;
        public boolean condonationApproved;
        public String assignedGrade;
        public String remarks;
    }

    public List<DefaulterRecord> getAttendanceDefaulters() throws SQLException {
        List<DefaulterRecord> list = new ArrayList<>();
        String sql = "SELECT s.roll_no, s.name, s.department, e.semester, " +
                     "c.course_code, c.title, e.total_classes, e.attended_classes, e.condonation_approved " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_no = s.roll_no " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE (e.attended_classes * 100.0 / e.total_classes) < 75.0 " +
                     "ORDER BY s.roll_no, c.course_code";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int total = rs.getInt("total_classes");
                int att = rs.getInt("attended_classes");
                boolean cond = rs.getBoolean("condonation_approved");
                double pct = total > 0 ? (att * 100.0) / total : 0.0;

                if (pct < 65.0 || !cond) {
                    DefaulterRecord rec = new DefaulterRecord();
                    rec.rollNo = rs.getString("roll_no");
                    rec.studentName = rs.getString("name");
                    rec.department = rs.getString("department");
                    rec.semester = rs.getInt("semester");
                    rec.courseCode = rs.getString("course_code");
                    rec.courseTitle = rs.getString("title");
                    rec.totalClasses = total;
                    rec.attendedClasses = att;
                    rec.attendancePct = Math.round(pct * 100.0) / 100.0;
                    rec.condonationApproved = cond;
                    rec.assignedGrade = "SA";
                    if (pct < 65.0) {
                        rec.remarks = "Critical Shortage (<65%) - Barred from ESE";
                    } else {
                        rec.remarks = "Marginal Shortage (65-74.9%) - Medical Condonation Not Approved";
                    }
                    list.add(rec);
                }
            }
        }
        return list;
    }

    public static class DepartmentSummary {
        public String department;
        public int totalStudents;
        public int totalCourseAttempts;
        public int passCount;
        public int failCount;
        public int saCount;
        public int abCount;
        public double passPercentage;
        public double classAverageSGPA;
    }

    public List<DepartmentSummary> getClassPerformanceSummary() throws SQLException {
        evaluateAllEnrollments();
        List<DepartmentSummary> list = new ArrayList<>();
        String deptSql = "SELECT DISTINCT department FROM students ORDER BY department";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsDept = stmt.executeQuery(deptSql)) {

            List<String> depts = new ArrayList<>();
            while (rsDept.next()) {
                depts.add(rsDept.getString("department"));
            }

            for (String dept : depts) {
                DepartmentSummary sum = new DepartmentSummary();
                sum.department = dept;

                List<Student> students = studentDAO.getStudentsByDepartment(dept);
                sum.totalStudents = students.size();

                String statsSql = "SELECT m.letter_grade, m.result_status " +
                                  "FROM assessment_marks m " +
                                  "JOIN enrollments e ON m.enrollment_id = e.enrollment_id " +
                                  "JOIN students s ON e.roll_no = s.roll_no " +
                                  "WHERE s.department = ?";

                try (PreparedStatement pStat = conn.prepareStatement(statsSql)) {
                    pStat.setString(1, dept);
                    try (ResultSet rsStat = pStat.executeQuery()) {
                        while (rsStat.next()) {
                            sum.totalCourseAttempts++;
                            String grade = rsStat.getString("letter_grade");
                            String status = rsStat.getString("result_status");
                            if ("PASS".equalsIgnoreCase(status)) {
                                sum.passCount++;
                            } else if ("SA".equalsIgnoreCase(grade)) {
                                sum.saCount++;
                                sum.failCount++;
                            } else if ("AB".equalsIgnoreCase(grade)) {
                                sum.abCount++;
                                sum.failCount++;
                            } else {
                                sum.failCount++;
                            }
                        }
                    }
                }

                if (sum.totalCourseAttempts > 0) {
                    sum.passPercentage = Math.round((sum.passCount * 100.0 / sum.totalCourseAttempts) * 100.0) / 100.0;
                }

                double totalSgpaSum = 0.0;
                int evaluatedStudentCount = 0;
                for (Student st : students) {
                    StudentMarkSheet ms = getStudentSemesterMarkSheet(st.getRollNo(), st.getSemester());
                    if (ms != null && ms.totalEnrolledCredits > 0) {
                        totalSgpaSum += ms.sgpa;
                        evaluatedStudentCount++;
                    }
                }

                if (evaluatedStudentCount > 0) {
                    sum.classAverageSGPA = Math.round((totalSgpaSum / evaluatedStudentCount) * 100.0) / 100.0;
                }

                list.add(sum);
            }
        }
        return list;
    }
}
