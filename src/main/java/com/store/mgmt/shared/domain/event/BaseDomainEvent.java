package com.store.mgmt.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base implementation of DomainEvent with common fields.
 */
public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;
    private final UUID aggregateId;
    private final String aggregateType;

    protected BaseDomainEvent(UUID aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public UUID getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }
}
