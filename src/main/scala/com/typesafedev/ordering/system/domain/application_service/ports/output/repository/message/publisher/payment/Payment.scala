package com.typesafedev.ordering.system.domain.application_service.ports.output.repository.message.publisher.payment

import com.typesafedev.ordering.system.domain.core.events.Events.DomainEvent.{OrderCancelledEvent, OrderCreatedEvent}
import com.typesafedev.ordering.system.domain.core.events.publisher.Publisher.DomainEventPublisher

object Payment {

  trait OrderCreatedPaymentRequestMessagePublisher extends DomainEventPublisher[OrderCreatedEvent]

  trait OrderCancelledPaymentRequestMessagePublisher extends DomainEventPublisher[OrderCancelledEvent]
}
