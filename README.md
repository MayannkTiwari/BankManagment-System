# Bank Management System

A server-rendered online banking application written in Java with Spring Boot. A customer applies for a savings or current account, receives a card number and PIN, and then signs in to deposit, withdraw, transfer to other accounts and read a statement.

This is a portfolio project and a rebuild of an earlier Java Swing desktop prototype. **No real money is held or moved.** The privacy policy and terms pages say so, and a notice appears in the footer while `APP_DEMO_MODE` is `true` (the default).

## Features

- Account application with server-side validation (18+ age check, PAN and Aadhaar format checks)
- Card number (16 digits, Luhn check digit) and 4-digit PIN issued once, at the end of the application
- Sign in with card number and PIN, automatic lock after 5 wrong attempts for 15 minutes
- Deposit, withdraw and transfer, with a per-transaction limit
- PIN required again for withdrawals, transfers and PIN changes
- Statement with running balance, paginated, printable from the browser
- Change PIN
- Privacy policy and terms pages, custom 403 and 404 pages, favicon

## Stack

| Layer | Choice |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Spring MVC, Spring Security, Spring Data JPA) |
| Views | Thymeleaf, one hand-written CSS file, no JavaScript |
| Database | MySQL 8.4, schema managed by Flyway |
| Build | Maven |

There is no front-end framework and no JavaScript at all, which is why the content security policy can be `default-src 'none'` with only same-origin styles and images allowed.

## Run it locally

You need JDK 21, Maven, and MySQL 8.4 (a `docker-compose.yml` is included for the database).

```text
1. Copy .env.example to .env.
2. Set DB_PASSWORD, MYSQL_ROOT_PASSWORD, APP_DATA_KEY, APP_PIN_PEPPER,
   APP_OPERATOR_NAME, APP_CONTACT_EMAIL and APP_JURISDICTION.
3. Start MySQL with Docker: docker compose up -d db
4. Export the same DB_* and APP_* values to your shell.
5. Start the app: mvn spring-boot:run
```

On Windows PowerShell, for example:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/bank?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="bank"
$env:DB_PASSWORD="your-db-password"
$env:APP_DATA_KEY="your-base64-key"
$env:APP_PIN_PEPPER="your-long-random-pepper"
$env:APP_OPERATOR_NAME="Your Name"
$env:APP_CONTACT_EMAIL="you@example.com"
$env:APP_JURISDICTION="India"
$env:APP_PUBLIC_URL="http://localhost:8080"
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

Open http://localhost:8080. `SPRING_PROFILES_ACTIVE=dev` (set in `.env.example`) turns off the Secure flag on the session cookie, which is needed because localhost is served over plain http.

In demo mode the application form accepts test values, for example PAN `ABCDE1234F` and Aadhaar `234567890123`.

Run the tests with `mvn test`. They need no database and no environment variables.

## Configuration

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL application connection |
| `MYSQL_ROOT_PASSWORD` | MySQL root password used only by Docker Compose |
| `APP_DATA_KEY` | 32 random bytes, base64. Encrypts the PAN |
| `APP_PIN_PEPPER` | At least 32 characters. Mixed into PIN hashes |
| `APP_OPERATOR_NAME`, `APP_CONTACT_EMAIL`, `APP_JURISDICTION` | Printed on the privacy policy and terms pages |
| `APP_PUBLIC_URL` | Public address of the site |
| `APP_BRAND_NAME` | Optional, defaults to "Bank Management System" |
| `APP_DEMO_MODE` | Optional, defaults to `true` |

None of the secrets have defaults, so the application refuses to start without them. Nothing sensitive is committed to the repository.

## Design decisions

**Money.** Amounts are `BigDecimal` stored as `decimal(19,2)`. Every operation that changes a balance locks the account row first (`SELECT ... FOR UPDATE`). Transfers lock both rows in ascending id order so two opposite transfers cannot deadlock. The ledger is insert-only and records the balance after each entry. The database also enforces `balance >= 0` and `amount > 0`.

**Double submits.** Each deposit, withdrawal and transfer form carries a request id, and the ledger has a unique constraint on `(account_id, request_id)`. Submitting the same form twice applies it once. The application form uses a one-time session token for the same reason.

**PINs.** A 4-digit PIN has only 10,000 values, so a plain bcrypt hash would not survive a database leak. The PIN is first run through HMAC-SHA256 with a pepper held in the environment, then hashed with bcrypt (cost 12). Wrong attempts are counted whether they happen at sign-in, at a withdrawal, at a transfer or at a PIN change. Failures are returned as results rather than thrown, so the counter is committed even though the operation is refused.

**Personal data.** PAN is encrypted with AES-256-GCM. Only the last four digits of the Aadhaar number are kept, and the rest is discarded when the form is submitted. Religion and category from the original form are no longer collected.

**Web security.** CSRF protection on every form, `HttpOnly` and `SameSite=Lax` session cookies, 15 minute idle timeout, no-store caching on authenticated pages, strict content security policy, generic sign-in error that does not reveal whether a card number exists or is locked.

## Changes from the original Swing version

- Rebuilt as a web application. The original login screen had empty button handlers.
- SQL built by string concatenation and a hardcoded MySQL root password are gone. Everything uses JPA with bound parameters, and credentials come from the environment.
- PINs were stored in plain text. They are now peppered hashes.
- Fixed Deposit and Recurring Deposit accounts were removed because there is no interest or maturity logic behind them. Savings and current accounts are fully working.
- The "services required" checkboxes (cheque book, mobile banking, email alerts and so on) were removed because none of them did anything.
- Senior citizen and existing account questions were removed. Age is derived from the date of birth.

## Project layout

```
src/main/java/dev/mayanktiwari/bank
  config/       properties, security filter chain, crypto beans, launch checks
  domain/       entities and enums
  repository/   Spring Data repositories
  security/     sign-in classes and the PIN encoder
  service/      banking rules, PIN rules, application, read-side queries
  support/      card numbers, PINs, encryption, formatting
  validation/   minimum-age constraint
  web/          controllers and form objects
src/main/resources
  db/migration/ Flyway schema
  templates/    Thymeleaf pages
  static/       stylesheet and favicons
src/test        unit tests for the rules above
```

## Known limitations

- One account per applicant through the UI, even though the schema allows more.
- No password reset: a forgotten PIN cannot be recovered, because there is no email or SMS integration.
- No admin area, interest calculation, or scheduled jobs.
- Deposits simply credit the account. A real system would receive funds from a payment rail.
- The legal pages are plain-language drafts for a demonstration project. If you ever operate this for real users, have a lawyer review them and check what licences apply to holding customer money.

## Deploying it (optional)

With `SPRING_PROFILES_ACTIVE=prod` the application checks at startup that `APP_PUBLIC_URL` is an `https://` address on your own domain, not a shared hosting address such as `*.onrender.com`, and that the contact email looks real. If not, it exits with an explanation. A `Dockerfile` is included.
