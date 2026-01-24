package com.store.mgmt.shared.domain.exception;

/**
 * Base exception for all domain-level exceptions.
 * These represent violations of business rules.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
