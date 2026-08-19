# FinAI Backend - XAMPP MySQL Setup Guide

මෙම guide එක XAMPP එකේ MySQL database එක use කරලා backend run කරන්න සඳහා.

## 🚀 XAMPP Setup

### Step 1: XAMPP Start කරන්න

XAMPP Control Panel open කරන්න:
```bash
sudo /opt/lampp/lampp start
```

හෝ XAMPP Manager එකෙන්:
- Apache start කරන්න (optional - phpMyAdmin සඳහා)
- MySQL start කරන්න (required)

### Step 2: MySQL Running ද Verify කරන්න

```bash
# MySQL service status check කරන්න
sudo /opt/lampp/lampp status

# හෝ port 3306 listen වෙනවාද බලන්න
netstat -tlnp | grep 3306
```

### Step 3: phpMyAdmin Open කරන්න

Browser එකේ open කරන්න:
```
http://localhost/phpmyadmin
```

**Default Credentials:**
- Username: `root`
- Password: (empty) - password එකක් නැත්නම් blank තියන්න

### Step 4: Database Create කරන්න

phpMyAdmin එකේ:

**Option 1: Manual Creation**
1. "New" button එක click කරන්න (left sidebar එකේ)
2. Database name: `finai_dev`
3. Collation: `utf8mb4_unicode_ci` (recommended)
4. "Create" button එක click කරන්න

**Option 2: SQL Query**
"SQL" tab එකේ මේ query එක run කරන්න:
```sql
CREATE DATABASE IF NOT EXISTS finai_dev 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### Step 5: User Permissions Verify කරන්න (Optional)

Root user එකට full permissions තියනවා. නමුත් production එකට වෙනම user එකක් create කරන්න පුළුවන්:

```sql
-- New user create කරන්න
CREATE USER 'finai_user'@'localhost' IDENTIFIED BY 'your_secure_password';

-- Database permissions grant කරන්න
GRANT ALL PRIVILEGES ON finai_dev.* TO 'finai_user'@'localhost';

-- Reload privileges
FLUSH PRIVILEGES;
```

## ⚙️ Backend Configuration

### Current Configuration (application-dev.properties)

```properties
# XAMPP MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/finai_dev?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

### Password Configure කරන්න

XAMPP MySQL root user එකට password එකක් set කරලා තියනවා නම්:

1. `application-dev.properties` file එක open කරන්න
2. Password line එක update කරන්න:
```properties
spring.datasource.password=your_xampp_mysql_password
```

### XAMPP MySQL Default Port වෙනස් කරලා තියනවා නම්

XAMPP configuration එකේ MySQL port එක වෙනස් කරලා තියනවා නම් (default: 3306):

```properties
spring.datasource.url=jdbc:mysql://localhost:YOUR_PORT/finai_dev?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

## 🏃 Backend Run කරන්න

### Prerequisites

1. **Java 21 JDK Install කරන්න:**
```bash
sudo apt install openjdk-21-jdk
```

2. **JAVA_HOME Set කරන්න:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

3. **Verify Java:**
```bash
java -version
javac -version
```

### Run Backend

```bash
cd /media/bbs/30CC197DCC193E92/app/FinAI-Backend

# Development profile එකෙන් run කරන්න
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew bootRun

# හෝ specific profile එකක් specify කරන්න
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Backend එක `http://localhost:8080` එකේ start වෙයි!

### Verify Backend Connection

Backend start වෙද්දී console output එකේ මේ වගේ messages පෙන්වයි:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

Database connection හරි නම් errors නැතිව backend start වෙයි.

## 🔍 phpMyAdmin එකෙන් Data Verify කරන්න

Backend run වෙලා user registration/login කරපු පස්සේ:

1. phpMyAdmin open කරන්න: `http://localhost/phpmyadmin`
2. Left sidebar එකේ `finai_dev` database එක click කරන්න
3. Tables තියනවාද බලන්න:
   - `users` - User accounts
   - `roles` - User roles
   - `user_roles` - User-role mappings
   - Other tables...

4. Data view කරන්න:
   - `users` table එක click කරන්න
   - "Browse" tab එකේ registered users පෙන්වයි

## 🐛 Troubleshooting

### Issue 1: MySQL Connection Refused
```
Error: Connection refused
```

**Solution:**
```bash
# XAMPP MySQL start කරන්න
sudo /opt/lampp/lampp startmysql

# Port listen වෙනවාද check කරන්න
netstat -tlnp | grep 3306
```

### Issue 2: Access Denied for User 'root'
```
Error: Access denied for user 'root'@'localhost'
```

**Solution:**
1. phpMyAdmin එකෙන් login කරන්න පුළුවන්ද verify කරන්න
2. Password correct ද `application-dev.properties` එකේ check කරන්න
3. XAMPP MySQL password reset කරන්න:
```bash
sudo /opt/lampp/bin/mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### Issue 3: Database Not Found
```
Error: Unknown database 'finai_dev'
```

**Solution:**
Database manual ව create කරන්න:
```sql
CREATE DATABASE finai_dev;
```

හෝ `createDatabaseIfNotExist=true` parameter එක URL එකේ තියනවාද check කරන්න.

### Issue 4: Port Already in Use (3306)
```
Error: Port 3306 is already in use
```

**Solution:**
System MySQL service එකක් run වෙනවා නම් stop කරන්න:
```bash
sudo systemctl stop mysql
```

XAMPP MySQL පාවිච්චි කරන්න.

## 📊 Database Management

### Backup Database

phpMyAdmin එකෙන්:
1. `finai_dev` database select කරන්න
2. "Export" tab එක click කරන්න
3. Export method: "Quick"
4. Format: "SQL"
5. "Go" button click කරන්න

### Import Database

phpMyAdmin එකෙන්:
1. `finai_dev` database select කරන්න
2. "Import" tab එක click කරන්න
3. "Choose File" click කරලා SQL file select කරන්න
4. "Go" button click කරන්න

### Reset Database

Database එක completely reset කරන්න නම්:

```sql
DROP DATABASE finai_dev;
CREATE DATABASE finai_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Backend restart කරද්දී tables auto-create වෙයි (`spring.jpa.hibernate.ddl-auto=update` නිසා).

## 🔐 Security Notes

### Development (XAMPP)
- Empty password එක development සඳහා OK
- localhost access only

### Production
- **Strong password එකක් use කරන්න!**
- Separate user create කරන්න (root use නොකරන්න)
- SSL enable කරන්න
- Remote access disable කරන්න

## 📱 Mobile App Integration

Backend XAMPP එකෙන් run වෙද්දී mobile app එකේ base URL එක:

```dart
// lib/app/core/constants/app_constants.dart
static const String baseUrl = 'http://localhost:8080/api';
```

**Physical device එකකින් test කරනවා නම්:**
```dart
// Your computer IP address use කරන්න
static const String baseUrl = 'http://192.168.1.XXX:8080/api';
```

Your IP address හොයාගන්න:
```bash
hostname -I | awk '{print $1}'
```

## ✅ Quick Test

Backend හරියට run වෙනවාද test කරන්න:

```bash
# Health check endpoint
curl http://localhost:8080/api/health

# Expected response:
# {"success":true,"message":"Application is healthy","data":{...}}
```

## 📝 Summary

✅ XAMPP MySQL configuration හරියට set කරලා
✅ Database auto-create වෙන විදියට configure කරලා
✅ Development සඳහා simple credentials use කරනවා
✅ phpMyAdmin එකෙන් database manage කරන්න පුළුවන්
✅ Backend port 8080 එකේ run වෙනවා

**Next Steps:**
1. XAMPP start කරන්න
2. Backend run කරන්න
3. Mobile app එකෙන් register/login test කරන්න
4. phpMyAdmin එකෙන් data verify කරන්න

Happy Coding! 🚀
