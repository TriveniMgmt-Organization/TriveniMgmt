package com.store.mgmt.shared.infrastructure.event;

import com.store.mgmt.shared.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events using Spring's ApplicationEventPublisher.
 * Events can be handled by @EventListener or @TransactionalEventListener methods.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} for aggregate {}",
                event.getClass().getSimpleName(),
                event.getAggregateId());

        springPublisher.publishEvent(event);
    }
}
