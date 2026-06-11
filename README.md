# Wallet Transaction API

A Spring Boot REST API for managing digital wallets with deposit and transfer capabilities.

---

## Tech Stack

| Layer        | Technology                   |
|--------------|-------------------------------|
| Language     | Java 17                       |
| Framework    | Spring Boot 3.2                |
| Persistence  | Spring Data JPA + Hibernate    |
| Database     | MySQL 8+                       |
| Build        | Maven                          |
| Validation   | Jakarta Bean Validation         |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Create the database
```bash
mysql -u root -p < src/main/resources/schema-init.sql
```
Or manually:
```sql
CREATE DATABASE walletdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/walletdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Run the application
```bash
mvn clean spring-boot:run
```
The server starts on **http://localhost:8080**. Hibernate creates the tables automatically on first boot (`ddl-auto=update`).

### Run tests
```bash
mvn clean test
```

---

## Project Structure

```
src/main/java/com/wallet/
├── WalletApplication.java          # Entry point
├── config/
│   └── DataSeeder.java             # Demo data on startup
├── controller/
│   └── WalletController.java       # REST endpoints
├── service/
│   ├── WalletService.java          # Interface
│   └── WalletServiceImpl.java      # Business logic
├── repository/
│   ├── WalletRepository.java
│   └── TransactionRepository.java
├── model/
│   ├── Wallet.java                 # Entity (id, ownerName, email, balance, status)
│   └── Transaction.java            # Entity (amount, type, balanceAfter, ...)
├── dto/
│   └── WalletDto.java              # All request/response DTOs
└── exception/
    ├── WalletException.java        # Custom exception hierarchy
    └── GlobalExceptionHandler.java # @RestControllerAdvice
```

---

## API Reference

Base path: `/api/v1/wallets`

### 1. Create Wallet
```
POST /api/v1/wallets
```
**Request body:**
```json
{
  "ownerName": "Alice Kumar",
  "email": "alice@example.com",
  "initialDeposit": 1000.00
}
```
**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": 1,
    "ownerName": "Alice Kumar",
    "email": "alice@example.com",
    "balance": 1000.00,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
}
```

---

### 2. Check Wallet Balance
```
GET /api/v1/wallets/{walletId}/balance
```
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Balance fetched",
  "data": {
    "walletId": 1,
    "ownerName": "Alice Kumar",
    "balance": 1000.00,
    "currency": "INR",
    "asOf": "2024-01-15T10:05:00"
  }
}
```

---

### 3. Add Money (Deposit)
```
POST /api/v1/wallets/{walletId}/deposit
```
**Request body:**
```json
{
  "amount": 500.00,
  "description": "Salary credit"
}
```
**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Money added successfully",
  "data": {
    "id": 5,
    "walletId": 1,
    "amount": 500.00,
    "balanceAfter": 1500.00,
    "type": "CREDIT",
    "status": "SUCCESS",
    "relatedWalletId": null,
    "relatedWalletOwner": null,
    "description": "Salary credit",
    "createdAt": "2024-01-15T10:10:00"
  }
}
```

---

### 4. Transfer Money
```
POST /api/v1/wallets/{walletId}/transfer
```
**Request body:**
```json
{
  "targetWalletId": 2,
  "amount": 200.00,
  "description": "Dinner split"
}
```
**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Transfer successful",
  "data": {
    "debit": {
      "id": 6,
      "walletId": 1,
      "amount": 200.00,
      "balanceAfter": 1300.00,
      "type": "TRANSFER_OUT",
      "status": "SUCCESS",
      "relatedWalletId": 2,
      "relatedWalletOwner": "Bob Sharma",
      "description": "Dinner split",
      "createdAt": "2024-01-15T10:15:00"
    },
    "credit": {
      "id": 7,
      "walletId": 2,
      "amount": 200.00,
      "balanceAfter": 700.00,
      "type": "TRANSFER_IN",
      "status": "SUCCESS",
      "relatedWalletId": 1,
      "relatedWalletOwner": "Alice Kumar",
      "description": "Dinner split",
      "createdAt": "2024-01-15T10:15:00"
    },
    "message": "Successfully transferred 200.00 from Alice Kumar to Bob Sharma"
  }
}
```

**Validations:**
- Source and target wallet must be different (no self-transfer).
- Both wallets must be `ACTIVE`.
- Source wallet must have sufficient balance.
- Both debit and credit operations are recorded atomically inside a single `@Transactional` method.

---

### 5. Transaction History
```
GET /api/v1/wallets/{walletId}/transactions
```
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Transactions fetched",
  "data": [
    {
      "id": 7,
      "walletId": 1,
      "amount": 200.00,
      "balanceAfter": 1300.00,
      "type": "TRANSFER_OUT",
      "status": "SUCCESS",
      "relatedWalletId": 2,
      "relatedWalletOwner": "Bob Sharma",
      "description": "Dinner split",
      "createdAt": "2024-01-15T10:15:00"
    },
    {
      "id": 5,
      "walletId": 1,
      "amount": 500.00,
      "balanceAfter": 1500.00,
      "type": "CREDIT",
      "status": "SUCCESS",
      "relatedWalletId": null,
      "relatedWalletOwner": null,
      "description": "Salary credit",
      "createdAt": "2024-01-15T10:10:00"
    }
  ]
}
```
Returns all transactions for the wallet, most recent first.

---

## Error Responses

All errors follow a consistent structure:
```json
{
  "success": false,
  "message": "Wallet not found with id: 99",
  "data": null
}
```

| Scenario                        | HTTP Status |
|----------------------------------|-------------|
| Wallet not found                 | 404         |
| Duplicate email / ownerName      | 409         |
| Insufficient balance             | 422         |
| Self-transfer / inactive wallet  | 400         |
| Validation failure               | 400         |
| Unexpected server error          | 500         |

---

## Sample cURL Commands

```bash
# Create wallet
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"ownerName":"Alice Kumar","email":"alice@example.com","initialDeposit":5000}'

# Check balance
curl http://localhost:8080/api/v1/wallets/1/balance

# Deposit
curl -X POST http://localhost:8080/api/v1/wallets/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":1000,"description":"Cash deposit"}'

# Transfer
curl -X POST http://localhost:8080/api/v1/wallets/1/transfer \
  -H "Content-Type: application/json" \
  -d '{"targetWalletId":2,"amount":300,"description":"Rent share"}'

# Transaction history
curl http://localhost:8080/api/v1/wallets/1/transactions
```

---

## Design Notes

- **Layered architecture:** Controller → Service → Repository → Database, with DTOs at every boundary.
- **Atomic transfers:** Both debit and credit operations run inside a single `@Transactional` method, so a failure rolls back both sides automatically.
- **Balance precision:** Stored as `DECIMAL(19,4)` to avoid floating-point rounding errors; all amounts use `BigDecimal`.
- **Transaction audit trail:** Every balance change (deposit, transfer-in, transfer-out) is recorded with `balanceAfter`, type, and optional description.
- **Validation:** Amounts must be `> 0`; emails must be valid; owner names 2–100 chars. All enforced via Jakarta Bean Validation.
- **Custom exceptions:** `WalletNotFoundException`, `DuplicateWalletException`, `InsufficientBalanceException`, `SelfTransferException`, `WalletNotActiveException` — all handled centrally by `GlobalExceptionHandler`.
- **Reduced API surface:** Only the 5 core endpoints listed above are exposed. Get-wallet-by-id, list-all-wallets, and paginated transaction history have been removed.
- **Production hardening:** Change `ddl-auto` from `update` to `validate` in production, and manage schema changes with Flyway or Liquibase instead.
