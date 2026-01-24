package com.store.mgmt.modules.products.domain.exception;

import com.store.mgmt.modules.products.domain.model.Barcode;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to create a variant with a duplicate barcode.
 */
public class DuplicateBarcodeException extends DomainException {

    public DuplicateBarcodeException(Barcode barcode) {
        super("Barcode already exists: " + barcode.getValue());
    }
}
