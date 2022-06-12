package com.typesafedev.ordering.system.domain.application_service.ports.output.repository

import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.Order
import com.typesafedev.ordering.system.domain.core.Domain.TrackingId
import zio.ZIO

trait OrderRepository {

  def save(order: Order): ZIO[Any,Throwable,Order]
  def findByTrackingId (trackingId: TrackingId): ZIO[Any, Throwable, Option[Order]]
}
