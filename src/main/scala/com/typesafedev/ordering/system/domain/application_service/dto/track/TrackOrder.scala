package com.typesafedev.ordering.system.domain.application_service.dto.track

import com.typesafedev.ordering.system.domain.core.Domain.{DomainError, OrderStatus, TrackingId}

object TrackOrder {
  final case class TrackOrderQuery(trackOrderId: TrackingId)

  final case class TrackOrderResponse(trackOrderId: TrackingId, orderStatus: OrderStatus, failures: List[DomainError])
}
