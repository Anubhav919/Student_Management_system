-- create_test_student.sql
-- Creates one student record with a real email so you can test Forgot Password / OTP

USE studentdb;

INSERT INTO students (
    roll_no, application_number, name, gender, category, religion,
    father_name, mother_name, student_phone, parent_phone,
    temporary_address, permanent_address, district, state, pincode,
    course, department, current_semester, division, password, email
) VALUES (
    '26CSE9999001', 'APP999001', 'Anubhav Test', 'Male', 'General', 'Hindu',
    'Test Father', 'Test Mother', '9876543210', '9876543211',
    'Hostel Block A, Test City', '123 Test Street, Test City', 'Agra', 'Uttar Pradesh', '282001',
    'B.Tech', 'Computer Science Engineering', '3rd', 12, 'pass123', 'sgs.anubhav.15jan.10@gmail.com'
);
