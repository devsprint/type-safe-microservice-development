package com.typesafedev.ordering.system.domain.application_service.ports.output.repository.message.publisher.payment

import com.typesafedev.ordering.system.domain.core.events.Events.DomainEvent.OrderCancelledEvent
import com.typesafedev.ordering.system.domain.core.events.publisher.Publisher.DomainEventPublisher

trait OrderCancelledPaymentRequestMessagePublisher extends DomainEventPublisher[OrderCancelledEvent]{

}
