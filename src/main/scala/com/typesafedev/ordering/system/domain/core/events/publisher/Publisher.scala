package com.typesafedev.ordering.system.domain.core.events.publisher

import com.typesafedev.ordering.system.domain.core.events.Events.DomainEvent

object Publisher {

  trait DomainEventPublisher[T <: DomainEvent] {

    def publish(event: T): Unit

  }

}
