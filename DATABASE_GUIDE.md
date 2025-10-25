# 🗄️ College ERP - Database Guide

## Database Status: ✅ READY

### Current Setup
- **Database Name**: `college_db`
- **Owner**: `postgres`
- **Tables Created**: 11 tables
- **Backend Status**: ✅ Running on port 8080 (PID 3864)

---

## 📊 Database Tables

```
1.  admin_permissions   - Admin role permissions
2.  admins             - Admin user records
3.  attendance_records - Student attendance tracking
4.  course             - Individual course data
5.  courses            - Course catalog
6.  departments        - Academic departments
7.  enrollments        - Student course enrollments
8.  faculty            - Faculty member records
9.  grades             - Student grades per course
10. students           - Student records
11. users              - Base user authentication table
```

---

## 🔍 Quick Database Commands

### Connect to Database
```bash
psql -U postgres -d college_db
```

### View All Tables
```sql
\dt
```

### View Table Structure
```sql
\d users
\d students
\d faculty
\d courses
```

### Exit psql
```
\q
```

---

## 📋 Current Database Data

### Users Table (5 users total)
```sql
SELECT id, name, email, role FROM users ORDER BY role;
```

**Results:**
| ID | Name         | Email                        | Role    |
|----|--------------|------------------------------|---------|
| 6  | Admin User   | admin@college.edu            | ADMIN   |
| 2  | alan         | alan@gmail.com               | STUDENT |
| 3  | Ajai Kumar N | deepanalanalanajai@gmail.com | STUDENT |
| 5  | Ajai Kumar N | deepanalan@gmail.com         | STUDENT |
| 1  | Ajai         | ajaikumar@karunya.edu.in     | (empty) |

### Students Table
**Status**: Empty (0 rows)
- Users exist but no student profiles created yet
- Need to use the admin panel to create student records

### Faculty Table
```sql
SELECT COUNT(*) FROM faculty;
```
**Status**: Check if any faculty exist

---

## 🎯 How to Populate the Database

### Option 1: Use the Sample Data Utility
1. Open your browser to: http://localhost:3000/add-sample-data.html
2. Click "Add All Sample Data"
3. Wait for completion
4. Refresh and check database

### Option 2: Use the Admin Panel
1. Login as admin: http://localhost:3000/login.html
   - Email: `admin@college.edu`
   - Password: `admin123`
2. Navigate to Students section
3. Add students using the form
4. Navigate to Faculty section
5. Add faculty using the form

### Option 3: Direct SQL Insert (for testing)
```sql
-- Insert a test student
INSERT INTO users (name, email, password, role, registration_date)
VALUES ('Test Student', 'test@student.com', '$2a$10$hashed_password', 'STUDENT', NOW())
RETURNING id;

-- Then create student record using the returned user_id
INSERT INTO students (user_id, admission_number, course, current_year)
VALUES (7, 'TEST001', 'Computer Science', 2);
```

---

## 🔬 Useful SQL Queries

### Check User Count by Role
```sql
SELECT role, COUNT(*) as count 
FROM users 
WHERE role IS NOT NULL 
GROUP BY role;
```

### View All Students with User Info
```sql
SELECT 
    s.admission_number,
    u.name,
    u.email,
    s.course,
    s.current_year
FROM students s
JOIN users u ON s.user_id = u.id
ORDER BY s.admission_number;
```

### View All Faculty with User Info
```sql
SELECT 
    f.faculty_id,
    u.name,
    u.email,
    f.department,
    f.qualification
FROM faculty f
JOIN users u ON f.user_id = u.id
ORDER BY f.faculty_id;
```

### Check Enrollments
```sql
SELECT 
    e.id,
    u.name as student_name,
    c.name as course_name,
    e.enrollment_date
FROM enrollments e
JOIN students s ON e.student_id = s.id
JOIN users u ON s.user_id = u.id
JOIN courses c ON e.course_id = c.id;
```

### Check Grades
```sql
SELECT 
    u.name as student_name,
    c.name as course_name,
    g.grade,
    g.marks
FROM grades g
JOIN students s ON g.student_id = s.id
JOIN users u ON s.user_id = u.id
JOIN courses c ON g.course_id = c.id;
```

### Check Attendance Summary
```sql
SELECT 
    u.name as student_name,
    c.name as course_name,
    COUNT(*) as total_records,
    SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) as present,
    SUM(CASE WHEN ar.status = 'ABSENT' THEN 1 ELSE 0 END) as absent
FROM attendance_records ar
JOIN students s ON ar.student_id = s.id
JOIN users u ON s.user_id = u.id
JOIN courses c ON ar.course_id = c.id
GROUP BY u.name, c.name;
```

---

## 🛠️ Database Management Commands

### Backup Database
```bash
pg_dump -U postgres college_db > college_db_backup_$(date +%Y%m%d).sql
```

### Restore Database
```bash
psql -U postgres college_db < college_db_backup_20251024.sql
```

### Drop All Data (CAREFUL!)
```sql
-- Drop all tables
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Restart backend to recreate tables
```

### View Table Sizes
```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 🔐 Database Security

### Current Admin User
- **Email**: admin@college.edu
- **Password**: admin123 (hashed with BCrypt)
- **User ID**: 6
- **Role**: ADMIN

### Password Hashing
All passwords are hashed using BCrypt with strength 10. Never store plain text passwords!

### Connection String
```
jdbc:postgresql://localhost:5432/college_db
Username: postgres
Password: (configured in application.properties)
```

---

## 📈 Database Performance Tips

### Add Indexes (if needed)
```sql
-- Index on frequently searched columns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_students_admission ON students(admission_number);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_grades_student ON grades(student_id);
```

### Analyze Tables
```sql
ANALYZE users;
ANALYZE students;
ANALYZE faculty;
ANALYZE courses;
```

---

## 🐛 Troubleshooting

### "No relations found" Error
- Make sure you're connected to `college_db` not `college_erp`
- Backend might not have started yet
- Check `application.properties` for correct database name

### Connection Refused
```bash
# Check if PostgreSQL is running
brew services list | grep postgresql

# Start PostgreSQL
brew services start postgresql@14
```

### Permission Denied
```bash
# Make sure postgres user has access
psql -U postgres -d postgres -c "ALTER USER postgres WITH PASSWORD 'your_password';"
```

---

## 🎉 Quick Start

**To view your database right now:**

```bash
# 1. Connect to database
psql -U postgres -d college_db

# 2. View all tables
\dt

# 3. View users
SELECT id, name, email, role FROM users;

# 4. View students (empty initially)
SELECT * FROM students;

# 5. Exit
\q
```

**To populate with sample data:**
1. Open: http://localhost:3000/add-sample-data.html
2. Click "Add All Sample Data"
3. Go back to psql and run queries above

---

**Database is ready! Backend is running! Start adding data!** 🚀
