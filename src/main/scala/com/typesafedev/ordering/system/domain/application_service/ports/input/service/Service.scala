package com.typesafedev.ordering.system.domain.application_service.ports.input.service

import com.typesafedev.ordering.system.domain.application_service.dto.create.OrderCommands
import com.typesafedev.ordering.system.domain.application_service.dto.create.OrderCommands.{CreateOrderCommand, CreateOrderResponse, OrderAddress, OrderItem}
import com.typesafedev.ordering.system.domain.application_service.dto.track.TrackOrder
import com.typesafedev.ordering.system.domain.application_service.dto.track.TrackOrder.{TrackOrderQuery, TrackOrderResponse}
import com.typesafedev.ordering.system.domain.application_service.ports.input.service.Service.OrderServiceError.{CustomerNotFound, FailToPublishCreateOrderEvent, FailToSaveOrder, InvalidOrder, RestaurantNotFound}
import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.Repositories._
import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.message.publisher.payment.Payment.OrderCreatedPaymentRequestMessagePublisher
import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.{Customer, Order, Restaurant}
import com.typesafedev.ordering.system.domain.core.Domain.BaseEntity.Product
import com.typesafedev.ordering.system.domain.core.Domain.{BaseEntity, CustomerId, OrderDomainService, OrderId, OrderItemId, RestaurantId, StreetAddress, StreetAddressId}
import zio.{ULayer, ZIO, ZLayer}

import java.util.UUID

object Service {

  sealed trait OrderServiceError {
    val message: String
  }
  object OrderServiceError {
    final case class CustomerNotFound(message: String) extends OrderServiceError
    final case class RestaurantNotFound(message: String) extends OrderServiceError
    final case class InvalidOrder(message: String) extends OrderServiceError
    final case class FailToSaveOrder(message: String) extends OrderServiceError
    final case class FailToPublishCreateOrderEvent(message: String) extends OrderServiceError
  }

  trait OrderApplicationService {
    def createOrder(command: CreateOrderCommand): ZIO[Any, OrderServiceError, CreateOrderResponse]
    def trackOrder(query: TrackOrderQuery): ZIO[Any, OrderServiceError, TrackOrderResponse]
  }

  object OrderApplicationService {
    val live: ZLayer[OrderRepository with CustomerRepository with RestaurantRepository with OrderCreatedPaymentRequestMessagePublisher, Nothing, OrderApplicationServiceLive] = ZLayer.fromFunction(OrderApplicationServiceLive(_, _, _, _))

    def createOrder(command: OrderCommands.CreateOrderCommand):  ZIO[OrderApplicationServiceLive, OrderServiceError, CreateOrderResponse] =
      ZIO.serviceWithZIO[OrderApplicationServiceLive](_.createOrder(command))

    def trackOrder(query: TrackOrder.TrackOrderQuery): ZIO[OrderApplicationServiceLive, OrderServiceError, TrackOrderResponse] =
      ZIO.serviceWithZIO[OrderApplicationServiceLive](_.trackOrder(query))
  }

  final case class OrderApplicationServiceLive(orderRepository: OrderRepository, customerRepository: CustomerRepository, restaurantRepository: RestaurantRepository, orderCreatedEventPublisher: OrderCreatedPaymentRequestMessagePublisher) extends OrderApplicationService {
    override def createOrder(command: OrderCommands.CreateOrderCommand):  ZIO[Any, OrderServiceError, CreateOrderResponse] = {
      val orderId: OrderId = OrderId(UUID.randomUUID())
      val transformOrderItemToOrderItemEntity = commandOrderItemToOrderItemEntity(orderId)_
      checkCustomer(command.customerId) *>  checkRestaurant(command).flatMap { restaurant =>
        val order = Order(command.customerId, restaurant.restaurantId, deliveryAddress = orderAddressToStreetAddress(command.address),
          price = command.price,
          orderItems = command.items.zipWithIndex.map {
            case (oi, index) => transformOrderItemToOrderItemEntity(oi).copy(orderItemId = OrderItemId(index))
          },
         orderId = orderId)
        OrderDomainService.validateAndInitiateOrder(order, restaurant) match {
          case Left(value) => ZIO.fail(InvalidOrder(value.map(_.message).mkString("\n")))
          case Right(value) =>
            orderRepository.save(value.order).mapError(err => FailToSaveOrder(err.getMessage)).flatMap { order =>
            orderCreatedEventPublisher.publish(value).mapBoth(err => FailToPublishCreateOrderEvent(err.getMessage), _ => order)
          }
        }

      }.map { order =>
        CreateOrderResponse(
          order.trackingId,
          order.orderStatus,
          message = "Success"
        )

      }

    }

    override def trackOrder(query: TrackOrder.TrackOrderQuery): ZIO[Any, OrderServiceError, TrackOrderResponse] = {
      orderRepository.findByTrackingId(query.trackOrderId)
        .mapError(err => InvalidOrder(s"Order could not be found using the tracking id: ${query.trackOrderId}. Details: ${err.getMessage}"))
        .flatMap {
          case Some(value) =>  ZIO.succeed(TrackOrderResponse(
            query.trackOrderId,
            value.orderStatus,
            failures = List.empty
          ))
          case None => ZIO.fail( InvalidOrder(s"Order could not be found using the tracking id: ${query.trackOrderId}."))
        }

    }

    private def checkCustomer(customerId: CustomerId): ZIO[Any, OrderServiceError,  Customer] = {
      customerRepository
        .findCustomer(customerId)
        .mapError(err => CustomerNotFound(s"Details: ${err.getMessage}"))
        .flatMap(op => ZIO.fromOption(op).orElseFail(CustomerNotFound(s"There is no customer in the system wth id $customerId.")))
    }

    private def checkRestaurant(command: OrderCommands.CreateOrderCommand): ZIO[Any, OrderServiceError, Restaurant] = {
        val restaurant = Restaurant(command.restaurantId, command.items.map(orderItem => Product(orderItem.productId)), active = true )
         restaurantRepository.findRestaurantInformation(restaurant)
           .mapError(err => RestaurantNotFound(s"Details: ${err.getMessage}"))
           .flatMap(op => ZIO.fromOption(op).orElseFail(RestaurantNotFound(s"There is no restaurant in the system wth id ${command.restaurantId}.")))
    }

    private def orderAddressToStreetAddress(orderAddress: OrderAddress):  StreetAddress = {
      StreetAddress(
        id = StreetAddressId(UUID.randomUUID()),
        street =  orderAddress.street,
        postalCode = orderAddress.postalCode,
        city = orderAddress.city
      )
    }

    private def commandOrderItemToOrderItemEntity(orderId: OrderId)(orderItem: OrderItem): BaseEntity.OrderItem = {
      BaseEntity.OrderItem(
        orderItemId = OrderItemId(0),
        orderId = orderId,
        product = Product(orderItem.productId),
        quantity = orderItem.quantity,
        price = orderItem.price,
        subTotal = orderItem.subTotal
      )
    }
  }
}
