package com.typesafedev.ordering.system.domain.application_service.dto.message

import com.typesafedev.ordering.system.domain.core.Domain.{DomainError, OrderApprovalStatus, PaymentStatus}

import java.time.Instant

object Messages {
  final case class PaymentResponse(
      id: String,
      sagaId: String,
      orderId: String,
      paymentId: String,
      customerId: String,
      price: BigDecimal,
      createdAt: Instant,
      paymentStatus: PaymentStatus,
      failures: List[DomainError]
                                  )

  final case class RestaurantApprovalResponse(
      id: String,
      sagaId: String,
      orderId: String,
      restaurantId: String,
      createdAt: Instant,
      orderApprovalStatus: OrderApprovalStatus,
      failures: List[DomainError]
                                             )
}
