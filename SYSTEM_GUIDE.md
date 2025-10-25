# College ERP System - How It Works

## 🔐 Authentication & User Management System

Your College ERP system is designed with **role-based access control** and follows enterprise-level security practices. Here's how everything works:

## 1. User Registration & Creation

### **Current Registration Flow:**

#### **Students:**
- ✅ **Public Registration Available**: Students can register themselves via the public register page (`/api/auth/register`)
- 🔄 **Auto-assigned Role**: All public registrations are automatically assigned `STUDENT` role
- 📧 **Email Verification**: Optional email verification system (currently disabled for immediate access)
- 🏠 **Direct Access**: Students get immediate access to their portal after registration

#### **Faculty & Admin:**
- 🚫 **No Public Registration**: Faculty and Admin accounts can only be created by existing Admins
- 👨‍💼 **Admin-Only Creation**: Only users with `ADMIN` role can create Faculty and Admin accounts
- 🔐 **Secure Process**: Created through admin panel with controlled access

### **Admin Account Creation:**

Currently, there are a few ways to create the first admin account:

1. **Direct Database Insert** (Most Common):
```sql
-- Insert into users table
INSERT INTO users (name, email, password, role, is_active, is_verified, registration_date) 
VALUES ('Admin User', 'admin@college.edu', '$2a$10$encoded_password_hash', 'ADMIN', true, true, NOW());

-- Insert into admins table (using the user_id from above)
INSERT INTO admins (user_id, admin_id, employee_id, admin_type, access_level, can_manage_users, status) 
VALUES (1, 'ADM001', 'EMP001', 'SUPER_ADMIN', 'SYSTEM', true, 'ACTIVE');
```

2. **Application Properties** (Development):
```properties
# Default admin credentials in application.properties
spring.security.user.name=admin
spring.security.user.password=admin
```

3. **Data Initialization Script** (Recommended for Production):
```java
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByRole(User.UserRole.ADMIN)) {
            // Create default admin account
            createDefaultAdmin();
        }
    }
}
```

## 2. Login System

### **Login Credentials:**

#### **Admin Login:**
- 📧 **Email**: admin@college.edu (or custom email)
- 🔑 **Password**: admin123 (or custom password)
- 🎯 **Access Level**: Full system access

#### **Faculty Login:**
- 📧 **Email**: Created by admin during faculty creation
- 🔑 **Password**: Default or custom set by admin
- 🎯 **Access Level**: Course and student management

#### **Student Login:**
- 📧 **Email**: Set during self-registration
- 🔑 **Password**: Set during self-registration
- 🎯 **Access Level**: Academic records and course enrollment

### **JWT Authentication Flow:**
1. User submits email/password
2. System validates credentials
3. JWT token generated with role information
4. Token stored in browser localStorage
5. All API requests include Bearer token
6. Role-based page redirection

## 3. Database Integration (PostgreSQL + pgAdmin4)

### **Database Configuration:**
```properties
# Your current database settings
spring.datasource.url=jdbc:postgresql://localhost:5432/college_db
spring.datasource.username=postgres
spring.datasource.password=nalanajaik
```

### **Database Schema:**

#### **Users Table:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('STUDENT', 'FACULTY', 'ADMIN')),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT true,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
```

#### **Students Table:**
```sql
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    student_id VARCHAR(50) UNIQUE,
    academic_year VARCHAR(20),
    semester INTEGER,
    cgpa DECIMAL(3,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    fee_status VARCHAR(20) DEFAULT 'PENDING'
);
```

#### **Faculty Table:**
```sql
CREATE TABLE faculty (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    employee_id VARCHAR(50) UNIQUE,
    designation VARCHAR(100),
    department_id BIGINT REFERENCES departments(id),
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

#### **Admins Table:**
```sql
CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    admin_id VARCHAR(50) UNIQUE,
    employee_id VARCHAR(50),
    admin_type VARCHAR(50) DEFAULT 'ACADEMIC_ADMIN',
    access_level VARCHAR(50) DEFAULT 'DEPARTMENT',
    can_manage_users BOOLEAN DEFAULT false,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

### **Real-time Database Updates:**

✅ **All operations update PostgreSQL database in real-time:**

1. **User Registration** → `users` table
2. **Profile Creation** → `students`, `faculty`, or `admins` table
3. **Course Enrollment** → `enrollments` table
4. **Attendance Marking** → `attendance_records` table
5. **Grade Assignment** → `grades` table
6. **Authentication** → Updates `last_login` timestamp

### **Viewing in pgAdmin4:**

1. Connect to your PostgreSQL server
2. Navigate to: `Databases` → `college_db` → `Schemas` → `public` → `Tables`
3. Right-click any table → `View/Edit Data` → `All Rows`

## 4. System Architecture Flow

```
Frontend (HTML/JS) 
    ↕️ JWT Auth
Spring Boot Backend (REST API)
    ↕️ JPA/Hibernate
PostgreSQL Database
```

### **Role-Based Access:**

#### **Admin Portal:**
- 👥 Create/manage all users (students, faculty, other admins)
- 🏢 Department and course management
- 📊 System-wide analytics and reports
- ⚙️ System configuration

#### **Faculty Portal:**
- 📚 Manage assigned courses
- 👨‍🎓 View and manage enrolled students
- ✅ Mark attendance
- 📝 Assign grades
- 📊 Course analytics

#### **Student Portal:**
- 📋 View academic records
- 📚 Course enrollment/withdrawal
- 📅 View attendance records
- 🎯 View grades and CGPA
- 📢 View announcements

## 5. Quick Setup for Testing

### **Create First Admin Account:**

**Option 1: Direct SQL (pgAdmin4):**
```sql
-- Insert admin user
INSERT INTO users (name, email, password, role, is_active, is_verified) 
VALUES ('Super Admin', 'admin@college.edu', 
        '$2a$10$V7/TJqYTXGwNGKM5IrQjKOeIZ3dACGRjLHZyBhMJzQJ6K1VU4XGkW', -- "admin123"
        'ADMIN', true, true);

-- Insert admin profile (replace '1' with actual user_id from above)
INSERT INTO admins (user_id, admin_id, employee_id, admin_type, access_level, can_manage_users) 
VALUES (1, 'ADM001', 'EMP001', 'SUPER_ADMIN', 'SYSTEM', true);
```

**Option 2: Use Registration then Promote:**
1. Register as student via frontend
2. Manually update role in database:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
```

### **Test Login Credentials:**
- **Admin**: admin@college.edu / admin123
- **Student**: Register via frontend
- **Faculty**: Created by admin

## 🎯 Summary

Your ERP system is a **professional, enterprise-grade solution** with:

✅ **Secure JWT-based authentication**  
✅ **Role-based access control**  
✅ **Real-time PostgreSQL database integration**  
✅ **Complete CRUD operations for all entities**  
✅ **Professional Spring Boot architecture**  
✅ **Modern, responsive frontend**

The system properly separates concerns between different user types and maintains data integrity through proper database relationships and constraints.