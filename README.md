# 🏗️ BuildRight Equipment Rental System

A full-featured **Construction Machine Rental Management System** built with **Spring Boot 3.3** and **SQLite**.

---

## ⚡ QUICKEST WAY TO RUN (Windows)

**Double-click `run.bat`** in the project folder.

> If that doesn't work, follow the manual steps below.

---

## 🔧 Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | **17, 21, or 25** | https://adoptium.net |
| Maven | 3.8+ | Included via VS Code extension OR https://maven.apache.org |

Check your Java: open Command Prompt and run `java -version`

---

## 🚀 How to Run — 3 Methods

### Method 1: VS Code (Recommended for your setup)

1. **Extract** the zip to a folder (e.g. `C:\Projects\buildright`)
2. Open VS Code → **File → Open Folder** → select the `buildright` folder
3. VS Code will prompt: *"Install recommended extensions?"* — click **Install All**
   - This installs the Maven for Java and Spring Boot extensions
4. Wait for the Java extension to finish importing (bottom status bar shows progress)
5. When ready: open **`src/main/java/com/buildright/BuildRightApplication.java`**
6. You'll see a **▶ Run** button appear above the `main` method — click it
7. Open **http://localhost:8080** in your browser

**⚠️ IMPORTANT:** Do NOT use the green ▶ play button in the top-right corner of VS Code — that runs it without Maven dependencies. Use the **CodeLens "Run" link** directly above the `main` method.

### Method 2: Command Prompt / Terminal

```cmd
cd C:\path\to\buildright
run.bat
```
Or if you have Maven installed:
```cmd
mvn spring-boot:run
```

### Method 3: PowerShell

```powershell
cd C:\path\to\buildright
.\run.bat
```

---

## 🔐 Login Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | **Owner** — full access |
| `manager1` | `mgr123` | **Manager** — approve rentals |
| `cust1` | `cust123` | **Customer** — Maria Santos |
| `cust2` | `cust456` | **Customer** — Pedro Reyes |
| `op1` | `op1pass` | **Operator** |
| `drv1` | `drv1pass` | **Driver** |

---

## ❓ Common Errors & Fixes

### Error: `SpringApplication cannot be resolved`
**Cause:** VS Code is running the file directly instead of through Maven.  
**Fix:** Use the **"Run" CodeLens** above the `main` method, NOT the top-right ▶ button.  
Or run `run.bat` from Command Prompt.

### Error: `Port 8080 already in use`
Edit `src/main/resources/application.properties` and change:
```
server.port=8081
```

### Error: `Could not find or load main class`
Make sure you opened the **`buildright` folder** in VS Code (the one containing `pom.xml`), not the outer zip folder.

### Error: Maven not found
Install the **"Maven for Java"** VS Code extension or download Maven from https://maven.apache.org/download.cgi and add it to your PATH.

### Build fails with dependency errors
```cmd
mvn dependency:resolve
mvn clean spring-boot:run
```

---

## 🗄️ Database

- Uses **SQLite** — zero installation needed
- Database file `buildright.db` is auto-created in the project folder on first run
- Sample data (10 equipment items, 3 operators, 2 drivers, 9 users) loads automatically
- **To reset:** delete `buildright.db` and restart

---

## 📋 System Features

### Rental Workflow
```
PENDING → AWAITING_APPROVAL → APPROVED → ACTIVE → RETURNED
                                               ↘ REJECTED / CANCELLED
```
1. Customer creates rental → **PENDING**
2. Customer pays (GCash/Bank Transfer) → **AWAITING_APPROVAL**
3. Manager approves, auto-assigns operator & driver → **APPROVED**
4. Manager activates → **ACTIVE**
5. Manager finalizes return → **RETURNED**

### All Original Business Rules Preserved
- ✅ No cash — GCash or Bank Transfer only
- ✅ Payment must be before rental start date
- ✅ Date conflict detection (prevents double-booking)
- ✅ Cancellation requires ≥ 2 days before start
- ✅ Extension requests require manager approval
- ✅ 3-step completion: operator done → customer confirms → manager returns

---

## 🗂️ Project Structure

```
buildright/
├── pom.xml                    ← Maven config (Spring Boot 3.3 + SQLite)
├── run.bat                    ← Double-click to run on Windows
├── run.sh                     ← Run on Mac/Linux
├── README.md
├── .vscode/
│   ├── launch.json            ← VS Code run configuration
│   └── settings.json          ← Java/Maven settings
└── src/main/
    ├── java/com/buildright/
    │   ├── BuildRightApplication.java
    │   ├── config/            ← Spring Security, BCrypt
    │   ├── model/             ← 6 JPA entities
    │   ├── repository/        ← 6 Spring Data repos
    │   ├── service/           ← Business logic + data seeder
    │   └── controller/        ← 7 MVC controllers
    └── resources/
        ├── application.properties  ← SQLite config
        ├── static/css/app.css      ← Industrial dark theme
        ├── static/js/app.js        ← UI interactions
        └── templates/             ← 17 Thymeleaf pages
```

---

*BuildRight Equipment Rental System — Spring Boot 3.3 + SQLite + Thymeleaf*
