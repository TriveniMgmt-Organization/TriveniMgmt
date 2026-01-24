package com.store.mgmt.shared.domain.exception;

/**
 * Exception thrown when domain validation fails.
 */
public class ValidationException extends DomainException {

    private final String field;

    public ValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
