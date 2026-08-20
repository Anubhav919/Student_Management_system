-- studentdb.sql
-- Run this in phpMyAdmin (XAMPP) or MySQL CLI to set up the database

CREATE DATABASE IF NOT EXISTS studentdb;
USE studentdb;

-- ============ admin table (for DB-backed admin login) ============
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL
);

INSERT INTO admin (username, password) VALUES ('admin', 'admin123');

-- ============ students / subjects / student_marks ============
-- roll_no is VARCHAR(12): 2-digit year + 3-letter dept code + 7 random digits (e.g. 26CSE1234567)

DROP TABLE IF EXISTS student_marks;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS students;

CREATE TABLE students (
    roll_no VARCHAR(12) PRIMARY KEY,
    application_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    category VARCHAR(20),
    religion VARCHAR(30),
    father_name VARCHAR(100),
    mother_name VARCHAR(100),
    student_phone VARCHAR(15),
    parent_phone VARCHAR(15),
    temporary_address TEXT,
    permanent_address TEXT,
    district VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10),
    course VARCHAR(20),
    department VARCHAR(60),
    current_semester VARCHAR(10),
    division INT
);

CREATE TABLE subjects (
    subject_id VARCHAR(15) PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    course VARCHAR(20),
    semester VARCHAR(10)
);

CREATE TABLE student_marks (
    marks_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(12) NOT NULL,
    subject_id VARCHAR(15) NOT NULL,
    semester_recorded VARCHAR(10),
    marks_obtained INT,
    total_marks INT DEFAULT 100,
    attendance_status ENUM('Present','Absent') DEFAULT 'Present',
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);

-- Now run sample_data.sql to fill students / subjects / student_marks with sample rows.