/**
 * AssessmentMarks Model - Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Supports phased/incremental marks entry:
 *   Phase 1: CIA 1 & CIA 2
 *   Phase 2: Assignments / Lab Evaluations
 *   Phase 3: ESE Marks & Absence (AB)
 */
public class AssessmentMarks {
    private int markId;
    private int enrollmentId;
    private Double cia1;
    private Double cia2;
    private Double assignment;
    private Double ciaTotal;
    private Double eseMark;
    private boolean isAbsent;
    private Double scaledCia;
    private Double scaledEse;
    private Double totalMarks;
    private String letterGrade;
    private Integer gradePoint;
    private String resultStatus;

    public AssessmentMarks() {
        this.isAbsent = false;
    }

    public AssessmentMarks(int markId, int enrollmentId) {
        this.markId = markId;
        this.enrollmentId = enrollmentId;
        this.isAbsent = false;
    }

    public int getMarkId() { return markId; }
    public void setMarkId(int markId) { this.markId = markId; }
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public Double getCia1() { return cia1; }
    public void setCia1(Double cia1) { this.cia1 = cia1; }
    public Double getCia2() { return cia2; }
    public void setCia2(Double cia2) { this.cia2 = cia2; }
    public Double getAssignment() { return assignment; }
    public void setAssignment(Double assignment) { this.assignment = assignment; }
    public Double getCiaTotal() { return ciaTotal; }
    public void setCiaTotal(Double ciaTotal) { this.ciaTotal = ciaTotal; }
    public Double getEseMark() { return eseMark; }
    public void setEseMark(Double eseMark) { this.eseMark = eseMark; }
    public boolean isAbsent() { return isAbsent; }
    public void setAbsent(boolean absent) { isAbsent = absent; }
    public Double getScaledCia() { return scaledCia; }
    public void setScaledCia(Double scaledCia) { this.scaledCia = scaledCia; }
    public Double getScaledEse() { return scaledEse; }
    public void setScaledEse(Double scaledEse) { this.scaledEse = scaledEse; }
    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }
    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }
    public Integer getGradePoint() { return gradePoint; }
    public void setGradePoint(Integer gradePoint) { this.gradePoint = gradePoint; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    @Override
    public String toString() {
        return String.format("Enrollment #%d | CIA1: %s | CIA2: %s | Assign: %s | CIA_Tot: %s | ESE: %s (AB: %s) | Final: %s [Grade: %s, GP: %s, Status: %s]",
                enrollmentId,
                cia1 != null ? String.format("%.1f", cia1) : "N/A",
                cia2 != null ? String.format("%.1f", cia2) : "N/A",
                assignment != null ? String.format("%.1f", assignment) : "N/A",
                ciaTotal != null ? String.format("%.1f", ciaTotal) : "N/A",
                eseMark != null ? String.format("%.1f", eseMark) : "N/A",
                isAbsent ? "YES" : "NO",
                totalMarks != null ? String.format("%.1f", totalMarks) : "N/A",
                letterGrade != null ? letterGrade : "PENDING",
                gradePoint != null ? gradePoint.toString() : "0",
                resultStatus != null ? resultStatus : "PENDING");
    }
}
