package com.store.mgmt.modules.inventory.domain.exception;

import com.store.mgmt.shared.domain.exception.ValidationException;

/**
 * Exception thrown when a quantity value is invalid.
 */
public class InvalidQuantityException extends ValidationException {

    public InvalidQuantityException(String message) {
        super("quantity", message);
    }
}
