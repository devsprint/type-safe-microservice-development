package com.typesafedev.ordering.system.domain.application_service.dto.create

import com.typesafedev.ordering.system.domain.core.Domain.{CustomerId, Money, OrderStatus, ProductId, RestaurantId, TrackingId}

object OrderCommands {

  final case class OrderItem(productId: ProductId, quantity: Integer, price: Money, subTotal: Money)
  final case class OrderAddress(street: String, postalCode: String, city: String)

  final case class CreateOrderCommand(
      customerId: CustomerId,
      restaurantId: RestaurantId,
      price: Money,
      items: List[OrderItem],
      address: OrderAddress)


  final case class CreateOrderResponse(
      orderTrackingId: TrackingId,
      orderStatus: OrderStatus,
      message: String

                                     )

}
