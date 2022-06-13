package com.typesafedev.ordering.system.application

import com.typesafedev.ordering.system.domain.application_service.dto.create.OrderCommands.{CreateOrderCommand, CreateOrderResponse, OrderAddress, OrderItem}
import com.typesafedev.ordering.system.domain.application_service.dto.track.TrackOrder.TrackOrderResponse
import com.typesafedev.ordering.system.domain.core.Domain.{CustomerId, DomainError, Money, OrderStatus, ProductId, RestaurantId, TrackingId}
import zio.json._

import java.util.UUID

object JsonSupport {

  implicit val customerIdDecoder: JsonDecoder[CustomerId] = JsonDecoder[UUID].map(CustomerId(_))
  implicit val restaurantIdDecoder: JsonDecoder[RestaurantId] = JsonDecoder[UUID].map(RestaurantId(_))
  implicit val trackingIdEncoder: JsonEncoder[TrackingId] = JsonEncoder[UUID].contramap(TrackingId.unwrap)
  implicit val productIdDecoder: JsonDecoder[ProductId] = JsonDecoder[UUID].map(ProductId(_))
  implicit val moneyDecoder: JsonDecoder[Money] = DeriveJsonDecoder.gen[Money]
  implicit val orderAddressDecoder: JsonDecoder[OrderAddress] = DeriveJsonDecoder.gen[OrderAddress]
  implicit val orderStatusEncoder: JsonEncoder[OrderStatus] = DeriveJsonEncoder.gen[OrderStatus]
  implicit val orderItemDecoder: JsonDecoder[OrderItem] = DeriveJsonDecoder.gen[OrderItem]
  implicit val decoder: JsonDecoder[CreateOrderCommand] = DeriveJsonDecoder.gen[CreateOrderCommand]
  implicit val encoder: JsonEncoder[CreateOrderResponse] = DeriveJsonEncoder.gen[CreateOrderResponse]

  implicit val domainErrorEncoder: JsonEncoder[DomainError] = DeriveJsonEncoder.gen[DomainError]
  implicit val trackOrderResponseEncoder: JsonEncoder[TrackOrderResponse] = DeriveJsonEncoder.gen[TrackOrderResponse]

}
