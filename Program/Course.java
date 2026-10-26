/**
 * Course Model - Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Course Types:
 *   - THEORY:    CIA = 40%, ESE = 60%, ESE Min Threshold = 45%, Pass Min = 50 Marks
 *   - PRACTICAL: CIA = 60%, ESE = 40%, ESE Min Threshold = 50%, Pass Min = 50 Marks
 *   - EMBEDDED:  CIA = 50%, ESE = 50%, ESE Min Threshold = 45%, Pass Min = 50 Marks
 */
public class Course {
    private String courseCode;
    private String title;
    private double credits;
    private String courseType; // THEORY, PRACTICAL, EMBEDDED
    private int ciaWeightage;
    private int eseWeightage;
    private double eseMinThreshold;
    private double overallPassMin;

    public Course() {
        this.overallPassMin = 50.0;
    }

    public Course(String courseCode, String title, double credits, String courseType) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.courseType = courseType.toUpperCase();
        this.overallPassMin = 50.0;
        configureWeightsByType(this.courseType);
    }

    public Course(String courseCode, String title, double credits, String courseType,
                  int ciaWeightage, int eseWeightage, double eseMinThreshold, double overallPassMin) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.courseType = courseType.toUpperCase();
        this.ciaWeightage = ciaWeightage;
        this.eseWeightage = eseWeightage;
        this.eseMinThreshold = eseMinThreshold;
        this.overallPassMin = overallPassMin;
    }

    public void configureWeightsByType(String type) {
        switch (type.toUpperCase()) {
            case "THEORY":
                this.ciaWeightage = 40;
                this.eseWeightage = 60;
                this.eseMinThreshold = 45.0; // 45% (27/60)
                break;
            case "PRACTICAL":
                this.ciaWeightage = 60;
                this.eseWeightage = 40;
                this.eseMinThreshold = 50.0; // 50% (20/40)
                break;
            case "EMBEDDED":
                this.ciaWeightage = 50;
                this.eseWeightage = 50;
                this.eseMinThreshold = 45.0; // 45% (22.5/50)
                break;
            default:
                throw new IllegalArgumentException("Unknown Course Type: " + type + ". Allowed: THEORY, PRACTICAL, EMBEDDED");
        }
    }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) {
        this.courseType = courseType.toUpperCase();
        configureWeightsByType(this.courseType);
    }
    public int getCiaWeightage() { return ciaWeightage; }
    public void setCiaWeightage(int ciaWeightage) { this.ciaWeightage = ciaWeightage; }
    public int getEseWeightage() { return eseWeightage; }
    public void setEseWeightage(int eseWeightage) { this.eseWeightage = eseWeightage; }
    public double getEseMinThreshold() { return eseMinThreshold; }
    public void setEseMinThreshold(double eseMinThreshold) { this.eseMinThreshold = eseMinThreshold; }
    public double getOverallPassMin() { return overallPassMin; }
    public void setOverallPassMin(double overallPassMin) { this.overallPassMin = overallPassMin; }

    @Override
    public String toString() {
        return String.format("%-10s | %-35s | Credits: %-3.1f | Type: %-9s | CIA: %d%% | ESE: %d%% (Min: %.0f%%)",
                courseCode, title, credits, courseType, ciaWeightage, eseWeightage, eseMinThreshold);
    }
}
