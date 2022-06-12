package com.typesafedev.ordering.system.domain.core.events

import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.Order

import java.time.ZonedDateTime

object Events {

  sealed trait DomainEvent
  object DomainEvent {
    // events
    final case class OrderCreatedEvent(order: Order, createdAt: ZonedDateTime) extends DomainEvent

    final case class OrderPaidEvent(order: Order, createdAt: ZonedDateTime) extends DomainEvent

    final case class OrderCancelledEvent(order: Order, createdAt: ZonedDateTime) extends DomainEvent
  }

}
