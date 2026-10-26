-- ============================================================================
-- Kongu Engineering College (Autonomous), Perundurai, Erode - 638060
-- Regulation 2024 (R2024) - Academic Continuous Evaluation System Database Schema
-- Database Name: kec_eval_db
-- ============================================================================

CREATE DATABASE IF NOT EXISTS kec_eval_db;
USE kec_eval_db;

-- 1. Drop existing tables if present to allow clean initialization
DROP TABLE IF EXISTS assessment_marks;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;

-- ============================================================================
-- 2. Students Table
-- Roll Number format: e.g. 24ITR001, 24CSR015
-- ============================================================================
CREATE TABLE students (
    roll_no VARCHAR(15) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(20) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 3. Courses Table (KEC R2024 Catalog)
-- Course Types:
--   THEORY:    CIA = 40%, ESE = 60%, ESE Min Threshold = 45% (27/60), Pass Min = 50
--   PRACTICAL: CIA = 60%, ESE = 40%, ESE Min Threshold = 50% (20/40), Pass Min = 50
--   EMBEDDED:  CIA = 50%, ESE = 50%, ESE Min Threshold = 45% (22.5/50), Pass Min = 50
-- ============================================================================
CREATE TABLE courses (
    course_code VARCHAR(15) PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    credits DECIMAL(3,1) NOT NULL CHECK (credits > 0),
    course_type ENUM('THEORY', 'PRACTICAL', 'EMBEDDED') NOT NULL,
    cia_weightage INT NOT NULL,
    ese_weightage INT NOT NULL,
    ese_min_threshold DECIMAL(5,2) NOT NULL,
    overall_pass_min DECIMAL(5,2) DEFAULT 50.00
) ENGINE=InnoDB;

-- ============================================================================
-- 4. Enrollments & Attendance Tracking Table
-- Attendance Rules:
--   >= 75.0%: Eligible for ESE
--   65.0% - 74.9%: Conditionally Eligible (Requires Medical Condonation approval)
--   < 65.0%: Barred from ESE, awarded SA (Shortage of Attendance) grade
-- ============================================================================
CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(15) NOT NULL,
    course_code VARCHAR(15) NOT NULL,
    semester INT NOT NULL,
    total_classes INT NOT NULL DEFAULT 60 CHECK (total_classes >= 0),
    attended_classes INT NOT NULL DEFAULT 0 CHECK (attended_classes >= 0 AND attended_classes <= total_classes),
    condonation_approved BOOLEAN DEFAULT FALSE,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    CONSTRAINT uk_student_course_sem UNIQUE (roll_no, course_code, semester)
) ENGINE=InnoDB;

-- ============================================================================
-- 5. Assessment Marks Table (Phased / Incremental Evaluation)
-- Phased Marks Entry:
--   Phase 1 (Mid-Semester): CIA Test 1 (0-100), CIA Test 2 (0-100)
--   Phase 2 (End of Semester - CIA): Assignments/Quizzes/Lab (0-100)
--   Phase 3 (Post Semester): ESE Marks (0-100) and Absent flag (AB)
-- Evaluation Fields:
--   Scaled CIA, Scaled ESE, Total Marks, Letter Grade, Grade Point, Result Status
-- ============================================================================
CREATE TABLE assessment_marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    cia1 DECIMAL(5,2) DEFAULT NULL CHECK (cia1 IS NULL OR (cia1 >= 0 AND cia1 <= 100)),
    cia2 DECIMAL(5,2) DEFAULT NULL CHECK (cia2 IS NULL OR (cia2 >= 0 AND cia2 <= 100)),
    assignment DECIMAL(5,2) DEFAULT NULL CHECK (assignment IS NULL OR (assignment >= 0 AND assignment <= 100)),
    cia_total DECIMAL(5,2) DEFAULT NULL,
    ese_mark DECIMAL(5,2) DEFAULT NULL CHECK (ese_mark IS NULL OR (ese_mark >= 0 AND ese_mark <= 100)),
    is_absent BOOLEAN DEFAULT FALSE,
    scaled_cia DECIMAL(5,2) DEFAULT NULL,
    scaled_ese DECIMAL(5,2) DEFAULT NULL,
    total_marks DECIMAL(5,2) DEFAULT NULL,
    letter_grade VARCHAR(5) DEFAULT NULL,
    grade_point INT DEFAULT NULL,
    result_status VARCHAR(40) DEFAULT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_marks_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- 6. Sample Initial Data for Kongu Engineering College (R2024)
-- ============================================================================

-- Courses
INSERT INTO courses (course_code, title, credits, course_type, cia_weightage, ese_weightage, ese_min_threshold, overall_pass_min) VALUES
('24ITR31', 'Database Management Systems', 3.0, 'THEORY', 40, 60, 45.0, 50.0),
('24ITR32', 'Object Oriented Programming with Java', 3.0, 'THEORY', 40, 60, 45.0, 50.0),
('24ITR33', 'Data Structures and Algorithms', 4.0, 'EMBEDDED', 50, 50, 45.0, 50.0),
('24ITR38', 'Java Programming Laboratory', 1.5, 'PRACTICAL', 60, 40, 50.0, 50.0),
('24CSR31', 'Computer Architecture and Organization', 3.0, 'THEORY', 40, 60, 45.0, 50.0);

-- Students
INSERT INTO students (roll_no, name, department, semester) VALUES
('24ITR001', 'Aravind Kumar K', 'IT', 3),
('24ITR002', 'Bhavani S', 'IT', 3),
('24ITR003', 'Chandran M', 'IT', 3),
('24ITR004', 'Deepika R', 'IT', 3),
('24CSR015', 'Gokulnath P', 'CSE', 3);

-- Enrollments with diverse attendance scenarios:
-- Aravind: Regular student (good attendance)
INSERT INTO enrollments (roll_no, course_code, semester, total_classes, attended_classes, condonation_approved) VALUES
('24ITR001', '24ITR31', 3, 60, 54, FALSE), -- 90.0%
('24ITR001', '24ITR32', 3, 60, 51, FALSE), -- 85.0%
('24ITR001', '24ITR33', 3, 60, 50, FALSE), -- 83.3%
('24ITR001', '24ITR38', 3, 30, 28, FALSE); -- 93.3%

-- Bhavani: Regular student
INSERT INTO enrollments (roll_no, course_code, semester, total_classes, attended_classes, condonation_approved) VALUES
('24ITR002', '24ITR31', 3, 60, 48, FALSE), -- 80.0%
('24ITR002', '24ITR32', 3, 60, 50, FALSE), -- 83.3%
('24ITR002', '24ITR38', 3, 30, 26, FALSE); -- 86.6%

-- Chandran: Marginal attendance (68%) with Medical Condonation approved
INSERT INTO enrollments (roll_no, course_code, semester, total_classes, attended_classes, condonation_approved) VALUES
('24ITR003', '24ITR31', 3, 60, 41, TRUE),  -- 68.3%, condoned
('24ITR003', '24ITR32', 3, 60, 42, TRUE);  -- 70.0%, condoned

-- Deepika: Attendance Defaulter (< 65%) -> Shortage of Attendance (SA)
INSERT INTO enrollments (roll_no, course_code, semester, total_classes, attended_classes, condonation_approved) VALUES
('24ITR004', '24ITR31', 3, 60, 32, FALSE), -- 53.3% -> SA Defaulter
('24ITR004', '24ITR32', 3, 60, 34, FALSE); -- 56.6% -> SA Defaulter

-- Gokulnath (CSE): Regular student
INSERT INTO enrollments (roll_no, course_code, semester, total_classes, attended_classes, condonation_approved) VALUES
('24CSR015', '24CSR31', 3, 60, 52, FALSE);

-- Marks Records
-- Aravind: Full semester marks entered and evaluated
INSERT INTO assessment_marks (enrollment_id, cia1, cia2, assignment, cia_total, ese_mark, is_absent, scaled_cia, scaled_ese, total_marks, letter_grade, grade_point, result_status) VALUES
(1, 92.0, 88.0, 95.0, 91.50, 86.0, FALSE, 36.60, 51.60, 88.20, 'A+', 9, 'PASS'),
(2, 95.0, 94.0, 98.0, 95.55, 92.0, FALSE, 38.22, 55.20, 93.42, 'O', 10, 'PASS'),
(3, 85.0, 82.0, 88.0, 84.85, 80.0, FALSE, 42.43, 40.00, 82.43, 'A+', 9, 'PASS'),
(4, 90.0, 92.0, 96.0, 92.50, 94.0, FALSE, 55.50, 37.60, 93.10, 'O', 10, 'PASS');

-- Bhavani: Mid-semester Phase 1 marks entered
INSERT INTO assessment_marks (enrollment_id, cia1, cia2, assignment, cia_total, ese_mark, is_absent) VALUES
(5, 78.0, 82.0, NULL, NULL, NULL, FALSE),
(6, 85.0, 80.0, NULL, NULL, NULL, FALSE),
(7, 88.0, 90.0, NULL, NULL, NULL, FALSE);
