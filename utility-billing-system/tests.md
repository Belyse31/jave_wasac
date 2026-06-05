# WASAC/REG Utility Billing System - Testing and Explanation Guide

Use this file when testing the APIs in Swagger and when explaining the project to teachers.

Base URL:

```text
http://127.0.0.1:8080
```

Swagger UI:

```text
http://127.0.0.1:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://127.0.0.1:8080/v3/api-docs
```

Default admin:

```text
Email: belyse457@gmail.com
Password: Admin@12345
```

## 1. Project Summary

This is a Spring Boot backend for a utility billing system for WASAC and REG in Rwanda.

The system manages:

- Users and roles
- Customer registration and verification
- Customers
- Utility meters
- Meter readings
- Versioned tariffs
- Taxes
- Penalties
- Bill generation
- Bill approval
- Payments
- Email notifications
- File uploads
- Audit logs

The backend uses:

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Mail
- Lombok
- Jakarta Validation
- Swagger/OpenAPI
- Maven

## 2. Main System Flow

The normal business flow is:

```text
Admin logs in
Admin creates staff users
Admin or operator creates customers
Admin or operator assigns meters to customers
Admin configures tariffs, taxes, and penalties
Operator records meter readings
Admin or finance generates bills
Finance approves bills
Customer receives approved bill by email
Finance records payments
Bill balance and status update automatically
```

## 3. Roles

```text
ROLE_ADMIN
ROLE_OPERATOR
ROLE_FINANCE
ROLE_CUSTOMER
```

Role meaning:

- `ROLE_ADMIN`: manages users, tariffs, taxes, penalties, reports, and setup.
- `ROLE_OPERATOR`: manages customers, meters, and meter readings.
- `ROLE_FINANCE`: approves bills and records payments.
- `ROLE_CUSTOMER`: views personal bills and payment history.

## 4. Authentication Test

### 4.1 Login As Admin

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "belyse457@gmail.com",
  "password": "Admin@12345"
}
```

Expected:

- Response contains `accessToken`.
- Copy the token.
- In Swagger, click `Authorize`.
- Paste:

```text
Bearer ACCESS_TOKEN_HERE
```

Token duration:

```text
Normal access token: 8 hours
Finance access token: 24 hours
Refresh token: 7 days
```

After changing token settings or restarting the backend, login again and paste the new token in Swagger.

## 5. Customer Registration Test

Only customers can self-register. Admin, operator, and finance users are created by admin.

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Jessica Muhire",
  "email": "muhirejessica@gmail.com",
  "phoneNumber": "0781234577",
  "password": "Password@123"
}
```

Expected:

```text
Registration successful. Verify the OTP sent to your email.
```

Important:

- Customer receives only one registration email.
- That email contains the OTP verification code.
- OTP expires after 15 minutes.

### 5.1 Verify OTP

```http
POST /api/auth/verify-otp
Content-Type: application/json
```

```json
{
  "email": "muhirejessica@gmail.com",
  "otp": "123456"
}
```

Expected:

```text
Account verified successfully
```

## 6. Staff Creation Test

Role required: `ROLE_ADMIN`

```http
POST /api/users
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "firstName": "Alice",
  "lastName": "Mukamana",
  "email": "operator@gmail.com",
  "phoneNumber": "0781234567",
  "role": "ROLE_OPERATOR"
}
```

Expected:

- Staff user is created.
- Temporary password is generated.
- Staff user receives login credentials by email.
- Staff must change password on first login.

## 7. Customer CRUD Test

Role required: `ROLE_ADMIN` or `ROLE_OPERATOR`

```http
POST /api/customers
Authorization: Bearer ADMIN_OR_OPERATOR_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "fullName": "Jean Mutabazi",
  "nationalId": "1199988776655443",
  "email": "jean.mutabazi@gmail.com",
  "phoneNumber": "0791234567",
  "address": "Kigali, Rwanda",
  "dateOfBirth": "1995-05-10",
  "status": "ACTIVE"
}
```

Expected:

- Customer is created.
- National ID must be unique.
- Email must be unique.
- Phone number must be unique.
- Date of birth cannot be in the future.
- Customer must be greater than 16 years old.

Passport sample:

```json
{
  "nationalId": "A1234567"
}
```

## 8. Meter Test

Role required: `ROLE_ADMIN` or `ROLE_OPERATOR`

```http
POST /api/meters
Authorization: Bearer ADMIN_OR_OPERATOR_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "meterNumber": "WM-10001",
  "meterType": "WATER",
  "installationDate": "2026-01-15",
  "status": "ACTIVE",
  "customerId": "CUSTOMER_ID_HERE"
}
```

Expected:

- Meter is created.
- Meter number must be unique.
- Meter belongs to one customer.
- Customer may own many meters.

Electricity meter sample:

```json
{
  "meterNumber": "EM-10001",
  "meterType": "ELECTRICITY",
  "installationDate": "2026-01-15",
  "status": "ACTIVE",
  "customerId": "CUSTOMER_ID_HERE"
}
```

## 9. Tariff Versioning Test

Role required: `ROLE_ADMIN`

The system supports tariff history. A new tariff does not change old bills.

Example logic:

```text
Tariff A starts on 2026-01-01
Tariff B starts on 2026-07-01
June 2026 bill uses Tariff A
July 2026 bill uses Tariff B
```

Create a new tariff:

```http
POST /api/config/tariffs
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "name": "Water July Tariff",
  "meterType": "WATER",
  "model": "FLAT",
  "rate": 600,
  "fixedCharge": 1000,
  "effectiveFrom": "2026-07-01",
  "effectiveTo": null,
  "tiers": []
}
```

Expected:

- New tariff version is created.
- Previous tariff is closed using `effectiveTo`.
- Billing uses the tariff that matches the billing month.

## 10. Meter Reading Test

Role required: `ROLE_ADMIN` or `ROLE_OPERATOR`

```http
POST /api/meter-readings
Authorization: Bearer ADMIN_OR_OPERATOR_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "meterId": "METER_ID_HERE",
  "previousReading": 500,
  "currentReading": 650,
  "readingDate": "2026-05-31"
}
```

Expected:

- Consumption is calculated as `currentReading - previousReading`.
- Example: `650 - 500 = 150`.
- Only one reading per meter per month is allowed.
- Current reading must be greater than previous reading.
- Reading date cannot be in the future.

## 11. Bill Generation Test

Role required: `ROLE_ADMIN` or `ROLE_FINANCE`

Use the `id` returned by `POST /api/meter-readings`. Do not use the meter ID.

```http
POST /api/bills/generate
Authorization: Bearer ADMIN_OR_FINANCE_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "readingId": "READING_ID_HERE",
  "dueDate": "2026-06-30"
}
```

Expected:

- Bill is generated.
- Consumption charge is calculated.
- Fixed charge is added.
- Tax is added.
- Penalty is added only if overdue.
- Outstanding balance equals total amount.
- Duplicate bill for the same meter/month/year is rejected.

Bill calculation formula:

```text
Consumption = Current Reading - Previous Reading
Consumption Charge = Consumption * Tariff Rate
Taxable Amount = Consumption Charge + Fixed Charge
Tax Amount = Taxable Amount * Tax Rate
Total Amount = Taxable Amount + Tax Amount + Penalty
Outstanding Balance = Total Amount - Payments
```

## 12. Bill Approval Email Test

Role required: `ROLE_FINANCE`

```http
PATCH /api/bills/BILL_ID_HERE/approve
Authorization: Bearer FINANCE_ACCESS_TOKEN
```

Expected:

- Bill is marked as approved.
- Bill cannot be approved twice.
- Customer receives an email after approval.
- The email contains:
  - Customer name
  - Bill reference
  - Billing month and year
  - Final amount to pay
  - Due date

This is implemented in:

```text
BillServiceImpl.approve()
EmailServiceImpl.sendApprovedBillEmail()
```

The email is sent to:

```text
bill.getCustomer().getEmail()
```

## 13. Payment Test

Role required: `ROLE_FINANCE`

```http
POST /api/payments
Authorization: Bearer FINANCE_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "billId": "BILL_ID_HERE",
  "amountPaid": 10000,
  "paymentMethod": "MOMO",
  "paymentDate": "2026-06-05"
}
```

Expected:

- Payment is recorded.
- Overpayment is rejected.
- Payment date cannot be in the future.
- Bill outstanding balance is reduced.
- If balance becomes zero, bill status becomes `PAID`.
- If balance remains above zero, bill status becomes `PARTIALLY_PAID`.
- Customer receives payment confirmation email.

## 14. File Upload Test

Role required: authenticated user

```http
POST /api/files
Authorization: Bearer ACCESS_TOKEN
Content-Type: multipart/form-data
```

Use file category examples:

```text
PROFILE_PICTURE
CUSTOMER_DOCUMENT
UTILITY_DOCUMENT
BILL_ATTACHMENT
```

Expected:

- File is uploaded.
- File type and size are validated.
- API returns file ID.
- Use that real file ID to download or delete.

## 15. Important Validation Tests

Try these negative tests to prove validations work:

Duplicate customer national ID:

```text
Create two customers with the same nationalId.
Expected: duplicate resource error.
```

Invalid phone number:

```text
Use 0711234567.
Expected: validation error.
Valid examples start with 072, 073, 078, or 079.
```

Invalid meter reading:

```text
previousReading = 500
currentReading = 450
Expected: current reading must be greater than previous reading.
```

Duplicate monthly reading:

```text
Create two readings for the same meter in May 2026.
Expected: only one reading per meter per month.
```

Duplicate bill:

```text
Generate two bills for the same meter and month.
Expected: duplicate bill error.
```

Overpayment:

```text
Bill balance = 15000
Payment amount = 20000
Expected: overpayment rejected.
```

Future customer date of birth:

```text
Use dateOfBirth = 2030-01-01.
Expected: Date of birth cannot be a future date.
```

Customer exactly 16 or younger:

```text
Use a dateOfBirth that makes the customer 16 years old or younger.
Expected: Customer must be greater than 16 years old.
```

Approve bill twice:

```text
Approve the same bill two times.
Expected: bill is already approved.
```

## 16. Codebase Architecture

The project follows layered architecture.

### Controller Layer

Package:

```text
com.wasac.utilitybilling.controller
```

Purpose:

- Receives HTTP requests.
- Validates DTO request bodies using `@Valid`.
- Applies role security using `@PreAuthorize`.
- Returns API responses.

Example:

```text
BillController
```

It exposes bill endpoints like generate bill, approve bill, and search bills.

### Service Layer

Package:

```text
com.wasac.utilitybilling.service
com.wasac.utilitybilling.service.impl
```

Purpose:

- Contains business logic.
- Applies business rules.
- Calls repositories.
- Sends emails.
- Records audit logs.

Example:

```text
BillServiceImpl
```

It calculates bills, validates duplicate bills, selects tariffs, applies taxes, and sends approval email.

### Repository Layer

Package:

```text
com.wasac.utilitybilling.repository
```

Purpose:

- Communicates with PostgreSQL.
- Uses Spring Data JPA.
- Provides query methods and custom JPQL queries.

Example:

```text
TariffRepository.findEffectiveTariff()
```

It finds the tariff version that applies to the billing month.

### Entity Layer

Package:

```text
com.wasac.utilitybilling.entity
```

Purpose:

- Represents database tables.
- Uses JPA annotations like `@Entity`, `@Table`, `@Column`, and relationships.

Important entities:

- `User`
- `Customer`
- `Meter`
- `MeterReading`
- `Tariff`
- `Tax`
- `Penalty`
- `Bill`
- `Payment`
- `Notification`
- `AuditLog`
- `StoredFile`

### DTO Layer

Package:

```text
com.wasac.utilitybilling.dto
```

Purpose:

- Defines request and response objects.
- Prevents exposing entities directly through APIs.
- Holds validation annotations.
- Holds simple Swagger examples.

Example:

```text
BillingDtos.BillGenerateRequest
```

### Security Layer

Package:

```text
com.wasac.utilitybilling.security
```

Purpose:

- Handles JWT creation and validation.
- Loads users for authentication.
- Filters requests before controllers.

Important classes:

- `JwtService`
- `JwtAuthenticationFilter`
- `AppUserDetailsService`

### Config Layer

Package:

```text
com.wasac.utilitybilling.config
```

Purpose:

- Configures security.
- Configures Swagger.
- Seeds default admin and billing setup.

Important classes:

- `SecurityConfig`
- `OpenApiConfig`
- `DataInitializer`

### Exception Layer

Package:

```text
com.wasac.utilitybilling.exception
```

Purpose:

- Handles errors globally.
- Converts exceptions into friendly API responses.

Important classes:

- `GlobalExceptionHandler`
- `ResourceNotFoundException`
- `DuplicateResourceException`
- `BusinessRuleException`
- `InvalidOtpException`

### Utility Layer

Package:

```text
com.wasac.utilitybilling.util
```

Purpose:

- Contains reusable helper logic.

Example:

```text
PageUtils
```

It builds pagination and sorting objects.

## 17. Important Business Logic

### Customer Registration

```text
Customer submits name, email, phone, and password.
System validates email, phone, and password strength.
System saves inactive customer user.
System generates OTP.
System sends OTP email.
Customer verifies OTP.
Account becomes active.
```

### Staff Creation

```text
Admin creates staff user.
System generates temporary password.
System hashes password using BCrypt.
System sets passwordChangeRequired = true.
System emails credentials to staff.
Staff logs in and changes password.
```

### Meter Reading

```text
Operator selects active meter.
Operator records previous and current reading.
System checks current > previous.
System checks reading date is not future.
System checks only one reading exists for meter/month/year.
System saves reading.
```

### Bill Generation

```text
System loads meter reading.
System checks customer is active.
System checks duplicate bill does not exist.
System finds tariff by meter type and billing month.
System calculates consumption.
System calculates charges, tax, penalty, total amount, and balance.
System saves bill.
```

### Bill Approval

```text
Finance user approves bill.
System checks bill exists.
System checks bill is not already approved.
System marks bill as approved.
System emails final bill amount to the customer.
System records audit log.
```

### Payment

```text
Finance user records payment.
System checks bill exists.
System checks amount is greater than zero.
System rejects overpayment.
System subtracts payment from outstanding balance.
System changes status to PAID or PARTIALLY_PAID.
System emails payment confirmation.
System records audit log.
```

## 18. How To Explain Tariff Versioning

Tariffs are not overwritten because old bills must keep old prices.

Instead:

```text
Every tariff has:
effectiveFrom
effectiveTo
version
active
```

When a new tariff starts, the previous tariff is closed:

```text
Old tariff effectiveTo = new tariff effectiveFrom - 1 day
```

Bill generation uses the billing month:

```text
billingDate = first day of reading month
```

Then it selects:

```text
effectiveFrom <= billingDate
and
effectiveTo is null OR effectiveTo >= billingDate
```

That is why:

```text
June 2026 bill uses the tariff active in June.
July 2026 bill uses the tariff active in July.
```

## 19. How To Explain Email Notifications

Emails are sent by `EmailServiceImpl`.

Important email events:

- OTP verification email during registration
- Password reset email
- Staff credentials email
- Bill processed email
- Approved bill email
- Payment confirmation email

The approved bill email is important because it tells the customer exactly how much to pay after finance approves the bill.

## 20. How To Explain Security

The system uses JWT authentication.

Flow:

```text
User logs in with email and password.
Server validates credentials.
Server returns access token and refresh token.
Client sends access token in Authorization header.
JwtAuthenticationFilter validates token on every request.
Spring Security checks roles using @PreAuthorize.
Controller executes only when the role is allowed.
```

Example:

```java
@PreAuthorize("hasRole('FINANCE')")
```

This means only finance users can access that endpoint.

## 21. How To Explain DTO Pattern

Entities are database objects.

DTOs are API objects.

The system does not expose entities directly because:

- Entities may contain sensitive fields like password hash.
- API responses should be controlled.
- Validation belongs on request DTOs.
- DTOs make Swagger documentation cleaner.

Example:

```text
User entity contains passwordHash.
UserResponse DTO does not expose passwordHash.
```

## 22. How To Explain Error Handling

All exceptions are handled in one place:

```text
GlobalExceptionHandler
```

Examples:

- Missing record returns friendly not found error.
- Duplicate record returns conflict error.
- Invalid business rule returns bad request.
- Validation errors return field messages.
- Unauthorized requests return security error.

## 23. Quick Teacher Demo Order

Use this order for a smooth presentation:

1. Open Swagger.
2. Login as admin.
3. Create staff user and explain email credentials.
4. Create customer.
5. Create meter.
6. Capture meter reading.
7. Generate bill.
8. Login as finance user.
9. Approve bill.
10. Show that customer receives approved bill email.
11. Record partial payment.
12. Record remaining payment.
13. Show bill status becomes `PAID`.
14. Show validation examples like duplicate meter reading or overpayment.

## 24. Database-Level Routines

The project includes PostgreSQL database routines managed by Flyway migrations.

Important file:

```text
src/main/resources/db/migration/V4__billing_payment_notification_routines.sql
```

### Bill Generation Trigger

When a new row is inserted into the `bills` table, PostgreSQL runs:

```text
create_bill_notification()
```

This trigger inserts a notification into the `notifications` table.

Message format:

```text
Dear <CustomerName>,
Your <Month/Year> utility bill of <Amount> FRW has been successfully processed.
```

This proves that bill notification creation is handled at database level too, not only in Java service code.

### Full Payment Trigger

When a new row is inserted into the `payments` table, PostgreSQL runs:

```text
update_bill_after_payment()
```

This routine:

- Calculates total payments made for the bill.
- Updates the bill outstanding balance.
- Sets bill status to `PARTIALLY_PAID` when some balance remains.
- Sets bill status to `PAID` when balance reaches zero.
- Inserts a customer notification when the bill becomes fully paid.

### Why This Is Useful

Even if a payment is inserted directly into the database, the database still protects the bill status and notification behavior.

This is important for exams because it shows:

- Trigger usage
- Stored function usage
- Database-level business automation
- Notification creation without depending only on controller logic

## 25. Common Issues During Testing

### 401 Unauthorized

Meaning:

```text
The token is missing, invalid, expired, or was created before the latest backend restart/config change.
```

Fix:

```text
Login again and use a fresh token in Swagger Authorize.
```

### 403 Forbidden

Meaning:

```text
Your token is valid, but your role is not allowed to use the endpoint.
```

Fix:

```text
Use a user with the required role.
```

### Meter reading not found during bill generation

Meaning:

```text
You used meter ID instead of meter reading ID.
```

Fix:

```text
Use the id returned from POST /api/meter-readings.
```

### Email not received

Check:

```text
Use a real email address.
Check Spam or Promotions.
Make sure SMTP configuration is correct.
```

### File delete returns 404

Meaning:

```text
The ID does not belong to an uploaded file.
```

Fix:

```text
First upload a file, then use the returned file id.
```
