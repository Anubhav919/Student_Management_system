-- add_student_password.sql
-- Run this AFTER studentdb.sql and sample_data.sql
-- Adds a password column so students can log in with roll_no + password

USE studentdb;

ALTER TABLE students ADD COLUMN password VARCHAR(50) NOT NULL DEFAULT 'pass123';

-- Every existing sample student now has the password: pass123
-- (When admin adds a new student later, admin sets this field too)