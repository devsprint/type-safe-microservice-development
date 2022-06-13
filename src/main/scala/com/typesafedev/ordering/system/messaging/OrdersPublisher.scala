package com.typesafedev.ordering.system.messaging

import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.message.publisher.payment.Payment.OrderCreatedPaymentRequestMessagePublisher
import com.typesafedev.ordering.system.domain.core.events.Events.DomainEvent
import zio.{Task, ZLayer}

object OrdersPublisher {

  object OrdersCreatedPublisher{
    val live = ZLayer.succeed(OrdersCreatedPublisherLive())
  }


  final case class OrdersCreatedPublisherLive() extends OrderCreatedPaymentRequestMessagePublisher {
    override def publish(event: DomainEvent.OrderCreatedEvent): Task[Unit] = ???
  }

}
