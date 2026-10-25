import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDAO - Data Access Object for Student, Course, and Enrollment management.
 * Kongu Engineering College (Autonomous) - Regulation 2024.
 */
public class StudentDAO {

    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, department, semester) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), department = VALUES(department), semester = VALUES(semester)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getRollNo().trim().toUpperCase());
            stmt.setString(2, student.getName().trim());
            stmt.setString(3, student.getDepartment().trim().toUpperCase());
            stmt.setInt(4, student.getSemester());
            return stmt.executeUpdate() > 0;
        }
    }

    public Student getStudentByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT roll_no, name, department, semester FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    );
                }
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_no, name, department, semester FROM students ORDER BY roll_no";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("semester")
                ));
            }
        }
        return list;
    }

    public List<Student> getStudentsByDepartment(String dept) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_no, name, department, semester FROM students WHERE department = ? ORDER BY roll_no";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dept.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, overall_pass_min) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE title = VALUES(title), credits = VALUES(credits), course_type = VALUES(course_type), " +
                     "cia_weightage = VALUES(cia_weightage), ese_weightage = VALUES(ese_weightage), ese_min_threshold = VALUES(ese_min_threshold), " +
                     "overall_pass_min = VALUES(overall_pass_min)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getCourseCode().trim().toUpperCase());
            stmt.setString(2, course.getTitle().trim());
            stmt.setDouble(3, course.getCredits());
            stmt.setString(4, course.getCourseType().trim().toUpperCase());
            stmt.setInt(5, course.getCiaWeightage());
            stmt.setInt(6, course.getEseWeightage());
            stmt.setDouble(7, course.getEseMinThreshold());
            stmt.setDouble(8, course.getOverallPassMin());
            return stmt.executeUpdate() > 0;
        }
    }

    public Course getCourseByCode(String courseCode) throws SQLException {
        String sql = "SELECT course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, overall_pass_min " +
                     "FROM courses WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getString("course_code"),
                            rs.getString("title"),
                            rs.getDouble("credits"),
                            rs.getString("course_type"),
                            rs.getInt("cia_weightage"),
                            rs.getInt("ese_weightage"),
                            rs.getDouble("ese_min_threshold"),
                            rs.getDouble("overall_pass_min")
                    );
                }
            }
        }
        return null;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, overall_pass_min " +
                     "FROM courses ORDER BY course_code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getDouble("credits"),
                        rs.getString("course_type"),
                        rs.getInt("cia_weightage"),
                        rs.getInt("ese_weightage"),
                        rs.getDouble("ese_min_threshold"),
                        rs.getDouble("overall_pass_min")
                ));
            }
        }
        return list;
    }

    public int enrollStudent(String rollNo, String courseCode, int semester,
                             int totalClasses, int attendedClasses, boolean condoned) throws SQLException {
        Enrollment existing = getEnrollment(rollNo, courseCode, semester);
        if (existing != null) {
            updateAttendance(existing.getEnrollmentId(), totalClasses, attendedClasses, condoned);
            initAssessmentMarks(existing.getEnrollmentId());
            return existing.getEnrollmentId();
        }

        String sql = "INSERT INTO enrollments (roll_no, course_code, semester, total_classes, attended_classes, condonation_approved) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, rollNo.trim().toUpperCase());
            stmt.setString(2, courseCode.trim().toUpperCase());
            stmt.setInt(3, semester);
            stmt.setInt(4, totalClasses);
            stmt.setInt(5, attendedClasses);
            stmt.setBoolean(6, condoned);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int enrollmentId = rs.getInt(1);
                    initAssessmentMarks(enrollmentId);
                    return enrollmentId;
                }
            }
        }
        return -1;
    }

    private void initAssessmentMarks(int enrollmentId) throws SQLException {
        String sql = "INSERT IGNORE INTO assessment_marks (enrollment_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        }
    }

    public boolean updateAttendance(int enrollmentId, int totalClasses, int attendedClasses, boolean condoned) throws SQLException {
        String sql = "UPDATE enrollments SET total_classes = ?, attended_classes = ?, condonation_approved = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, totalClasses);
            stmt.setInt(2, attendedClasses);
            stmt.setBoolean(3, condoned);
            stmt.setInt(4, enrollmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Enrollment getEnrollment(String rollNo, String courseCode, int semester) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.attended_classes, e.condonation_approved, " +
                     "s.name AS student_name, c.title AS course_title, c.credits, c.course_type " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_no = s.roll_no " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE e.roll_no = ? AND e.course_code = ? AND e.semester = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo.trim().toUpperCase());
            stmt.setString(2, courseCode.trim().toUpperCase());
            stmt.setInt(3, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("course_code"),
                            rs.getInt("semester"),
                            rs.getInt("total_classes"),
                            rs.getInt("attended_classes"),
                            rs.getBoolean("condonation_approved")
                    );
                    e.setStudentName(rs.getString("student_name"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCredits(rs.getDouble("credits"));
                    e.setCourseType(rs.getString("course_type"));
                    return e;
                }
            }
        }
        return null;
    }

    public Enrollment getEnrollmentById(int enrollmentId) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.attended_classes, e.condonation_approved, " +
                     "s.name AS student_name, c.title AS course_title, c.credits, c.course_type " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_no = s.roll_no " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE e.enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("course_code"),
                            rs.getInt("semester"),
                            rs.getInt("total_classes"),
                            rs.getInt("attended_classes"),
                            rs.getBoolean("condonation_approved")
                    );
                    e.setStudentName(rs.getString("student_name"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCredits(rs.getDouble("credits"));
                    e.setCourseType(rs.getString("course_type"));
                    return e;
                }
            }
        }
        return null;
    }

    public List<Enrollment> getEnrollmentsByStudent(String rollNo, int semester) throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.attended_classes, e.condonation_approved, " +
                     "s.name AS student_name, c.title AS course_title, c.credits, c.course_type " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_no = s.roll_no " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "WHERE e.roll_no = ? AND e.semester = ? " +
                     "ORDER BY e.course_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo.trim().toUpperCase());
            stmt.setInt(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("course_code"),
                            rs.getInt("semester"),
                            rs.getInt("total_classes"),
                            rs.getInt("attended_classes"),
                            rs.getBoolean("condonation_approved")
                    );
                    e.setStudentName(rs.getString("student_name"));
                    e.setCourseTitle(rs.getString("course_title"));
                    e.setCredits(rs.getDouble("credits"));
                    e.setCourseType(rs.getString("course_type"));
                    list.add(e);
                }
            }
        }
        return list;
    }

    public List<Enrollment> getAllEnrollments() throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.attended_classes, e.condonation_approved, " +
                     "s.name AS student_name, c.title AS course_title, c.credits, c.course_type " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.roll_no = s.roll_no " +
                     "JOIN courses c ON e.course_code = c.course_code " +
                     "ORDER BY e.roll_no, e.course_code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Enrollment e = new Enrollment(
                        rs.getInt("enrollment_id"),
                        rs.getString("roll_no"),
                        rs.getString("course_code"),
                        rs.getInt("semester"),
                        rs.getInt("total_classes"),
                        rs.getInt("attended_classes"),
                        rs.getBoolean("condonation_approved")
                );
                e.setStudentName(rs.getString("student_name"));
                e.setCourseTitle(rs.getString("course_title"));
                e.setCredits(rs.getDouble("credits"));
                e.setCourseType(rs.getString("course_type"));
                list.add(e);
            }
        }
        return list;
    }
}
