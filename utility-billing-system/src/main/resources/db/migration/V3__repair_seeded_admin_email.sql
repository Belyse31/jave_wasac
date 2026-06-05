UPDATE app_users
SET email = 'belyse457@gmail.com',
    full_name = 'WASAC REG Administrator',
    email_verified = true,
    status = 'ACTIVE',
    password_change_required = false,
    password_expires_at = NULL
WHERE phone_number = '+250788000000'
  AND NOT EXISTS (
      SELECT 1
      FROM app_users existing_admin
      WHERE existing_admin.email = 'belyse457@gmail.com'
  );
