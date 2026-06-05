package com.wasac.utilitybilling.dto;

public final class ValidationPatterns {
    public static final String LOWERCASE_EMAIL = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
    public static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
    public static final String RWANDA_PHONE = "^07[2389]\\d{7}$";
    public static final String NATIONAL_ID_OR_PASSPORT = "^(\\d{16}|[A-Z]{1,2}\\d{6,9})$";
    public static final String METER_NUMBER = "^[A-Z0-9-]{4,64}$";

    private ValidationPatterns() {
    }
}
