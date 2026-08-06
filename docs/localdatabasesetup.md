# Local Database Setup (MySQL) - Portfolio Manager

This guide documents the exact steps to run the backend locally with a local MySQL database (without Docker).

## 1) Start MySQL

1. Open MySQL Workbench.
2. Connect to your local MySQL server.

## 2) Create Database and User

Run the following SQL in MySQL Workbench:

```sql
CREATE DATABASE portfolio_manager;

USE portfolio_manager;

SHOW TABLES;

CREATE USER IF NOT EXISTS 'pm_user'@'localhost' IDENTIFIED BY 'pm_password';
ALTER USER 'pm_user'@'localhost' IDENTIFIED BY 'pm_password';
GRANT ALL PRIVILEGES ON portfolio_manager.* TO 'pm_user'@'localhost';
FLUSH PRIVILEGES;
```

## 3) Set Environment Variables (PowerShell)

From project root, run:

```powershell
$env:DB_HOST='localhost'
$env:DB_NAME='portfolio_manager'
$env:DB_USER='pm_user'
$env:DB_PASSWORD='pm_password'
$env:CHATBOT_PROVIDER='groq'
$env:GROQ_API_KEY='dummy_groq_key'
$env:GEMINI_API_KEY='dummy_gemini_key'
```

## 4) Run Backend Locally

```powershell
mvn -f backend spring-boot:run
```

## 5) Expected Behavior

- Spring Boot starts on `http://localhost:8080`
- Flyway runs migrations against `portfolio_manager`
- Backend APIs become available for frontend proxy calls

## 6) If You Get Access Denied

Re-run the SQL user setup and ensure username/password in environment variables match exactly:

- `DB_USER=pm_user`
- `DB_PASSWORD=pm_password`

Also confirm MySQL server is running and reachable on `localhost:3306`.

## 7) Seed Large Demo Data (Optional)

If you want the dashboard charts to look realistic quickly, seed synthetic data from the local profile endpoint:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/dev/seed?investments=28&transactionsPerInvestment=40&snapshotDays=220&wipeExistingData=true"
```

This creates:

- 28 investments
- 1,120 transactions
- 6,160 historical price snapshots

You can tune query params as needed:

- `investments` range: 1 to 200
- `transactionsPerInvestment` range: 2 to 500
- `snapshotDays` range: 30 to 730
- `wipeExistingData=true` clears old investments, transactions, and snapshots before seeding
