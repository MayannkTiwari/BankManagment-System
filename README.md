# Bank Management System

A full-stack web-based Bank Management System built with **Java, Spring Boot, Spring Security, Thymeleaf, MySQL, and Maven**.

The project provides a secure banking workflow for customer account applications, authentication, PIN management, deposits, withdrawals, transfers, and transaction statements.

## Features

- Customer account application
- Automatic account and card number generation
- Secure PIN-based authentication
- Spring Security authentication and authorization
- Failed-login protection with temporary account locking
- Deposit and withdrawal operations
- Account-to-account money transfer
- Transaction statement with pagination
- PIN change functionality
- Transaction limits and validation
- Encrypted sensitive account data
- PIN hashing with an application-level pepper
- Database versioning with Flyway
- Global error pages for common HTTP errors
- Privacy Policy and Terms & Conditions pages
- Responsive web interface using Thymeleaf and CSS
- Automated Maven build through GitHub Actions
- Docker support for application and database setup

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Web | Spring MVC |
| Template Engine | Thymeleaf |
| Security | Spring Security |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Database Migration | Flyway |
| Build Tool | Maven |
| Containerization | Docker / Docker Compose |
| CI | GitHub Actions |
| Frontend | HTML, CSS, Thymeleaf |
| JavaScript | Not required |

## Architecture

The application follows a layered architecture:

```text
Browser
   │
   ▼
Thymeleaf Views
   │
   ▼
Controllers
   │
   ▼
Services
   │
   ├── Business Logic
   ├── Validation
   ├── Security
   └── Transaction Processing
   │
   ▼
Repositories
   │
   ▼
MySQL Database
```

### Main packages

```text
dev.mayanktiwari.bank
│
├── config
├── domain
├── repository
├── security
├── service
├── support
├── validation
└── web
```

## Security

Security is an important part of the application rather than an afterthought.

The project includes:

- Spring Security authentication
- PIN hashing
- Application-level PIN pepper
- Encryption for sensitive data
- Failed-login tracking
- Temporary account locking
- Server-side validation
- CSRF protection through Spring Security
- Environment-based configuration for secrets
- No application credentials committed to Git

Sensitive configuration is supplied through environment variables rather than being hardcoded in the source code.

## Database

The application uses **MySQL 8** and **Flyway** for database schema management.

The initial schema is located at:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

Flyway automatically applies database migrations when the application starts.

## Requirements

Before running the project locally, install:

- Java 21 or compatible JDK
- Maven
- MySQL 8
- Git

Docker can also be used instead of installing MySQL manually.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/MayannkTiwari/BankManagment-System.git
cd BankManagment-System
```

### 2. Create the database

Open MySQL and create the application database:

```sql
CREATE DATABASE bank_management_system;
```

### 3. Configure environment variables

Copy the example environment configuration:

```text
.env.example
```

Set the required environment variables for your local machine.

Example:

```text
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:mysql://localhost:3306/bank_management_system
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password

APP_DATA_KEY=your_base64_encoded_32_byte_key
APP_PIN_PEPPER=your_long_random_secret

APP_OPERATOR_NAME=Your Name
APP_CONTACT_EMAIL=your-email@example.com
APP_JURISDICTION=India
APP_PUBLIC_URL=http://localhost:8080
```

**Do not commit `.env` or real credentials to Git.**

### 4. Build the project

Using Maven:

```bash
mvn clean package
```

### 5. Run the application

```bash
mvn spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

## Docker

The project also contains Docker configuration.

Build and start the application with:

```bash
docker compose up --build
```

To stop the containers:

```bash
docker compose down
```

Environment variables should still be configured according to the project's environment configuration.

## Testing

The project includes unit tests covering areas such as:

- Banking operations
- PIN handling
- Card number generation
- Data encryption
- Formatting utilities
- Age validation
- Application services
- Application startup checks

Run the test suite with:

```bash
mvn test
```

## CI

GitHub Actions is configured to automatically build the project.

Workflow:

```text
.github/workflows/build.yml
```

The CI pipeline verifies that the project can be built successfully using Maven.

## Project Structure

```text
BankManagment-System/
│
├── .github/
│   └── workflows/
│       └── build.yml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/mayanktiwari/bank/
│   │   │
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── static/
│   │       └── templates/
│   │
│   └── test/
│       └── java/
│
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Application Modules

### Account Application

Customers can submit an application to open a bank account.

The application performs server-side validation before creating the account.

### Authentication

Customers authenticate using their account credentials and PIN.

The application also tracks failed authentication attempts and can temporarily lock accounts after repeated failures.

### Banking Operations

Authenticated users can perform:

- Deposits
- Withdrawals
- Transfers
- PIN changes

Each operation is validated before being persisted.

### Transaction Statement

Customers can view their transaction history through the statement section.

Transactions are stored as ledger entries and displayed through a paginated interface.

## Design Goals

The project was developed with the following goals:

1. Keep the application simple enough to understand and maintain.
2. Separate web, business, security, and persistence responsibilities.
3. Keep sensitive configuration outside the source code.
4. Use database migrations instead of relying on automatic schema generation.
5. Validate financial operations on the server.
6. Maintain a clean Git history and reproducible build process.
7. Keep the application deployable through both Maven and Docker.

## Current Status

**Development Status: Functional local build**

The application has been configured and tested for local execution using:

- Spring Boot
- Java
- MySQL
- Maven
- Flyway

The project is currently intended for **educational and portfolio purposes** and should not be considered production banking software.

## Disclaimer

This project is an educational implementation of a banking system.

It is not intended to process real financial transactions or store real banking/customer information.

Do not use real passwords, PINs, banking credentials, or sensitive personal information when testing the application.

## Author

**Mayank Tiwari**

B.Tech - Computer Science and Engineering (Artificial Intelligence)

Galgotias College of Engineering & Technology

GitHub:  
https://github.com/MayannkTiwari

## License

This project is intended for educational and portfolio purposes.
