/**
 * Enrollment Model - Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Attendance Rules:
 *   >= 75.0%: Eligible for ESE
 *   65.0% - 74.9%: Conditionally eligible (Requires Medical Condonation approval)
 *   < 65.0%: Award SA (Shortage of Attendance) grade (0 Grade Points). Barred from ESE.
 */
public class Enrollment {
    private int enrollmentId;
    private String rollNo;
    private String courseCode;
    private int semester;
    private int totalClasses;
    private int attendedClasses;
    private boolean condonationApproved;

    private String studentName;
    private String courseTitle;
    private double credits;
    private String courseType;

    public Enrollment() {}

    public Enrollment(int enrollmentId, String rollNo, String courseCode, int semester,
                      int totalClasses, int attendedClasses, boolean condonationApproved) {
        this.enrollmentId = enrollmentId;
        this.rollNo = rollNo;
        this.courseCode = courseCode;
        this.semester = semester;
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
        this.condonationApproved = condonationApproved;
    }

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    public int getAttendedClasses() { return attendedClasses; }
    public void setAttendedClasses(int attendedClasses) { this.attendedClasses = attendedClasses; }
    public boolean isCondonationApproved() { return condonationApproved; }
    public void setCondonationApproved(boolean condonationApproved) { this.condonationApproved = condonationApproved; }

    public double getAttendancePercentage() {
        if (totalClasses <= 0) return 0.0;
        return (attendedClasses * 100.0) / totalClasses;
    }

    public boolean isEligibleForESE() {
        double pct = getAttendancePercentage();
        if (pct >= 75.0) return true;
        if (pct >= 65.0 && condonationApproved) return true;
        return false;
    }

    public String getAttendanceStatus() {
        double pct = getAttendancePercentage();
        if (pct >= 75.0) return "Eligible";
        if (pct >= 65.0) return condonationApproved ? "Eligible (Condoned)" : "Barred (Condonation Req)";
        return "Barred (<65% Shortage)";
    }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    @Override
    public String toString() {
        return String.format("ID: %-4d | Roll: %-10s | Course: %-8s | Classes: %d/%d (%5.1f%%) | Status: %s",
                enrollmentId, rollNo, courseCode, attendedClasses, totalClasses, getAttendancePercentage(), getAttendanceStatus());
    }
}
