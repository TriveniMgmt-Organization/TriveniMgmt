package com.store.mgmt.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events.
 * Domain events represent something meaningful that happened in the domain.
 */
public interface DomainEvent {

    /**
     * Unique identifier for this event instance.
     */
    UUID getEventId();

    /**
     * When this event occurred.
     */
    Instant getOccurredAt();

    /**
     * The aggregate that produced this event.
     */
    UUID getAggregateId();

    /**
     * The type of aggregate that produced this event.
     */
    String getAggregateType();
}
