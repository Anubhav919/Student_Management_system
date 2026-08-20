# SmartStudent - Student Management System

A desktop-based Student Management System built with **Java Swing**, **MySQL**, and **JDBC** featuring secure authentication with OTP-based password recovery.

---

## 📋 Quick Links

- **[Project Overview](#project-overview)**
- **[Tech Stack & Libraries](#-tech-stack--libraries)**
- **[Database Structure](#-database-structure)**
- **[Installation & Setup](#-installation--setup)**
- **[Configuration](#%EF%B8%8F-configuration)**
- **[How to Run](#-how-to-run)**
- **[Troubleshooting](#-troubleshooting)**

---

## 🎯 Project Overview

**SmartStudent** is a comprehensive student management desktop application with:

- ✅ User authentication (Login/Register)
- ✅ OTP-based password reset via Gmail
- ✅ Complete student CRUD operations
- ✅ Secure database connectivity
- ✅ User-friendly Swing GUI

**Target Users:** Schools, colleges, and educational institutions managing student records.

---

## 🛠 Tech Stack & Libraries

### **Technology Stack**

| Component         | Technology         | Version              | Purpose                     |
| ----------------- | ------------------ | -------------------- | --------------------------- |
| **Language**      | Java               | 8+ (11+ recommended) | Core programming language   |
| **GUI Framework** | Java Swing         | Built-in             | Desktop user interface      |
| **Database**      | MySQL              | 5.7+ or 8.0+         | Data storage and management |
| **JDBC Driver**   | MySQL Connector/J  | 8.0.33               | Java-to-MySQL communication |
| **Email API**     | Java Mail          | 1.6.2                | Send OTP emails via SMTP    |
| **IDE**           | VS Code / IntelliJ | Latest               | Code development            |
| **Server Setup**  | XAMPP              | Latest               | Local MySQL + phpMyAdmin    |

---

### **Required Libraries (JAR Files)**

**1. mysql-connector-java-8.0.33.jar** (2.5 MB)

- **Purpose:** JDBC driver for MySQL database connectivity
- **Download:** https://dev.mysql.com/downloads/connector/j/
- **Location in Project:** `lib/mysql-connector-java-8.0.33.jar`
- **Used By:** DatabaseConnection.java, UserDAO.java, StudentDAO.java

**2. javax.mail-1.6.2.jar** (500 KB)

- **Purpose:** Java Mail API for sending OTP emails via SMTP
- **Download:** https://github.com/eclipse-ee4j/mail/releases
- **Location in Project:** `lib/javax.mail-1.6.2.jar`
- **Used By:** EmailOTPService.java

---

### **Java Libraries (Built-in)**

| Library         | Components Used                                      | Purpose             |
| --------------- | ---------------------------------------------------- | ------------------- |
| `javax.swing.*` | JFrame, JPanel, JButton, JTable, JTextField, JDialog | GUI components      |
| `java.awt.*`    | Layout managers, Graphics, Color, Font               | GUI utilities       |
| `java.sql.*`    | Connection, Statement, ResultSet, PreparedStatement  | Database operations |
| `java.util.*`   | ArrayList, HashMap, Date                             | Data structures     |
| `javax.mail.*`  | Session, Message, Transport, Authenticator           | Email sending       |

---

### **How to Add Libraries**

**Method 1: Manual Setup**

```bash
1. Create lib/ folder in project root
2. Download both JAR files
3. Place them in lib/ folder
4. Configure IDE classpath (see IDE setup below)
```

**Method 2: VS Code**

```bash
1. Install "Extension Pack for Java" (Microsoft)
2. Create .classpath file in project root:
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
    <classpathentry kind="src" path="src"/>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
    <classpathentry kind="lib" path="lib/mysql-connector-java-8.0.33.jar"/>
    <classpathentry kind="lib" path="lib/javax.mail-1.6.2.jar"/>
    <classpathentry kind="output" path="bin"/>
</classpath>
```

```bash
3. Restart VS Code
```

**Method 3: IntelliJ IDEA**

```
File → Project Structure → Libraries
→ Click + → Java → Select both JAR files from lib/ folder
```

**Method 4: Eclipse**

```
Right-click Project → Build Path → Configure Build Path
→ Add External Archives → Select JAR files
```

---

## 🗄️ Database Structure

### **Database Name:** `studentdb`

### **Table 1: users** (User Authentication)

```sql
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    otp VARCHAR(6),
    otp_expiry TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    INDEX idx_email (email)
);
```

**Fields:**

- `user_id` - Unique user identifier
- `email` - User login email (must be unique)
- `password` - Hashed password
- `username` - Display name
- `otp` - 6-digit one-time password for password reset
- `otp_expiry` - When OTP expires (valid for 5 minutes)
- `is_verified` - Email verification status

---

### **Table 2: students** (Student Records)

```sql
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    date_of_birth DATE,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    enrollment_date DATE DEFAULT CURRENT_DATE,
    class_name VARCHAR(50),
    gpa DECIMAL(3, 2),
    status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED') DEFAULT 'ACTIVE',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    INDEX idx_roll_no (roll_no),
    INDEX idx_email_student (email),
    INDEX idx_status (status)
);
```

**Fields:**

- `student_id` - Unique student identifier
- `roll_no` - Unique roll number
- `first_name`, `last_name` - Full name
- `email`, `phone` - Contact information
- `date_of_birth` - Birth date
- `address`, `city`, `state`, `zip_code` - Address details
- `enrollment_date` - When student joined
- `class_name` - Current class/semester
- `gpa` - Grade point average (0.00 - 4.00)
- `status` - ACTIVE, INACTIVE, or GRADUATED
- `created_by` - User ID who created record (foreign key)
- `created_at`, `updated_at` - Audit timestamps

---

### **Complete SQL Schema Script**

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS studentdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE studentdb;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    otp VARCHAR(6),
    otp_expiry TIMESTAMP NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    INDEX idx_email (email),
    INDEX idx_otp_expiry (otp_expiry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    date_of_birth DATE,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    enrollment_date DATE DEFAULT CURRENT_DATE,
    class_name VARCHAR(50),
    gpa DECIMAL(3, 2),
    status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED') DEFAULT 'ACTIVE',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_roll_no (roll_no),
    INDEX idx_email_student (email),
    INDEX idx_status (status),
    INDEX idx_class_name (class_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample admin user
INSERT INTO users (email, password, username, is_verified)
VALUES ('admin@smartstudent.com', 'admin123', 'Admin User', TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- Insert sample students
INSERT INTO students (roll_no, first_name, last_name, email, phone,
    date_of_birth, city, state, zip_code, class_name, gpa, status, created_by)
VALUES
('ROLL001', 'Rajesh', 'Kumar', 'rajesh@example.com', '9876543210',
    '2005-01-15', 'Delhi', 'Delhi', '110001', 'Class 12-A', 3.8, 'ACTIVE', 1),
('ROLL002', 'Priya', 'Singh', 'priya@example.com', '9876543211',
    '2005-03-22', 'Bangalore', 'Karnataka', '560001', 'Class 12-A', 3.9, 'ACTIVE', 1);
```

---

## 💻 System Requirements

- **OS:** Windows 10+, macOS 10.13+, Linux (Ubuntu 18.04+)
- **Java:** JDK 8 or higher (preferably JDK 11+)
- **RAM:** 2GB minimum (4GB recommended)
- **Disk Space:** 500MB free space
- **MySQL:** 5.7 or higher (or XAMPP with MySQL)

---

## 📥 Installation & Setup

### **Step 1: Install Java JDK**

**Windows:**

- Download from: https://www.oracle.com/java/technologies/downloads/
- Run installer → Follow prompts
- Verify: Open Command Prompt, type `java -version`

**macOS:**

```bash
brew install openjdk@11
java -version
```

**Linux (Ubuntu):**

```bash
sudo apt update
sudo apt install openjdk-11-jdk
java -version
```

---

### **Step 2: Install MySQL**

**Option A: Using XAMPP (Easiest)**

1. Download from: https://www.apachefriends.org/
2. Install and run XAMPP Control Panel
3. Click "Start" next to MySQL
4. Open `http://localhost/phpmyadmin`
5. Default: username=`root`, password=empty

**Option B: Direct MySQL Installation**

**Windows:** Download from https://dev.mysql.com/downloads/mysql/

**Linux (Ubuntu):**

```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
sudo systemctl start mysql
```

---

### **Step 3: Create Database**

**Method 1: Using phpMyAdmin (XAMPP)**

1. Open `http://localhost/phpmyadmin`
2. Click "New" → Database name: `studentdb`
3. Collation: `utf8mb4_unicode_ci`
4. Click "Create"
5. Select `studentdb` → Go to "Import" tab
6. Upload SQL schema file (or copy-paste SQL above)
7. Click "Execute"

**Method 2: Command Line**

```bash
mysql -u root -p
# Inside MySQL:
CREATE DATABASE studentdb;
USE studentdb;
# Paste entire SQL schema from above
```

**Verify Setup:**

```bash
mysql -u root -p studentdb
SHOW TABLES;  # Should show: students, users
```

---

### **Step 4: Download Required Libraries**

1. **Create `lib/` folder** in your project root
2. **Download:**
   - mysql-connector-java-8.0.33.jar
   - javax.mail-1.6.2.jar
3. **Place** both JAR files in `lib/` folder

---

### **Step 5: Set Up Project Structure**

```
SmartStudent/
├── src/
│   ├── Main.java
│   ├── DatabaseConnection.java
│   ├── EmailOTPService.java
│   ├── UserDAO.java
│   ├── StudentDAO.java
│   ├── LoginFrame.java
│   ├── RegisterFrame.java
│   ├── PasswordResetFrame.java
│   └── StudentManagementFrame.java
├── lib/
│   ├── mysql-connector-java-8.0.33.jar
│   └── javax.mail-1.6.2.jar
├── bin/  (auto-generated)
└── README.md
```

---

## ⚙️ Configuration

### **File 1: DatabaseConnection.java**

**CHANGE THESE VALUES:**

```java
public class DatabaseConnection {
    // ❌ CONFIGURE THESE:
    private static final String URL = "jdbc:mysql://localhost:3306/studentdb";
    private static final String USERNAME = "root";        // Your MySQL username
    private static final String PASSWORD = "";            // Your MySQL password
}
```

**Examples:**

```java
// XAMPP Default
private static final String USERNAME = "root";
private static final String PASSWORD = "";  // Empty

// Custom MySQL Setup
private static final String USERNAME = "admin";
private static final String PASSWORD = "myPassword123";

// Different Port
private static final String URL = "jdbc:mysql://localhost:3307/studentdb";

// Remote Database (Cloud)
private static final String URL = "jdbc:mysql://your-host.com:3306/studentdb";
private static final String USERNAME = "clouduser";
private static final String PASSWORD = "cloudpassword";
```

---

### **File 2: EmailOTPService.java**

**⚠️ IMPORTANT: Gmail Setup Required**

**Step 1: Enable 2-Step Verification**

1. Go to: https://myaccount.google.com/security
2. Find "2-Step Verification" → Click Enable
3. Follow Google's verification process

**Step 2: Generate App Password**

1. Go to: https://myaccount.google.com/apppasswords
2. Select: Mail + your device type
3. Click "Generate"
4. Copy the 16-character password

**Step 3: Update Code**

```java
public class EmailOTPService {
    // ❌ CONFIGURE THESE:
    private static final String SENDER_EMAIL = "your-email@gmail.com";
    private static final String SENDER_APP_PASSWORD = "xxxx xxxx xxxx xxxx";
}
```

**Example:**

```java
private static final String SENDER_EMAIL = "john.doe@gmail.com";
private static final String SENDER_APP_PASSWORD = "abcd efgh ijkl mnop";  // 16-char with spaces
```

**⚠️ Important Notes:**

- **NEVER** use your regular Gmail password — only App Password
- **Must include spaces** in the 16-character password
- **First use?** Gmail may show security alert — approve it
- **OTP expires in 5 minutes**

---

## ▶️ How to Run

### **Method 1: VS Code**

1. Open project folder
2. Install "Extension Pack for Java"
3. Open `Main.java`
4. Press **Ctrl + F5** to run

### **Method 2: IntelliJ IDEA**

1. File → Open → Select project folder
2. Right-click `Main.java` → Run

### **Method 3: Eclipse**

1. File → Import → Existing Projects
2. Select project folder
3. Right-click `Main.java` → Run As → Java Application

### **Method 4: Command Line**

```bash
# Navigate to project directory
cd SmartStudent

# Compile
javac -d bin -cp lib/* src/*.java

# Run
java -cp bin:lib/* Main
```

**On Windows, use semicolon instead:**

```bash
java -cp bin;lib/* Main
```

---

## 🧪 Test the Setup

### **Test Database Connection**

Create `TestConnection.java`:

```java
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("✓ Database connected successfully!");
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("✗ Connection failed!");
            e.printStackTrace();
        }
    }
}
```

Run it:

```bash
javac -cp lib/* TestConnection.java
java -cp bin:lib/* TestConnection
```

**Expected:** `✓ Database connected successfully!`

---

### **Test Gmail OTP**

```java
public class TestEmail {
    public static void main(String[] args) {
        String testEmail = "your-email@gmail.com";
        String otp = EmailOTPService.generateOtp();

        System.out.println("Generated OTP: " + otp);

        boolean success = EmailOTPService.sendOtp(testEmail, otp);

        if (success) {
            System.out.println("✓ Email sent successfully!");
        } else {
            System.out.println("✗ Email sending failed!");
        }
    }
}
```

**Expected:** Should receive email with OTP in 1-2 seconds

---

## 🐛 Troubleshooting

| Problem                                            | Cause                    | Solution                                                      |
| -------------------------------------------------- | ------------------------ | ------------------------------------------------------------- |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | MySQL JAR missing        | Add mysql-connector-java JAR to lib/ folder                   |
| `Connection refused: 127.0.0.1:3306`               | MySQL not running        | Start MySQL from XAMPP or `sudo systemctl start mysql`        |
| `Access denied for user 'root'@'localhost'`        | Wrong credentials        | Update DatabaseConnection.java with correct username/password |
| `Unknown database 'studentdb'`                     | Database not created     | Run SQL schema or create via phpMyAdmin                       |
| `ClassNotFoundException: javax.mail.Message`       | Mail JAR missing         | Add javax.mail-1.6.2.jar to lib/ folder                       |
| Gmail OTP not received                             | App password setup issue | Verify 2-Step Verification enabled, use correct App Password  |
| `SQLException: The server time zone value`         | MySQL timezone error     | Add `?serverTimezone=UTC` to JDBC URL                         |

---

## 📋 Pre-Launch Checklist

- [ ] Java JDK installed (`java -version` works)
- [ ] MySQL running (XAMPP or standalone)
- [ ] Database `studentdb` created
- [ ] Tables created from SQL schema
- [ ] DatabaseConnection.java configured with MySQL credentials
- [ ] EmailOTPService.java configured with Gmail app password
- [ ] Both JAR files in `lib/` folder
- [ ] IDE classpath configured correctly
- [ ] Database connection test passes
- [ ] Email sending test passes

---

## 🔐 Security Notes

**Important for Production:**

1. **Use environment variables** instead of hardcoding credentials:

```java
private static final String USERNAME = System.getenv("DB_USER");
private static final String PASSWORD = System.getenv("DB_PASSWORD");
```

2. **Hash passwords** using BCrypt or SHA-256:

```java
// Add BCrypt library for hashing
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
```

3. **Use prepared statements** to prevent SQL injection
4. **Enable HTTPS** if deploying as web app
5. **Never commit** code with real credentials to GitHub
6. **Implement rate limiting** on login attempts

---

## 📁 File Descriptions

| File                          | Purpose                                       |
| ----------------------------- | --------------------------------------------- |
| `Main.java`                   | Application entry point (run this)            |
| `DatabaseConnection.java`     | Manages database connections (⚙️ CONFIGURE)   |
| `EmailOTPService.java`        | Sends OTP emails via SMTP (⚙️ CONFIGURE)      |
| `UserDAO.java`                | User login/register/password reset operations |
| `StudentDAO.java`             | Student CRUD operations                       |
| `LoginFrame.java`             | Login screen GUI                              |
| `RegisterFrame.java`          | Registration screen GUI                       |
| `PasswordResetFrame.java`     | Password reset with OTP                       |
| `StudentManagementFrame.java` | Student management GUI                        |

---

## 📚 Resources

- **Java Documentation:** https://docs.oracle.com/javase/
- **MySQL Documentation:** https://dev.mysql.com/doc/
- **JDBC Guide:** https://www.oracle.com/java/technologies/persistence-jsp.html
- **Java Mail API:** https://javaee.github.io/javamail/
- **Java Swing Tutorial:** https://docs.oracle.com/javase/tutorial/uiswing/

---

**Last Updated: August 2026**
