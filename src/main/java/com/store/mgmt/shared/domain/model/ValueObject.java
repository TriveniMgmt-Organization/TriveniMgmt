package com.store.mgmt.shared.domain.model;

/**
 * Marker interface for value objects.
 * Value objects are immutable and compared by their attributes, not identity.
 *
 * Implementing classes should:
 * 1. Be immutable (all fields final)
 * 2. Override equals() and hashCode() based on all fields
 * 3. Have no identity (no ID field)
 *
 * Consider using Java records for value objects.
 */
public interface ValueObject {
    // Marker interface - implementing classes must override equals() and hashCode()
}
