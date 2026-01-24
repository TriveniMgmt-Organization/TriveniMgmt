package com.store.mgmt.shared.domain.exception;

/**
 * Exception thrown when authorization fails at the domain level.
 */
public class AuthorizationException extends DomainException {

    private final String action;
    private final String resource;

    public AuthorizationException(String message) {
        super(message);
        this.action = null;
        this.resource = null;
    }

    public AuthorizationException(String action, String resource) {
        super(String.format("Not authorized to %s on %s", action, resource));
        this.action = action;
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }
}
