# WASAC/REG Utility Billing API Testing Guide

Base URL:

```text
http://127.0.0.1:8080
```

Swagger:

```text
http://127.0.0.1:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://127.0.0.1:8080/v3/api-docs
```

Default admin credentials:

```text
Email: belyse457@gmail.com
Password: Admin@12345
```

Important OTP note:

```text
Registration OTP is only for /api/auth/verify-otp.
Password reset OTP is only for /api/auth/reset-password.
To reset password, first call /api/auth/forgot-password and use the OTP from that email.
OTP expiry is 15 minutes by default.
```

## 1. Register Customer

Public endpoint. This sends one email only, containing the verification OTP.

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

Expected response:

```json
{
  "success": true,
  "message": "Registration successful. Verify the OTP sent to your email.",
  "data": null
}
```

## 2. Verify Registration OTP

Use the OTP from the email received during registration.

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

Expected response:

```json
{
  "success": true,
  "message": "Account verified successfully",
  "data": null
}
```

## 3. Login As Admin

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

Copy the `accessToken` from the response.

In Swagger, click `Authorize` and enter:

```text
Bearer YOUR_ACCESS_TOKEN
```

## 4. Create Staff User

Role required: `ROLE_ADMIN`

Allowed roles:

```text
ROLE_ADMIN
ROLE_OPERATOR
ROLE_FINANCE
```

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

## 5. Create Customer

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

Copy the returned customer `id`.

Delete staff user:

```http
DELETE /api/users/USER_ID_HERE
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

The default seeded admin account cannot be deleted.

Alternative passport sample:

```json
{
  "nationalId": "A1234567"
}
```

## 6. Search Customers

Role required: `ROLE_ADMIN`, `ROLE_OPERATOR`, or `ROLE_FINANCE`

```http
GET /api/customers?query=Jean&page=0&size=20&sortField=createdAt&sortDirection=desc
Authorization: Bearer ACCESS_TOKEN
```

## 7. Create Meter

Role required: `ROLE_ADMIN` or `ROLE_OPERATOR`

Use the customer `id` from step 5.

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

Copy the returned meter `id`.

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

## 8. Create Tariff

Role required: `ROLE_ADMIN`

The application now seeds default active tariffs on startup:

```text
WATER: rate 500 FRW, fixed charge 1000 FRW
ELECTRICITY: rate 300 FRW, fixed charge 1500 FRW
```

You only need this step when you want to replace the seeded defaults with your own tariff.

Tariffs are versioned by effective date:

```text
Tariff A effectiveFrom = 2026-01-01
Tariff B effectiveFrom = 2026-07-01
June 2026 bills use Tariff A.
July 2026 and later bills use Tariff B.
```

Flat tariff:

```http
POST /api/config/tariffs
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "name": "Water Residential Tariff",
  "meterType": "WATER",
  "model": "FLAT",
  "rate": 500,
  "fixedCharge": 1000,
  "effectiveFrom": "2026-07-01",
  "effectiveTo": null,
  "tiers": []
}
```

Tiered tariff:

```json
{
  "name": "Water Tiered Tariff",
  "meterType": "WATER",
  "model": "TIERED",
  "rate": 500,
  "fixedCharge": 1000,
  "effectiveFrom": "2026-07-01",
  "effectiveTo": null,
  "tiers": [
    {
      "fromUnits": 0,
      "toUnits": 10,
      "rate": 350
    },
    {
      "fromUnits": 11,
      "toUnits": 30,
      "rate": 500
    },
    {
      "fromUnits": 31,
      "toUnits": null,
      "rate": 700
    }
  ]
}
```

## 9. Create Tax

Role required: `ROLE_ADMIN`

VAT rate `0.18` means 18 percent.

```http
POST /api/config/taxes
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "name": "VAT",
  "rate": 0.18,
  "effectiveFrom": "2026-07-01",
  "effectiveTo": null
}
```

## 10. Create Penalty

Role required: `ROLE_ADMIN`

Percentage penalty:

```http
POST /api/config/penalties
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "name": "Late Payment Penalty",
  "type": "PERCENTAGE",
  "value": 0.05,
  "effectiveFrom": "2026-07-01",
  "effectiveTo": null
}
```

Fixed penalty:

```json
{
  "name": "Fixed Late Fee",
  "type": "FIXED",
  "value": 2000,
  "effectiveFrom": "2026-07-01",
  "effectiveTo": null
}
```

## 11. Capture Meter Reading

Role required: `ROLE_ADMIN` or `ROLE_OPERATOR`

Use the meter `id` from step 7.

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

Copy the returned reading `id`.

Rules:

```text
Current reading must be greater than previous reading.
Only one reading is allowed per meter per month.
Reading date cannot be in the future.
Meter must be ACTIVE.
```

## 12. Generate Bill

Role required: `ROLE_ADMIN` or `ROLE_FINANCE`

Use the reading `id` from step 11.

Do not use the meter `id` here. Use the `id` returned by `POST /api/meter-readings`.

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

Copy the returned bill `id`.

## 13. Approve Bill

Role required: `ROLE_FINANCE`

```http
PATCH /api/bills/BILL_ID_HERE/approve
Authorization: Bearer FINANCE_ACCESS_TOKEN
```

After approval, the customer receives an email with the bill reference, billing month, final outstanding amount to pay, and due date.

## 14. Record Payment

Role required: `ROLE_FINANCE`

```http
POST /api/payments
Authorization: Bearer FINANCE_ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "billId": "BILL_ID_HERE",
  "amountPaid": 15000,
  "paymentMethod": "MOMO",
  "paymentDate": "2026-06-05"
}
```

Payment methods:

```text
MOMO
BANK
CARD
CASH
```

Rules:

```text
Amount must be greater than zero.
Payment date cannot be in the future.
Payment cannot exceed outstanding balance.
If balance becomes zero, bill status becomes PAID.
If balance remains greater than zero, bill status becomes PARTIALLY_PAID.
```

## 15. Forgot Password

This sends a password-reset OTP. Do not use the registration OTP for reset password.

```http
POST /api/auth/forgot-password
Content-Type: application/json
```

```json
{
  "email": "muhirejessica@gmail.com"
}
```

## 16. Reset Password

Use the OTP from forgot-password email.

```http
POST /api/auth/reset-password
Content-Type: application/json
```

```json
{
  "email": "muhirejessica@gmail.com",
  "otp": "123456",
  "newPassword": "Muhire@123"
}
```

## 17. Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "REFRESH_TOKEN_FROM_LOGIN"
}
```

## 18. Logout

```http
POST /api/auth/logout
Authorization: Bearer ACCESS_TOKEN
Content-Type: application/json
```

```json
{
  "refreshToken": "REFRESH_TOKEN_FROM_LOGIN"
}
```

## 19. Upload File

Role required: any authenticated role.

Use Swagger for this endpoint because it is multipart form data.

```text
POST /api/files
file: choose a PDF, PNG, or JPEG
category: PROFILE_PICTURE
ownerReference: optional customer or bill reference
```

After upload, copy the returned `id`. That exact `id` is required for download or delete.

List uploaded files:

```http
GET /api/files
Authorization: Bearer ACCESS_TOKEN
```

Delete uploaded file:

```http
DELETE /api/files/FILE_ID_FROM_UPLOAD_OR_LIST
Authorization: Bearer ACCESS_TOKEN
```

Do not use random UUID examples shown by Swagger. They are samples only and will return `File not found`.

Allowed categories:

```text
PROFILE_PICTURE
CUSTOMER_DOCUMENT
UTILITY_DOCUMENT
BILL_ATTACHMENT
```

## 20. Common Validation Failures

Duplicate email:

```text
409 Conflict
Email is already registered
```

Duplicate phone:

```text
409 Conflict
Phone number is already registered
```

Invalid OTP:

```text
400 Bad Request
Invalid OTP
```

Expired OTP:

```text
400 Bad Request
OTP has expired
```

Wrong role:

```text
403 Forbidden
Access denied
```

No token:

```text
401 Unauthorized
```

## 21. Recommended Exam Demo Order

1. Login as admin.
2. Create operator user.
3. Create finance user.
4. Create customer.
5. Create meter for customer.
6. Configure tariff.
7. Configure tax.
8. Capture meter reading.
9. Generate bill.
10. Login as finance.
11. Approve bill.
12. Record partial payment.
13. Record remaining payment.
14. Show bill status becomes `PAID`.
