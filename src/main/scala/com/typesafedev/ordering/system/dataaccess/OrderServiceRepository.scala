package com.typesafedev.ordering.system.dataaccess

import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.Repositories.OrderRepository
import com.typesafedev.ordering.system.domain.core.Domain.{AggregateRoot, TrackingId}
import zio.{ZIO, ZLayer}

object OrderServiceRepository {

  object OrderRepository {

    val live  = ZLayer.succeed(OrderRepositoryLive())
  }


  final case class OrderRepositoryLive() extends OrderRepository {

    override def save(order: AggregateRoot.Order): ZIO[Any, Throwable, AggregateRoot.Order] = ???

    override def findByTrackingId(trackingId: TrackingId): ZIO[Any, Throwable, Option[AggregateRoot.Order]] = ???
  }

}
