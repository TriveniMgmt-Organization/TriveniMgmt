package com.store.mgmt.modules.products.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when price validation fails.
 */
public class InvalidPriceException extends DomainException {

    public InvalidPriceException(String message) {
        super(message);
    }

    public static InvalidPriceException retailPriceBelowCost() {
        return new InvalidPriceException("Retail price cannot be lower than cost price");
    }
}
