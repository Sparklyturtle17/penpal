package com.penpals.common.util;

/**
 * Shared regex patterns for Bean Validation (@Pattern).
 * These are compile-time String constants so they can be used in annotation attributes.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {}

    /** E.164 phone format: leading '+', country code, up to 15 digits total. e.g. +14155552671 */
    public static final String PHONE_E164 = "^\\+[1-9]\\d{1,14}$";
}
